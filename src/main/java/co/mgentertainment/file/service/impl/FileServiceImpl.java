package co.mgentertainment.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.xuyanwu.spring.file.storage.FileInfo;
import cn.xuyanwu.spring.file.storage.FileStorageProperties;
import cn.xuyanwu.spring.file.storage.FileStorageService;
import cn.xuyanwu.spring.file.storage.UploadPretreatment;
import cn.xuyanwu.spring.file.storage.constant.Constant;
import cn.xuyanwu.spring.file.storage.spring.SpringFileStorageProperties;
import cn.xuyanwu.spring.file.storage.tika.ContentTypeDetect;
import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.common.utils.SecurityHelper;
import co.mgentertainment.file.dal.enums.ResourceTypeEnum;
import co.mgentertainment.file.dal.enums.UploadStatusEnum;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import co.mgentertainment.file.dal.po.ResourceDO;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.dal.repository.ResourceRepository;
import co.mgentertainment.file.service.FfmpegService;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.MgfsProperties;
import co.mgentertainment.file.service.config.ResourcePathType;
import co.mgentertainment.file.service.config.ResourceSuffix;
import co.mgentertainment.file.service.dto.QueryUploadConditionDTO;
import co.mgentertainment.file.service.dto.UploadedFileDTO;
import co.mgentertainment.file.service.dto.UploadedImageDTO;
import co.mgentertainment.file.service.dto.VideoUploadInfoDTO;
import co.mgentertainment.file.service.event.VideoConvertEvent;
import co.mgentertainment.file.utils.MediaHelper;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
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
    private FfmpegService ffmpegService;

    @Resource
    private AsyncEventBus eventBus;

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
    public UploadedImageDTO uploadImage(MultipartFile multipartFile) {
        if (getResourceType(multipartFile) != ResourceTypeEnum.IMAGE) {
            throw new IllegalArgumentException("file type is not image");
        }
        Map<ResourcePathType, String> map = file2CloudStorage(multipartFile, ResourceTypeEnum.IMAGE);
        return UploadedImageDTO.builder().filename(multipartFile.getOriginalFilename()).imagePath(map.get(ResourcePathType.IMAGE)).thumbnailPath(map.get(ResourcePathType.THUMBNAIL)).build();
    }

    @Override
    public UploadedFileDTO uploadFile(MultipartFile multipartFile) {
        ResourceTypeEnum resourceType = getResourceType(multipartFile);
        if (resourceType == ResourceTypeEnum.IMAGE || resourceType == ResourceTypeEnum.VIDEO) {
            throw new IllegalArgumentException("wrong file type");
        }
        Map<ResourcePathType, String> map = file2CloudStorage(multipartFile, resourceType);
        return UploadedFileDTO.builder().filename(multipartFile.getOriginalFilename()).remotePath(map.get(ResourcePathType.DEFAULT)).build();
    }

    @Override
    public VideoUploadInfoDTO uploadVideo(MultipartFile multipartFile, CuttingSetting cuttingSetting) {
        long size = multipartFile.getSize();
        if (getResourceType(multipartFile) != ResourceTypeEnum.VIDEO) {
            throw new IllegalArgumentException("file type is not video");
        }
        File file;
        try {
            file = saveMultipartFileInDisk(multipartFile);
        } catch (IOException e) {
            throw new RuntimeException("fail to persist file", e);
        }
        String filename = multipartFile.getOriginalFilename();
        // 添加上传记录
        Long uploadId = this.addUploadRecord(filename);
        eventBus.post(
                VideoConvertEvent.builder()
                        .uploadId(uploadId)
                        .originVideo(file)
                        .cuttingSetting(cuttingSetting)
                        .appName(ClientHolder.getCurrentClient())
                        .build());
        return VideoUploadInfoDTO.builder().uploadId(uploadId).filename(filename).size(MediaHelper.getMediaSize(size) + "kb").status(UploadStatusEnum.CONVERTING.getDesc()).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<ResourcePathType, String> file2CloudStorage(MultipartFile multipartFile, ResourceTypeEnum resourceType) {
        String filename = multipartFile.getOriginalFilename();
        String remoteFolderName = DateUtils.format(new Date(), DateUtils.FORMAT_YYYYMMDD);
        // 添加resource记录
        Long rid = this.persistResource(filename, resourceType, remoteFolderName, multipartFile.getSize(), null, null);
        String resourceFolderLocation = getResourceFolderLocation(resourceType, remoteFolderName, rid, null);
        boolean isImage = resourceType == ResourceTypeEnum.IMAGE;
        upload2CloudStorage(multipartFile, filename, resourceFolderLocation, isImage);
        Map<ResourcePathType, String> pathMap = new HashMap<>(0);
        if (isImage) {
            pathMap.put(ResourcePathType.IMAGE, retrieveResourcePath(resourceFolderLocation, filename, null));
            pathMap.put(ResourcePathType.THUMBNAIL, retrieveResourcePath(resourceFolderLocation, filename, ResourceSuffix.THUMBNAIL));
        } else {
            pathMap.put(ResourcePathType.DEFAULT, retrieveResourcePath(resourceFolderLocation, filename, null));
        }
        return pathMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long media2CloudStorage(File media, ResourceTypeEnum resourceType, String appName, Integer duration) {
        if (media == null || media.getParentFile().isFile()) {
            throw new IllegalArgumentException("media parent is not a directory");
        }
        File[] files = media.getParentFile().listFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        String originFilename = StringUtils.substringBefore(media.getParentFile().getName(), ".");
        String remoteFolderName = DateUtils.format(new Date(), DateUtils.FORMAT_YYYYMMDD);
        // 添加资源记录
        Long rid = this.persistResource(originFilename, resourceType, remoteFolderName, media.length(), appName, duration);
        String folderLocation = getResourceFolderLocation(resourceType, remoteFolderName, rid,
                resourceType == ResourceTypeEnum.VIDEO ? ResourcePathType.FEATURE_FILM.getValue() : null);
        List<UploadPretreatment> list = getCloudStorageUploadList(files, folderLocation, resourceType == ResourceTypeEnum.IMAGE);
        List<CompletableFuture<Boolean>> cfs = list.parallelStream().map(pretreatment ->
                CompletableFuture.supplyAsync(() -> {
                    pretreatment.upload();
                    return true;
                }, this.uploadExecutor).exceptionally(throwable -> {
                    log.error("fail to upload file {}", pretreatment.getOriginalFilename(), throwable);
                    return false;
                })
        ).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(cfs)) {
            CompletableFuture<List<Boolean>> cf = CompletableFuture.allOf(
                    cfs.toArray(new CompletableFuture[cfs.size()])).thenApplyAsync(
                    v -> cfs.stream().map(CompletableFuture::join).collect(Collectors.toList()));
            cf.join();
        }
        return rid;
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
            criteria.andFilenameLike(condition.getFilename());
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
        PageResult<FileUploadDO> pr = fileUploadRepository.queryFileUpload(example);
        List<VideoUploadInfoDTO> dtoList = toVideoUploadInfoDTOList(pr.getRecords());
        return PageResult.createPageResult(pr.getCurrent(), pr.getSize(), pr.getTotal(), dtoList);
    }

    @Override
    public void uploadLocalTrailUnderResource(Long rid, File trailVideo) {
        ResourceDO resourceDO = resourceRepository.getResourceByRid(rid);
        if (resourceDO == null) {
            throw new IllegalArgumentException("resource not found");
        }
        String filename = trailVideo.getName();
        String remoteFolderName = resourceDO.getFolder();
        ResourceTypeEnum resourceType = ResourceTypeEnum.getByValue(resourceDO.getType().intValue());
        String folderLocation = getResourceFolderLocation(resourceType, remoteFolderName, rid, ResourcePathType.TRAILER.getValue());
        upload2CloudStorage(trailVideo, filename, folderLocation, resourceType == ResourceTypeEnum.IMAGE);
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

    private List<UploadPretreatment> getCloudStorageUploadList(File[] files, String remoteFolderLocation, boolean isImage) {
        return isImage ?
                Arrays.stream(files).map(f -> fileStorageService.of(f)
                        .setSaveFilename(f.getName())
                        .setPath(remoteFolderLocation)
                        .setAcl(Constant.ACL.PUBLIC_READ)
                        .setSaveThFilename(StringUtils.substringBeforeLast(f.getName(), "."))
                        .setThumbnailSuffix(ResourceSuffix.THUMBNAIL)
                        .thumbnail(th -> th.scale(1f).outputQuality(0.3f))).collect(Collectors.toList()) :
                Arrays.stream(files).map(f -> fileStorageService.of(f)
                        .setSaveFilename(f.getName())
                        .setPath(remoteFolderLocation)
                        .setAcl(Constant.ACL.PUBLIC_READ)).collect(Collectors.toList());
    }

    private List<VideoUploadInfoDTO> toVideoUploadInfoDTOList(List<FileUploadDO> fileUploadDOS) {
        if (CollectionUtils.isEmpty(fileUploadDOS)) {
            return Lists.newArrayList();
        }
        List<Long> uploadIds = fileUploadDOS.stream().map(FileUploadDO::getUploadId).collect(Collectors.toList());
        Map<Long, ResourceDO> ridMap = resourceRepository.getResourceByUploadIds(uploadIds).stream().collect(Collectors.toMap(ResourceDO::getRid, r -> r));
        return fileUploadDOS.stream().map(fileUploadDO -> VideoUploadInfoDTO.builder()
                .filename(fileUploadDO.getFilename())
                .size(ridMap.containsKey(fileUploadDO.getRid()) ? MediaHelper.getMediaSize(ridMap.get(fileUploadDO.getRid()).getSize().longValue()) + "kb" : null)
                .duration(ridMap.containsKey(fileUploadDO.getRid()) ? MediaHelper.formatMediaDuration(ridMap.get(fileUploadDO.getRid()).getDuration()) : null)
                .uploadId(fileUploadDO.getUploadId())
                .status(UploadStatusEnum.getByValue(fileUploadDO.getStatus().intValue()).getDesc())
                .filmPath(ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getResourceFolderLocation(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.FEATURE_FILM.getValue()), ridMap.get(fileUploadDO.getRid()).getFilename(), ResourceSuffix.FEATURE_FILM) : null)
                .trailerPath(ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getResourceFolderLocation(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.TRAILER.getValue()), ridMap.get(fileUploadDO.getRid()).getFilename(), ResourceSuffix.TRAILER) : null)
                .build()).collect(Lists::newArrayList, List::add, List::addAll);
    }

    private static String getResourceFolderLocation(ResourceTypeEnum type, String remoteFolder, Long rid, String category) {
        String[] arr = StringUtils.isEmpty(category) ?
                new String[]{type.name().toLowerCase(), remoteFolder, rid.toString()} :
                new String[]{type.name().toLowerCase(), remoteFolder, rid.toString(), category};
        return StringUtils.join(arr, '/') + '/';
    }

    private FileInfo upload2CloudStorage(Object file, String filename, String folderLocation, boolean isImage) {
        UploadPretreatment uploadPretreatment = fileStorageService.of(file)
                .setSaveFilename(filename)
                .setPath(folderLocation)
                .setAcl(Constant.ACL.PUBLIC_READ);
        if (isImage) {
            // 生成缩略图
            uploadPretreatment
                    .setSaveThFilename(StringUtils.substringBeforeLast(filename, "."))
                    .setThumbnailSuffix(ResourceSuffix.THUMBNAIL)
                    .thumbnail(th -> th.scale(1f).outputQuality(0.3f));
        }
        return uploadPretreatment.upload();
    }

    private Long persistResource(String filename, ResourceTypeEnum type, String remoteFolder, long sizeInBytes, @Nullable String appName, @Nullable Integer duration) {
        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setFilename(filename);
        resourceDO.setFolder(remoteFolder);
        resourceDO.setType((short) type.getValue());
        resourceDO.setSize(MediaHelper.getMediaSize(sizeInBytes));
        if (StringUtils.isNotEmpty(ClientHolder.getCurrentClient())) {
            resourceDO.setAppName(ClientHolder.getCurrentClient());
        }
        if (duration != null) {
            resourceDO.setDuration(duration);
        }
        return resourceRepository.addResource(resourceDO);
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

    private File saveMultipartFileInDisk(MultipartFile multipartFile) throws IOException {
        File folder = new File(new File(System.getProperty("user.home"), "tmp"), RandomStringUtils.randomAlphanumeric(4));
        FileUtil.mkdir(folder);
        File localFile = new File(folder, multipartFile.getOriginalFilename());
        multipartFile.transferTo(localFile);
        return localFile;
    }

    private Long addUploadRecord(String filename) {
        FileUploadDO fileUpload = new FileUploadDO();
        fileUpload.setFilename(filename);
        if (StringUtils.isNotBlank(ClientHolder.getCurrentClient())) {
            fileUpload.setAppName(ClientHolder.getCurrentClient());
        }
        fileUploadRepository.addFileUpload(fileUpload);
        return fileUpload.getUploadId();
    }
}
