package co.mgentertainment.file.service.impl;

import cn.hutool.core.io.FileUtil;
import co.mgentertainment.common.fileupload.FileInfo;
import co.mgentertainment.common.fileupload.FileStorageProperties;
import co.mgentertainment.common.fileupload.FileStorageService;
import co.mgentertainment.common.fileupload.UploadPretreatment;
import co.mgentertainment.common.fileupload.constant.Constant;
import co.mgentertainment.common.fileupload.spring.SpringFileStorageProperties;
import co.mgentertainment.common.fileupload.tika.ContentTypeDetect;
import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.model.R;
import co.mgentertainment.common.model.media.*;
import co.mgentertainment.common.uidgen.impl.CachedUidGenerator;
import co.mgentertainment.common.utils.DateUtils;
import co.mgentertainment.common.utils.SecurityHelper;
import co.mgentertainment.file.dal.mapper.ResourceExtMapper;
import co.mgentertainment.file.dal.po.*;
import co.mgentertainment.file.dal.repository.AccessClientRepository;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.dal.repository.ResourceRepository;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.MgfsProperties;
import co.mgentertainment.file.service.converter.FileObjectMapper;
import co.mgentertainment.file.service.dto.*;
import co.mgentertainment.file.service.queue.*;
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
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
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
    private final List<String> packageTypes = Lists.newArrayList("apk", "ipa", "hap", "zip", "bzip", "application/x-bzip");

    private Executor uploadExecutor;

    @Resource
    private SpringFileStorageProperties springFileStorageProperties;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private ResourceRepository resourceRepository;

    @Resource
    private AccessClientRepository accessClientRepository;

    @Resource
    private FileUploadRepository fileUploadRepository;

    @Resource
    private ContentTypeDetect contentTypeDetect;

    @Resource
    private MgfsProperties mgfsProperties;

    @Resource
    private CachedUidGenerator cachedUidGenerator;

    @Resource
    private ResourceExtMapper resourceExtMapper;

    @Resource
    private ConvertVideoQueue convertVideoQueue;

    @Resource
    private UploadFilmQueue uploadFilmQueue;

    @Resource
    private CaptureAndUploadCoverQueue captureAndUploadCoverQueue;

    @Resource
    private PrintWatermarkQueue printWatermarkQueue;

    @Resource
    private UploadOriginVideoQueue uploadOriginVideoQueue;

    @Resource
    private CutTrailerQueue cutTrailerQueue;

    @Resource
    private CutShortVideoQueue cutShortVideoQueue;

    @Resource
    private UploadTrailerQueue uploadTrailerQueue;

    @Resource
    private UploadShortVideoQueue uploadShortVideoQueue;

    private AmazonS3 s3Client;

    // 数据初始化
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
        autoAddInnerAccessClient("inner");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadedImageDTO uploadImage(MultipartFile multipartFile) {
        if (getResourceType(multipartFile) != ResourceTypeEnum.IMAGE) {
            throw new IllegalArgumentException("file type is not image");
        }
        Map<Integer, String> map = file2CloudStorage(multipartFile, ResourceTypeEnum.IMAGE);
        return UploadedImageDTO.builder().filename(multipartFile.getOriginalFilename()).imagePath(map.get(ResourceFileType.IMAGE)).thumbnailPath(map.get(ResourceFileType.THUMBNAIL)).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadedFileDTO uploadFile(MultipartFile multipartFile) {
        ResourceTypeEnum resourceType = getResourceType(multipartFile);
        if (resourceType == ResourceTypeEnum.IMAGE || resourceType == ResourceTypeEnum.VIDEO) {
            throw new IllegalArgumentException("wrong file type");
        }
        Map<Integer, String> map = file2CloudStorage(multipartFile, resourceType);
        return UploadedFileDTO.builder().filename(multipartFile.getOriginalFilename()).remotePath(map.get(ResourceFileType.DEFAULT)).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VideoUploadInfoDTO uploadVideo(MultipartFile multipartFile, CuttingSetting cuttingSetting) {
        long size = multipartFile.getSize();
        if (getResourceType(multipartFile) != ResourceTypeEnum.VIDEO) {
            throw new IllegalArgumentException("file type is not video");
        }
        // 过滤文件名非法字符
        String filename = MediaHelper.filterInvalidFilenameChars(multipartFile.getOriginalFilename());
        log.debug("(1)添加上传记录:{}", filename);
        Long uploadId = this.addUploadVideoRecord(filename, cuttingSetting, Optional.empty());
        File file;
        try {
            file = saveMultipartFileInDisk(multipartFile, filename, uploadId);
        } catch (IOException e) {
            throw new RuntimeException("fail to persist file", e);
        }
        log.debug("(1)已上传记录:{} uploadId:{}", filename, uploadId);
        convertVideoQueue.put(ConvertVideoParameter.builder()
                .uploadId(uploadId)
                .originVideoPath(file.getAbsolutePath())
                .appCode(ClientHolder.getCurrentClient())
                .build());
        return VideoUploadInfoDTO.builder().uploadId(uploadId).filename(filename).size(MediaHelper.getMediaSize(size) + "kb").status(UploadStatusEnum.CONVERTING.getDesc()).statusCode(UploadStatusEnum.CONVERTING.getValue()).uploadStartTime(new Date()).build();
    }

    @Override
    public void reuploadVideo(Long uploadId, CuttingSetting cuttingSetting) {
        FileUploadDO fileUploadDO = fileUploadRepository.getFileUploadByUploadId(uploadId);
        File originVideoDir = MediaHelper.getUploadIdDir(uploadId, MgfsPath.MgfsPathType.MAIN);
        File originVideo = new File(originVideoDir, fileUploadDO.getFilename());
        if (fileUploadDO == null) {
            if (originVideoDir.exists()) {
                FileUtil.del(originVideoDir);
            }
            throw new IllegalArgumentException("uploadId not exists");
        }
        if (!originVideo.exists()) {
            // 多实例下本地磁盘找不到则忽略
            return;
        }
        UploadStatusEnum oldStatus = UploadStatusEnum.getByValue(fileUploadDO.getStatus().intValue());
        File filmVideo = MediaHelper.getProcessedFileByOriginFile(originVideo, VideoType.FEATURE_FILM.getValue(), ResourceSuffix.FEATURE_FILM);
        Long rid = fileUploadDO.getRid();
        switch (oldStatus) {
            case CONVERT_FAILURE:
                convertVideoQueue.put(ConvertVideoParameter.builder()
                        .uploadId(uploadId)
                        .originVideoPath(originVideo.getAbsolutePath())
                        .appCode(ClientHolder.getCurrentClient())
                        .build());
                break;
            case UPLOAD_FAILURE:
                if (rid == null || resourceRepository.getResourceByRid(rid) == null) {
                    uploadFilmQueue.put(UploadFilmParameter.builder()
                            .uploadId(uploadId)
                            .originVideoPath(originVideo.getAbsolutePath())
                            .processedVideoPath(filmVideo.getAbsolutePath())
                            .appCode(ClientHolder.getCurrentClient())
                            .build());
                }
                break;
            case CAPTURE_FAILURE:
            case UPLOAD_COVER_FAILURE:
                if (resourceRepository.getResourceByRid(rid) == null) {
                    break;
                }
                captureAndUploadCoverQueue.put(CaptureAndUploadCoverParameter.builder()
                        .uploadId(uploadId)
                        .originVideoPath(originVideo.getAbsolutePath())
                        .build());
                break;
            default:
                break;
        }
        UploadSubStatusEnum oldSubStatus = UploadSubStatusEnum.getByValue(fileUploadDO.getSubStatus().intValue());
        switch (oldSubStatus) {
            case PRINT_FAILURE:
                printWatermarkQueue.put(PrintWatermarkParameter.builder().uploadId(uploadId).build());
                break;
            case UPLOAD_ORIGIN_FAILURE:
                uploadOriginVideoQueue.put(UploadOriginVideoParameter.builder().uploadId(uploadId).build());
                break;
            case CUT_TRAILER_FAILURE:
                cutTrailerQueue.put(CutTrailerParameter.builder().uploadId(uploadId).build());
                break;
            case UPLOAD_TRAILER_FAILURE:
                uploadTrailerQueue.put(UploadTrailerParameter.builder().uploadId(uploadId).build());
                break;
            case CUT_SHORT_FAILURE:
                cutShortVideoQueue.put(CutShortVideoParameter.builder().uploadId(uploadId).build());
                break;
            case UPLOAD_SHORT_FAILURE:
                uploadShortVideoQueue.put(UploadShortVideoParameter.builder().uploadId(uploadId).build());
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
        if (StringUtils.isNotBlank(condition.getAppCode())) {
            criteria.andAppCodeEqualTo(condition.getAppCode());
        } else {
            criteria.andAppCodeEqualTo(SERVER_INNER_APP_CODE);
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
        if (null != condition.getSubStatus()) {
            criteria.andSubStatusEqualTo(condition.getSubStatus().shortValue());
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
    public void uploadLocalFile2Cloud(File file, ResourceTypeEnum resourceType, String subDirName, Long rid, ResourcePathType pathType) {
        Preconditions.checkArgument(file != null && pathType != null, "video or pathType should not be null");
        String filename = file.getName();
        String cloudPath = getCloudPath(resourceType, subDirName, rid, pathType.getValue());
        log.debug("文件名:{}, 云存储路径:{}, 上传资源目录:{}", pathType.getValue(), cloudPath, filename);
        upload2CloudStorage(file, cloudPath, filename, ResourcePathType.COVER.equals(pathType));
    }

    @Override
    public Map<String, Long> batchAddUploadVideoRecord(List<String> filenames, CuttingSetting cuttingSetting) {
        return fileUploadRepository.batchAddFileUpload(filenames, ResourceTypeEnum.VIDEO, ClientHolder.getCurrentClient(), cuttingSetting.getTrailerDuration(), cuttingSetting.getTrailerStartFromProportion());
    }

    @Override
    public Long addUploadVideoRecord(String filename, CuttingSetting cuttingSetting, Optional<String> appCode) {
        FileUploadDO fu = new FileUploadDO();
        fu.setFilename(filename);
        fu.setAppCode(appCode.isPresent() ? appCode.get() : ClientHolder.getCurrentClient());
        boolean hasTrailer = cuttingSetting != null && cuttingSetting.getTrailerDuration() != null && cuttingSetting.getTrailerStartFromProportion() != null;
        boolean hasShort = cuttingSetting != null && cuttingSetting.getShortVideoDuration() != null && cuttingSetting.getShortVideoStartFromProportion() != null;
        fu.setHasTrailer(hasTrailer ? (byte) 1 : (byte) 0);
        fu.setHasShort(hasShort ? (byte) 1 : (byte) 0);
        if (cuttingSetting.getTrailerDuration() != null) {
            fu.setTrailerDuration(cuttingSetting.getTrailerDuration());
        }
        if (cuttingSetting.getShortVideoDuration() != null) {
            fu.setShortDuration(cuttingSetting.getShortVideoDuration());
        }
        if (cuttingSetting.getTrailerStartFromProportion() != null) {
            fu.setTrailerStartPos(cuttingSetting.getTrailerStartFromProportion());
        }
        if (cuttingSetting.getShortVideoStartFromProportion() != null) {
            fu.setShortStartPos(cuttingSetting.getShortVideoStartFromProportion());
        }
        if (cuttingSetting.getAutoCaptureCover()) {
            fu.setHasCover((byte) 1);
        }
        return fileUploadRepository.addFileUpload(fu);
    }

    @Override
    public void batchUpdateUploadStatus(List<Long> uploadIds, UploadStatusEnum status) {
        fileUploadRepository.batchUpdateUploadStatus(uploadIds, status);
    }

    @Override
    public void updateUpload(FileUploadDTO fileUploadDTO) {
        FileUploadDO fileUploadDO = FileObjectMapper.INSTANCE.toFileUploadDO(fileUploadDTO);
        fileUploadRepository.updateFileUploadByPrimaryKey(fileUploadDO);
    }

    @Override
    public void updateUploadStatus(Long uploadId, UploadStatusEnum status) {
        if (uploadId == null || status == null) {
            return;
        }
        FileUploadDO fileUploadDO = new FileUploadDO();
        fileUploadDO.setUploadId(uploadId);
        fileUploadDO.setStatus(status.getValue().shortValue());
        fileUploadRepository.updateFileUploadByPrimaryKey(fileUploadDO);
    }

    @Override
    public void updateSubStatus(Long uploadId, UploadSubStatusEnum subStatus) {
        if (uploadId == null || subStatus == null) {
            return;
        }
        FileUploadDO fileUploadDO = new FileUploadDO();
        fileUploadDO.setUploadId(uploadId);
        fileUploadDO.setSubStatus(subStatus.getValue().shortValue());
        fileUploadRepository.updateFileUploadByPrimaryKey(fileUploadDO);
    }

    @Override
    public void updateUploadStatusAndRid(Long uploadId, UploadStatusEnum status, Long rid) {
        if (uploadId == null || rid == null) {
            log.error("Error to call updateUploadRid, uploadId or rid is null");
            return;
        }
        FileUploadDO fileUploadDO = new FileUploadDO();
        fileUploadDO.setUploadId(uploadId);
        fileUploadDO.setRid(rid);
        if (status != null) {
            fileUploadDO.setStatus(status.getValue().shortValue());
        }
        fileUploadRepository.updateFileUploadByPrimaryKey(fileUploadDO);
    }

    @Override
    public void updateUploadRid(Long uploadId, Long rid) {
        if (uploadId == null || rid == null) {
            log.error("Error to call updateUploadRid, uploadId or rid is null");
            return;
        }
        FileUploadDO fileUploadDO = new FileUploadDO();
        fileUploadDO.setUploadId(uploadId);
        fileUploadDO.setRid(rid);
        fileUploadRepository.updateFileUploadByPrimaryKey(fileUploadDO);
    }

    @Override
    public void updateStatus(Long uploadId, UploadStatusEnum status, UploadSubStatusEnum subStatus) {
        if (uploadId == null || status == null || subStatus == null) {
            log.error("Error to call updateStatus, uploadId or status or subStatus is null");
            return;
        }
        FileUploadDO fileUploadDO = new FileUploadDO();
        fileUploadDO.setUploadId(uploadId);
        fileUploadDO.setStatus(status.getValue().shortValue());
        fileUploadDO.setSubStatus(subStatus.getValue().shortValue());
        fileUploadRepository.updateFileUploadByPrimaryKey(fileUploadDO);
    }

    @Override
    public Long saveResource(ResourceDTO resourceDTO) {
        ResourceDO resourceDO = FileObjectMapper.INSTANCE.toResourceDO(resourceDTO);
        if (StringUtils.isNotEmpty(ClientHolder.getCurrentClient())) {
            resourceDO.setAppCode(ClientHolder.getCurrentClient());
        }
        return resourceRepository.saveResource(resourceDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void afterMainProcessComplete(Long uploadId, File originVideo) {
        if (!FileUtil.exist(originVideo)) {
            originVideo = getMainOriginFile(uploadId);
        }
        // 移动原视频
        File newOriginFile = MediaHelper.moveFileToUploadDir(originVideo, uploadId, MgfsPath.MgfsPathType.VICE);
        // 删除已处理目录
        MediaHelper.deleteCompletedVideoFolder(uploadId, MgfsPath.MgfsPathType.MAIN);
        // 入水印处理队列
        printWatermarkQueue.put(PrintWatermarkParameter.builder()
                .uploadId(uploadId)
                .newOriginVideoPath(newOriginFile.getAbsolutePath())
                .build());
        // 更新状态
        updateStatus(uploadId, UploadStatusEnum.COMPLETED, UploadSubStatusEnum.PRINTING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void afterViceProcessComplete(Long uploadId) {
        // 删除已处理目录
        MediaHelper.deleteCompletedVideoFolder(uploadId, MgfsPath.MgfsPathType.VICE);
        // 更新状态
        updateSubStatus(uploadId, UploadSubStatusEnum.END);
    }

    @Override
    public File getMainOriginFile(Long uploadId) {
        return getOriginFile(uploadId, MgfsPath.MgfsPathType.MAIN);
    }

    @Override
    public File getViceOriginFile(Long uploadId) {
        return getOriginFile(uploadId, MgfsPath.MgfsPathType.VICE);
    }

    @Override
    public File getWatermarkFile(Long uploadId) {
        FileUploadDO fileUploadDO = fileUploadRepository.getFileUploadByUploadId(uploadId);
        if (fileUploadDO == null || fileUploadDO.getUploadId() == null) {
            log.error("uploadId:{} not exists", uploadId);
            return null;
        }
        File uploadIdDir = MediaHelper.getUploadIdDir(uploadId, MgfsPath.MgfsPathType.VICE);
        File watermarkDir = new File(uploadIdDir, ResourcePathType.ORIGIN.getValue());
        return new File(watermarkDir, StringUtils.substringBeforeLast(fileUploadDO.getFilename(), ".") + ResourceSuffix.ORIGIN_FILM);
    }

    @Override
    public File getConvertedFilmDir(Long uploadId) {
        File uploadIdDir = getOriginFile(uploadId, MgfsPath.MgfsPathType.MAIN);
        return new File(uploadIdDir, ResourcePathType.FEATURE_FILM.getValue());
    }

    @Override
    public File getTrailerFile(Long uploadId) {
        return getResourceFile(uploadId, ResourcePathType.TRAILER);
    }

    @Override
    public File getShortVideoFile(Long uploadId) {
        return getResourceFile(uploadId, ResourcePathType.SHORT);
    }

    @Override
    public UploadResourceDTO getUploadResource(Long uploadId) {
        ResourceExtDO resourceExtDO = resourceExtMapper.selectByUploadId(uploadId);
        return FileObjectMapper.INSTANCE.toUploadResourceDTO(resourceExtDO);
    }

    @Override
    public R<Void> startInnerUploads(File innerDirToUpload) {
        if (!FileUtil.exist(innerDirToUpload) || FileUtil.isDirEmpty(innerDirToUpload)) {
            return R.failed("内部服务器目录不存在或没有文件");
        }
        File[] files = innerDirToUpload.listFiles();
        for (File file : files) {
            // 过滤文件名非法字符
            String filename = MediaHelper.filterInvalidFilenameChars(file.getName());
            Long uploadId = addUploadVideoRecord(filename, CuttingSetting.builder()
                            .trailerDuration(30)
                            .trailerStartFromProportion(0)
                            .autoCaptureCover(true)
                            .build(),
                    Optional.of(FileService.SERVER_INNER_APP_CODE));
            File newOriginFile = MediaHelper.moveFileToUploadDir(file, uploadId, MgfsPath.MgfsPathType.MAIN);
            convertVideoQueue.put(ConvertVideoParameter.builder()
                    .uploadId(uploadId)
                    .originVideoPath(newOriginFile.getAbsolutePath())
                    .appCode(ClientHolder.getCurrentClient())
                    .build());
        }
        return R.ok();
    }

    private File getOriginFile(Long uploadId, MgfsPath.MgfsPathType pathType) {
        FileUploadDO fileUploadDO = fileUploadRepository.getFileUploadByUploadId(uploadId);
        if (fileUploadDO == null || fileUploadDO.getUploadId() == null) {
            log.error("uploadId:{} not exists", uploadId);
            return null;
        }
        File uploadIdDir = MediaHelper.getUploadIdDir(uploadId, pathType);
        return new File(uploadIdDir, fileUploadDO.getFilename());
    }

    private File getResourceFile(Long uploadId, ResourcePathType resourcePathType) {
        FileUploadDO fileUploadDO = fileUploadRepository.getFileUploadByUploadId(uploadId);
        if (fileUploadDO == null || fileUploadDO.getUploadId() == null) {
            log.error("uploadId:{} not exists", uploadId);
            return null;
        }
        File uploadIdDir = MediaHelper.getUploadIdDir(uploadId, MgfsPath.MgfsPathType.VICE);
        File watermarkDir = new File(uploadIdDir, ResourcePathType.ORIGIN.getValue());
        File targetDir = new File(watermarkDir, resourcePathType.getValue());
        String suffix = resourcePathType == ResourcePathType.FEATURE_FILM ? ResourceSuffix.FEATURE_FILM :
                resourcePathType == ResourcePathType.ORIGIN ? ResourceSuffix.ORIGIN_FILM :
                        resourcePathType == ResourcePathType.TRAILER ? ResourceSuffix.TRAILER :
                                resourcePathType == ResourcePathType.SHORT ? ResourceSuffix.SHORT :
                                        resourcePathType == ResourcePathType.COVER ? ResourceSuffix.SCREENSHOT : ResourceSuffix.ORIGIN_FILM;
        return new File(targetDir, StringUtils.substringBeforeLast(fileUploadDO.getFilename(), ".") + suffix);
    }

    private Map<Integer, String> file2CloudStorage(MultipartFile multipartFile, ResourceTypeEnum resourceType) {
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
                .appCode(ClientHolder.getCurrentClient())
                .build();
        this.saveResource(resourceDTO);
        Map<Integer, String> pathMap = new HashMap<>(0);
        if (isImage) {
            pathMap.put(ResourceFileType.IMAGE, retrieveResourcePath(cloudPath, filename, null));
            pathMap.put(ResourceFileType.THUMBNAIL, retrieveResourcePath(cloudPath, filename, springFileStorageProperties.getThumbnailSuffix()));
        } else {
            pathMap.put(ResourceFileType.DEFAULT, retrieveResourcePath(cloudPath, filename, null));
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
        return imageTypes.stream().anyMatch(finalFileType::contains) ?
                ResourceTypeEnum.IMAGE : mgfsProperties.getSupportVideoFormat().stream().anyMatch(finalFileType::contains) ? ResourceTypeEnum.VIDEO :
                packageTypes.stream().anyMatch(finalFileType::contains) ? ResourceTypeEnum.PACKAGE : ResourceTypeEnum.OTHER;
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
                .duration(ridMap.containsKey(fileUploadDO.getRid()) ? ridMap.get(fileUploadDO.getRid()).getDuration() : null)
                .durationStr(ridMap.containsKey(fileUploadDO.getRid()) ? MediaHelper.formatMediaDuration(ridMap.get(fileUploadDO.getRid()).getDuration()) : null)
                .uploadId(fileUploadDO.getUploadId())
                .status(UploadStatusEnum.getByValue(fileUploadDO.getStatus().intValue()).getDesc())
                .statusCode(UploadStatusEnum.getByValue(fileUploadDO.getStatus().intValue()).getValue())
                .subStatus(UploadSubStatusEnum.getByValue(fileUploadDO.getSubStatus().intValue()).getDesc())
                .subStatusCode(UploadSubStatusEnum.getByValue(fileUploadDO.getSubStatus().intValue()).getValue())
                .originPath(ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getCloudPath(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.ORIGIN.getValue()), ridMap.get(fileUploadDO.getRid()).getFilename(), ResourceSuffix.ORIGIN_FILM) : null)
                .filmPath(ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getCloudPath(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.FEATURE_FILM.getValue()), ridMap.get(fileUploadDO.getRid()).getFilename(), ResourceSuffix.FEATURE_FILM) : null)
                .trailerPath(fileUploadDO.getHasTrailer() == (byte) 0 ? null : ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getCloudPath(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.TRAILER.getValue()), ridMap.get(fileUploadDO.getRid()).getFilename(), ResourceSuffix.TRAILER) : null)
                .shortPath(fileUploadDO.getHasShort() == (byte) 0 ? null : ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getCloudPath(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.SHORT.getValue()), ridMap.get(fileUploadDO.getRid()).getFilename(), ResourceSuffix.SHORT) : null)
                .screenshotPath(fileUploadDO.getHasCover() == (byte) 0 ? null : ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getCloudPath(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.COVER.getValue()), StringUtils.EMPTY, ResourceSuffix.SCREENSHOT) : null)
                .screenshotThumbnailPath(fileUploadDO.getHasCover() == (byte) 0 ? null : ridMap.containsKey(fileUploadDO.getRid()) ?
                        retrieveResourcePath(getCloudPath(ResourceTypeEnum.VIDEO, ridMap.get(fileUploadDO.getRid()).getFolder(), fileUploadDO.getRid(), ResourcePathType.COVER.getValue()), StringUtils.EMPTY, ResourceSuffix.SCREENSHOT_THUMBNAIL) : null)
                .uploadStartTime(fileUploadDO.getCreateTime())
                .statusUpdateTime(fileUploadDO.getUpdatedTime())
                .build()).collect(Lists::newArrayList, List::add, List::addAll);
    }

    private static String getCloudPath(ResourceTypeEnum type, String subDirName, Long rid, String category) {
        String[] arr = StringUtils.isEmpty(category) ?
                new String[]{type.name().toLowerCase(), subDirName, String.valueOf(rid)} :
                new String[]{type.name().toLowerCase(), subDirName, String.valueOf(rid), category};
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

    private void autoAddInnerAccessClient(String appName) {
        AccessClientDO accessClientDO = new AccessClientDO();
        accessClientDO.setAppCode(SERVER_INNER_APP_CODE);
        accessClientDO.setAppName(appName);
        accessClientDO.setEncryptAlgorithm(MgfsProperties.AlgorithmType.RSA.name());
        try {
            accessClientRepository.saveAccessClient(accessClientDO);
        } catch (Exception ignored) {
        }
    }

    private File saveMultipartFileInDisk(MultipartFile multipartFile, String newFilename, Long uploadId) throws IOException {
        File folder = MediaHelper.getUploadIdDir(uploadId, MgfsPath.MgfsPathType.MAIN);
        FileUtil.mkdir(folder);
        File localFile = new File(folder, newFilename);
        FileCopyUtils.copy(multipartFile.getInputStream(), Files.newOutputStream(localFile.toPath()));
        return localFile;
    }

    private static class ResourceFileType {
        private static Integer DEFAULT = 0;
        private static Integer IMAGE = 1;
        private static Integer THUMBNAIL = 2;
    }
}
