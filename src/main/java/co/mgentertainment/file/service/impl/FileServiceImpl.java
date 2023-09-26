package co.mgentertainment.file.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.xuyanwu.spring.file.storage.FileInfo;
import cn.xuyanwu.spring.file.storage.FileStorageProperties;
import cn.xuyanwu.spring.file.storage.FileStorageService;
import cn.xuyanwu.spring.file.storage.UploadPretreatment;
import cn.xuyanwu.spring.file.storage.constant.Constant;
import cn.xuyanwu.spring.file.storage.spring.SpringFileStorageProperties;
import cn.xuyanwu.spring.file.storage.tika.ContentTypeDetect;
import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.model.media.ResourceSuffix;
import co.mgentertainment.common.model.media.ResourceTypeEnum;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.uidgen.impl.CachedUidGenerator;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.common.utils.SecurityHelper;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import co.mgentertainment.file.dal.po.ResourceDO;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.dal.repository.ResourceRepository;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.MgfsProperties;
import co.mgentertainment.file.service.config.ResourcePathType;
import co.mgentertainment.file.service.config.VideoType;
import co.mgentertainment.file.service.converter.FileObjectMapper;
import co.mgentertainment.file.service.dto.*;
import co.mgentertainment.file.service.event.VideoConvertEvent;
import co.mgentertainment.file.service.event.VideoCutEvent;
import co.mgentertainment.file.service.event.VideoUploadEvent;
import co.mgentertainment.file.service.utils.MediaHelper;
import co.mgentertainment.file.web.cache.ClientHolder;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest;
import com.amazonaws.services.s3.model.ownership.ObjectOwnership;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author larry
 * @createTime 2023/9/14
 * @description FileServiceImpl
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService, InitializingBean {
    private final List<String> imageTypes = Lists.newArrayList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");
    private final List<String> videoTypes = Lists.newArrayList("mp4", "avi", "mov", "wmv", "flv", "f4v", "rmvb", "rm", "mkv", "3gp", "dat", "ts", "mts", "vob");
    private final List<String> packageTypes = Lists.newArrayList("apk", "ipa", "hap");
    private Executor uploadExecutor;

    @Resource
    private SpringFileStorageProperties springFileStorageProperties;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private ResourceRepository resourceRepository;

    @Resource
    private FileUploadRepository fileUploadRepository;

    @Resource
    private ContentTypeDetect contentTypeDetect;

    @Resource
    private MgfsProperties mgfsProperties;

    @Resource
    private AsyncEventBus eventBus;

    @Resource
    private CachedUidGenerator cachedUidGenerator;

    private AmazonS3 s3Client;

    @Override
    public void afterPropertiesSet() {
        List<? extends FileStorageProperties.AmazonS3Config> s3Configs = springFileStorageProperties.getAmazonS3();
        if (CollectionUtils.isEmpty(s3Configs)) {
            return;
        }
        FileStorageProperties.AmazonS3Config s3Config = s3Configs.get(0);
        String accessKey = s3Config.getAccessKey();
        String secretKey = s3Config.getSecretKey();
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withRegion(s3Config.getRegion())
                .withClientConfiguration(
                        new ClientConfiguration().withProtocol(Protocol.HTTP)
                )
                .withPathStyleAccessEnabled(true)
                .withChunkedEncodingDisabled(true)
                .build();
        createBucketIfNotExists(s3Config);
        this.uploadExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 8, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("upload-executor-" + RandomStringUtils.randomAlphanumeric(4));
            return thread;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadedImageDTO uploadImage(MultipartFile multipartFile) {
        if (getResourceType(multipartFile) != ResourceTypeEnum.IMAGE) {
            throw new IllegalArgumentException("file type is not image");
        }
        Map<ResourcePathType, String> map = file2CloudStorage(multipartFile, ResourceTypeEnum.IMAGE);
        return UploadedImageDTO.builder().filename(multipartFile.getOriginalFilename()).imagePath(map.get(ResourcePathType.IMAGE)).thumbnailPath(map.get(ResourcePathType.THUMBNAIL)).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadedFileDTO uploadFile(MultipartFile multipartFile) {
        ResourceTypeEnum resourceType = getResourceType(multipartFile);
        if (resourceType == ResourceTypeEnum.IMAGE || resourceType == ResourceTypeEnum.VIDEO) {
            throw new IllegalArgumentException("wrong file type");
        }
        Map<ResourcePathType, String> map = file2CloudStorage(multipartFile, resourceType);
        return UploadedFileDTO.builder().filename(multipartFile.getOriginalFilename()).remotePath(map.get(ResourcePathType.DEFAULT)).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoUploadInfoDTO uploadVideo(MultipartFile multipartFile, CuttingSetting cuttingSetting) {
        if (cuttingSetting == null) {
            // 填充默认设置
            cuttingSetting = CuttingSetting.builder().duration(mgfsProperties.getUserTrailerTimeLength()).startFromProportion(0).build();
        }
        long size = multipartFile.getSize();
        if (getResourceType(multipartFile) != ResourceTypeEnum.VIDEO) {
            throw new IllegalArgumentException("file type is not video");
        }
        String filename = multipartFile.getOriginalFilename();
        log.debug("(1.1)添加上传记录:{}", filename);
        Map<String, Long> map = this.batchAddUploadVideoRecord(ListUtil.of(filename));
        Long uploadId = map.get(filename);
        File file;
        try {
            file = saveMultipartFileInDisk(multipartFile, uploadId);
        } catch (IOException e) {
            throw new RuntimeException("fail to persist file", e);
        }
        log.debug("(1.2)已上传记录:{} uploadId:{}", filename, uploadId);
        eventBus.post(
                VideoConvertEvent.builder()
                        .uploadId(uploadId)
                        .originVideo(file)
                        .cuttingSetting(cuttingSetting)
                        .appName(ClientHolder.getCurrentClient())
                        .build());
        return VideoUploadInfoDTO.builder().uploadId(uploadId).filename(filename).size(MediaHelper.getMediaSize(size) + "kb").status(UploadStatusEnum.CONVERTING.getDesc()).statusCode(UploadStatusEnum.CONVERTING.getValue()).uploadStartTime(new Date()).build();
    }

    @Override
    public void reuploadVideo(Long uploadId, CuttingSetting cuttingSetting) {
        FileUploadDO fileUploadDO = fileUploadRepository.getFileUploadByUploadId(uploadId);
        File originVideo = MediaHelper.getUploadFileParentDirByUploadId(uploadId);
        if (fileUploadDO == null) {
            if (originVideo.exists()) {
                FileUtil.del(originVideo);
            }
            throw new IllegalArgumentException("uploadId not exists");
        }
        if (!originVideo.exists()) {
            updateUploadStatus(uploadId, UploadStatusEnum.VIDEO_DAMAGED_OR_LOST);
            return;
        }
        UploadStatusEnum oldStatus = UploadStatusEnum.getByValue(fileUploadDO.getStatus().intValue());
        File filmVideo = MediaHelper.getProcessedFileByOriginFile(originVideo, VideoType.FEATURE_FILM.getValue(), ResourceSuffix.FEATURE_FILM);
        File trailerVideo = MediaHelper.getProcessedFileByOriginFile(originVideo, VideoType.TRAILER.getValue(), ResourceSuffix.TRAILER);
        switch (oldStatus) {
            case CONVERTING:
                if (filmVideo.exists()) {
                    eventBus.post(
                            VideoUploadEvent.builder()
                                    .uploadId(uploadId)
                                    .processedVideo(filmVideo)
                                    .originVideo(originVideo)
                                    .videoType(VideoType.FEATURE_FILM)
                                    .cuttingSetting(cuttingSetting)
                                    .appName(ClientHolder.getCurrentClient())
                                    .build());
                } else {
                    eventBus.post(
                            VideoConvertEvent.builder()
                                    .uploadId(uploadId)
                                    .originVideo(originVideo)
                                    .cuttingSetting(cuttingSetting)
                                    .appName(ClientHolder.getCurrentClient())
                                    .build());
                }
                break;
            case UPLOADING:
                if (fileUploadDO.getRid() == null) {
                    eventBus.post(
                            VideoUploadEvent.builder()
                                    .uploadId(uploadId)
                                    .processedVideo(filmVideo)
                                    .originVideo(originVideo)
                                    .videoType(VideoType.FEATURE_FILM)
                                    .cuttingSetting(cuttingSetting)
                                    .appName(ClientHolder.getCurrentClient())
                                    .build());
                } else {
                    ResourceDO resourceDO = resourceRepository.getResourceByRid(fileUploadDO.getRid());
                    if (resourceDO == null) {
                        eventBus.post(
                                VideoUploadEvent.builder()
                                        .uploadId(uploadId)
                                        .processedVideo(filmVideo)
                                        .originVideo(originVideo)
                                        .videoType(VideoType.FEATURE_FILM)
                                        .cuttingSetting(cuttingSetting)
                                        .appName(ClientHolder.getCurrentClient())
                                        .build());
                        break;
                    }
                    eventBus.post(
                            VideoCutEvent.builder()
                                    .uploadId(uploadId)
                                    .originVideo(originVideo)
                                    .cuttingSetting(cuttingSetting)
                                    .rid(fileUploadDO.getRid())
                                    .build());
                }
                break;
            case TRAILER_CUTTING_AND_UPLOADING:
                if (trailerVideo.exists()) {
                    ResourceDO resourceDO = resourceRepository.getResourceByRid(fileUploadDO.getRid());
                    if (resourceDO == null) {
                        eventBus.post(
                                VideoUploadEvent.builder()
                                        .uploadId(uploadId)
                                        .processedVideo(filmVideo)
                                        .originVideo(originVideo)
                                        .videoType(VideoType.FEATURE_FILM)
                                        .cuttingSetting(cuttingSetting)
                                        .appName(ClientHolder.getCurrentClient())
                                        .build());
                    } else {
                        eventBus.post(
                                VideoUploadEvent.builder()
                                        .uploadId(uploadId)
                                        .processedVideo(trailerVideo)
                                        .originVideo(originVideo)
                                        .videoType(VideoType.TRAILER)
                                        .rid(resourceDO.getRid())
                                        .build());
                    }
                } else {
                    eventBus.post(
                            VideoCutEvent.builder()
                                    .uploadId(uploadId)
                                    .originVideo(originVideo)
                                    .cuttingSetting(cuttingSetting)
                                    .rid(fileUploadDO.getRid())
                                    .build());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void files2CloudStorage(File[] files, ResourceTypeEnum resourceType, String subDirName, Long rid) {
        if (Objects.isNull(files) || files.length == 0) {
            log.error("no files need to be upload");
            return;
        }
        String cloudPath = getCloudPath(resourceType, subDirName, rid,
                resourceType == ResourceTypeEnum.VIDEO ? ResourcePathType.FEATURE_FILM.getValue() : null);
        List<CompletableFuture<Object>> cfs = Arrays.stream(files).parallel().map(file ->
                CompletableFuture.supplyAsync(() -> {
                    prepareUploadPretreatment(file, cloudPath, false).upload();
                    return null;
                }, this.uploadExecutor).exceptionally(throwable -> {
                    String filePath = file.getAbsolutePath();
                    log.error("fail to upload file:{}", filePath, throwable);
                    return filePath;
                })
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(cfs)) {
            CompletableFuture<List<Object>> cf = CompletableFuture.allOf(
                    cfs.toArray(new CompletableFuture[cfs.size()])).thenApplyAsync(
                    v -> cfs.stream().map(CompletableFuture::join).collect(Collectors.toList()));
            List<Object> failedFilePaths = Lists.newArrayList();
            try {
                cf.join();
                failedFilePaths = cf.get().stream().filter(Objects::nonNull).collect(Collectors.toList());
            } catch (Exception ignored) {
            } finally {
                // 失败的文件再次上传
                if (CollectionUtils.isNotEmpty(failedFilePaths)) {
                    File[] retryFiles = failedFilePaths.stream().map(f -> new File(String.valueOf(f))).toArray(File[]::new);
                    files2CloudStorage(retryFiles, resourceType, subDirName, rid);
                }
            }
        }
    }

    @Override
    public List<VideoUploadInfoDTO> getUploadInfos(List<Long> uploadIds) {
        FileUploadExample example = new FileUploadExample();
        example.createCriteria().andDeletedEqualTo((byte) 0).andUploadIdIn(uploadIds);
        List<FileUploadDO> fileUploadDOS = fileUploadRepository.getFileUploadsByExample(example);
        if (CollectionUtils.isEmpty(fileUploadDOS)) {
            return Lists.newArrayList();
        }
        return toVideoUploadInfoDTOList(fileUploadDOS);
    }

    @Override
    public PageResult<VideoUploadInfoDTO> queryFileUpload(QueryUploadConditionDTO condition) {
        FileUploadExample example = new FileUploadExample();
        example.setLimit(condition.getPageSize());
        example.setOffset((condition.getPageNo() - 1) * condition.getPageSize());
        FileUploadExample.Criteria criteria = example.createCriteria().andDeletedEqualTo((byte) 0);
        if (StringUtils.isNotBlank(ClientHolder.getCurrentClient())) {
            criteria.andAppNameEqualTo(ClientHolder.getCurrentClient());
        }
        if (StringUtils.isNotBlank(condition.getFilename())) {
            criteria.andFilenameLike(String.format("%%%s%%", condition.getFilename()));
        }
        if (null != condition.getResourceType()) {
            criteria.andTypeEqualTo(condition.getResourceType().shortValue());
        }
        if (null != condition.getStatus()) {
            criteria.andStatusEqualTo(condition.getStatus().shortValue());
        }
        if (null != condition.getUploadStartDate() && null != condition.getUploadEndDate()) {
            criteria.andCreateTimeBetween(condition.getUploadStartDate(), condition.getUploadEndDate());
        }
        example.setOrderByClause("create_time desc");
        PageResult<FileUploadDO> pr = fileUploadRepository.queryFileUpload(example);
        List<VideoUploadInfoDTO> dtoList = toVideoUploadInfoDTOList(pr.getRecords());
        return PageResult.createPageResult(pr.getCurrent(), pr.getSize(), pr.getTotal(), dtoList);
    }

    @Override
    public void uploadLocalTrailUnderResource(File trailVideo, Long rid, String subDirName) {
        String filename = trailVideo.getName();
        String cloudPath = getCloudPath(ResourceTypeEnum.VIDEO, subDirName, rid, ResourcePathType.TRAILER.getValue());
        upload2CloudStorage(trailVideo, cloudPath, filename, false);
    }

    @Override
    public Map<String, Long> batchAddUploadVideoRecord(List<String> filename) {
        return fileUploadRepository.batchAddFileUpload(filename, ResourceTypeEnum.VIDEO, ClientHolder.getCurrentClient());
    }

    @Override
    public void updateUpload(FileUploadDTO fileUploadDTO) {
        FileUploadDO fileUploadDO = FileObjectMapper.INSTANCE.toFileUploadDO(fileUploadDTO);
        fileUploadRepository.updateFileUploadByPrimaryKey(fileUploadDO);
    }

    @Override
    public void updateUploadStatus(Long uploadId, UploadStatusEnum status) {
        FileUploadDO fileUploadDO = new FileUploadDO();
        fileUploadDO.setUploadId(uploadId);
        fileUploadDO.setStatus(status.getValue().shortValue());
        fileUploadRepository.updateFileUploadByPrimaryKey(fileUploadDO);
    }

    @Override
    public void updateUploadRid(Long uploadId, UploadStatusEnum status, Long rid) {
        FileUploadDO fileUploadDO = new FileUploadDO();
        fileUploadDO.setUploadId(uploadId);
        fileUploadDO.setRid(rid);
        fileUploadDO.setStatus(status.getValue().shortValue());
        fileUploadRepository.updateFileUploadByPrimaryKey(fileUploadDO);
    }

    @Override
    public Long saveResource(ResourceDTO resourceDTO) {
        ResourceDO resourceDO = FileObjectMapper.INSTANCE.toResourceDO(resourceDTO);
        if (StringUtils.isNotEmpty(ClientHolder.getCurrentClient())) {
            resourceDO.setAppName(ClientHolder.getCurrentClient());
        }
        return resourceRepository.saveResource(resourceDO);
    }

    private Map<ResourcePathType, String> file2CloudStorage(MultipartFile multipartFile, ResourceTypeEnum resourceType) {
        String filename = multipartFile.getOriginalFilename();
        String subDirName = DateUtils.format(new Date(), DateUtils.FORMAT_YYYYMMDD);
        long rid = cachedUidGenerator.getUID();
        String cloudPath = getCloudPath(resourceType, subDirName, rid, null);
        boolean isImage = resourceType == ResourceTypeEnum.IMAGE;
        upload2CloudStorage(multipartFile, cloudPath, filename, isImage);
        // 添加resource记录
        ResourceDTO resourceDTO = ResourceDTO.builder()
                .rid(rid)
                .filename(filename)
                .type(Integer.valueOf(resourceType.getValue()).shortValue())
                .folder(subDirName)
                .size(new BigDecimal(multipartFile.getSize()))
                .build();
        this.saveResource(resourceDTO);
        Map<ResourcePathType, String> pathMap = new HashMap<>(0);
        if (isImage) {
            pathMap.put(ResourcePathType.IMAGE, retrieveResourcePath(cloudPath, filename, null));
            pathMap.put(ResourcePathType.THUMBNAIL, retrieveResourcePath(cloudPath, filename, springFileStorageProperties.getThumbnailSuffix()));
        } else {
            pathMap.put(ResourcePathType.DEFAULT, retrieveResourcePath(cloudPath, filename, null));
        }
        return pathMap;
    }

    private ResourceTypeEnum getResourceType(MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String fileType;
        try {
            fileType = contentTypeDetect.detect(multipartFile.getInputStream(), filename);
        } catch (IOException e) {
            fileType = StringUtils.lowerCase(StringUtils.substringAfterLast(filename, "."));
        }
        String finalFileType = fileType;
        return imageTypes.stream().anyMatch(type -> finalFileType.contains(type)) ?
                ResourceTypeEnum.IMAGE : videoTypes.stream().anyMatch(type -> finalFileType.contains(type)) ? ResourceTypeEnum.VIDEO :
                packageTypes.stream().anyMatch(type -> finalFileType.contains(type)) ? ResourceTypeEnum.PACKAGE : ResourceTypeEnum.OTHER;
    }

    private String retrieveResourcePath(String resourceFolderLocation, String filename, String suffix) {
        filename = StringUtils.isEmpty(suffix) ? filename : StringUtils.substringBeforeLast(filename, ".") + suffix;
        String resourcePath = new StringBuilder('/').append(resourceFolderLocation).append(filename).toString();
        if (mgfsProperties.getEncryption().isEnabled()) {
            return SecurityHelper.aesEncrypt(resourcePath, mgfsProperties.getEncryption().getSecret());
        }
        return resourcePath;
    }

    private UploadPretreatment prepareUploadPretreatment(File file, String cloudPath, boolean isImage) {
        UploadPretreatment uploadPretreatment = fileStorageService.of(file)
                .setSaveFilename(file.getName())
                .setPath(cloudPath)
                .setAcl(Constant.ACL.PUBLIC_READ);
        if (isImage) {
            uploadPretreatment
                    .setSaveThFilename(StringUtils.substringBeforeLast(file.getName(), "."))
                    .setThumbnailSuffix(springFileStorageProperties.getThumbnailSuffix())
                    .thumbnail(th -> th.scale(1f).outputQuality(0.3f));
        }
        return uploadPretreatment;
    }

    private List<VideoUploadInfoDTO> toVideoUploadInfoDTOList(List<FileUploadDO> fileUploadDOS) {
        if (CollectionUtils.isEmpty(fileUploadDOS)) {
            return Lists.newArrayList();
        }
        List<Long> uploadIds = fileUploadDOS.stream().map(FileUploadDO::getUploadId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(uploadIds)) {
            return Lists.newArrayList();
        }
        Map<Long, ResourceDO> ridMap = resourceRepository.getResourceByUploadIds(uploadIds).stream().collect(Collectors.toMap(ResourceDO::getRid, r -> r));
        return fileUploadDOS.stream().map(fileUploadDO -> VideoUploadInfoDTO.builder()
                .filename(fileUploadDO.getFilename())
                .size(ridMap.containsKey(fileUploadDO.getRid()) ? MediaHelper.getMediaSize(ridMap.get(fileUploadDO.getRid()).getSize().longValue()) + "kb" : null)
                .duration(ridMap.containsKey(fileUploadDO.getRid()) ? MediaHelper.formatMediaDuration(ridMap.get(fileUploadDO.getRid()).getDuration()) : null)
                .uploadId(fileUploadDO.getUploadId())
                .status(UploadStatusEnum.getByValue(fileUploadDO.getStatus().intValue()).getDesc())
                .statusCode(UploadStatusEnum.getByValue(fileUploadDO.getStatus().intValue()).getValue())
                .filmPath(ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getCloudPath(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.FEATURE_FILM.getValue()), ridMap.get(fileUploadDO.getRid()).getFilename(), ResourceSuffix.FEATURE_FILM) : null)
                .trailerPath(ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getCloudPath(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.TRAILER.getValue()), ridMap.get(fileUploadDO.getRid()).getFilename(), ResourceSuffix.TRAILER) : null)
                .uploadStartTime(fileUploadDO.getCreateTime())
                .statusUpdateTime(fileUploadDO.getUpdatedTime())
                .build()).collect(Lists::newArrayList, List::add, List::addAll);
    }

    private static String getCloudPath(ResourceTypeEnum type, String subDirName, Long rid, String category) {
        String[] arr = StringUtils.isEmpty(category) ?
                new String[]{type.name().toLowerCase(), subDirName, rid.toString()} :
                new String[]{type.name().toLowerCase(), subDirName, rid.toString(), category};
        return StringUtils.join(arr, '/') + '/';
    }

    private FileInfo upload2CloudStorage(Object file, String cloudPath, String filename, boolean isImage) {
        UploadPretreatment uploadPretreatment = fileStorageService.of(file)
                .setPath(cloudPath)
                .setSaveFilename(filename)
                .setAcl(Constant.ACL.PUBLIC_READ);
        if (isImage) {
            // 生成缩略图
            uploadPretreatment
                    .setSaveThFilename(StringUtils.substringBeforeLast(filename, "."))
                    .setThumbnailSuffix(springFileStorageProperties.getThumbnailSuffix())
                    .thumbnail(th -> th.scale(1f).outputQuality(0.3f));
        }
        return uploadPretreatment.upload();
    }

    private void createBucketIfNotExists(FileStorageProperties.AmazonS3Config s3Config) {
        boolean exists;
        try {
            String bucketName = s3Config.getBucketName();
            exists = s3Client.doesBucketExistV2(bucketName);
            if (exists) {
                return;
            }
            Bucket bucket = s3Client.createBucket(
                    new CreateBucketRequest(bucketName, s3Config.getRegion())
                            .withObjectOwnership(ObjectOwnership.ObjectWriter)
            );
            s3Client.deletePublicAccessBlock(new DeletePublicAccessBlockRequest().withBucketName(bucketName));
            log.debug("new bucket {} created", bucket.getName());
        } catch (Exception e) {
            log.error("error to create bucket", e);
        }
    }

    private File saveMultipartFileInDisk(MultipartFile multipartFile, Long uploadId) throws IOException {
        File folder = MediaHelper.getUploadFileParentDirByUploadId(uploadId);
        FileUtil.mkdir(folder);
        File localFile = new File(folder, multipartFile.getOriginalFilename());
        multipartFile.transferTo(localFile);
        return localFile;
    }
}
