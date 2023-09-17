package co.mgentertainment.file.service.impl;

import cn.xuyanwu.spring.file.storage.FileInfo;
import cn.xuyanwu.spring.file.storage.FileStorageProperties;
import cn.xuyanwu.spring.file.storage.FileStorageService;
import cn.xuyanwu.spring.file.storage.UploadPretreatment;
import cn.xuyanwu.spring.file.storage.constant.Constant;
import cn.xuyanwu.spring.file.storage.spring.SpringFileStorageProperties;
import cn.xuyanwu.spring.file.storage.tika.ContentTypeDetect;
import co.mgentertainment.common.utils.SecurityHelper;
import co.mgentertainment.file.dal.enums.ResourceTypeEnum;
import co.mgentertainment.file.dal.enums.UploadStatusEnum;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import co.mgentertainment.file.dal.po.ResourceDO;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.dal.repository.ResourceRepository;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.config.MgfsProperties;
import co.mgentertainment.file.service.dto.FileUploadInfoDTO;
import co.mgentertainment.file.service.event.VideoConvertEvent;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

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
    }

    @Override
    public FileUploadInfoDTO upload(MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String fileType;
        try {
            fileType = contentTypeDetect.detect(multipartFile.getInputStream(), filename);
        } catch (IOException e) {
            fileType = StringUtils.lowerCase(StringUtils.substringAfterLast(filename, "."));
        }
        String finalFileType = fileType;
        ResourceTypeEnum resourceType = imageTypes.stream().anyMatch(type -> finalFileType.contains(type)) ?
                ResourceTypeEnum.IMAGE : videoTypes.stream().anyMatch(type -> finalFileType.contains(type)) ? ResourceTypeEnum.VIDEO :
                packageTypes.stream().anyMatch(type -> finalFileType.contains(type)) ? ResourceTypeEnum.PACKAGE : ResourceTypeEnum.OTHER;
        if (resourceType == ResourceTypeEnum.VIDEO) {
            File file = saveMultipartFileInDisk(multipartFile);
            FileUploadDO fileUpload = new FileUploadDO();
            fileUpload.setFilename(filename);
            fileUploadRepository.addFileUpload(fileUpload);
            Long uploadId = fileUpload.getUploadId();
            eventBus.post(VideoConvertEvent.builder().videoFilePath(file.getAbsolutePath()).uploadId(uploadId).build());
            return FileUploadInfoDTO.builder().fileName(filename).uploadId(uploadId).statusEnum(UploadStatusEnum.TO_CONVERT).build();
        }
        String encryptResourceAddress = file2CloudStorage(multipartFile, resourceType);
        return FileUploadInfoDTO.builder().fileName(filename).encryptResourcePath(encryptResourceAddress).statusEnum(UploadStatusEnum.COMPLETED).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String file2CloudStorage(MultipartFile multipartFile, ResourceTypeEnum resourceType) {
        String filename = multipartFile.getOriginalFilename();
        Long rid = persistResource(filename, resourceType);
        String folderLocation = getResourceFolderLocation(resourceType, rid);
        update2CloudStorage(multipartFile, filename, folderLocation, resourceType);
        return encryptResourcePath(rid, filename, resourceType);
    }

    private String encryptResourcePath(Long rid, String filename, ResourceTypeEnum resourceType) {
        String folderLocation = getResourceFolderLocation(resourceType, rid);
        String resourcePath = getResourcePath(folderLocation, filename);
        return SecurityHelper.hyperEncrypt(resourcePath, mgfsProperties.getSecret());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void folder2CloudStorage(File folder, ResourceTypeEnum resourceType) {
        if (folder == null || folder.isFile()) {
            throw new IllegalArgumentException("folder is not a directory");
        }
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        Long rid = persistResource(folder.getName(), resourceType);
        String folderLocation = getResourceFolderLocation(resourceType, rid);
        for (File file : files) {
            update2CloudStorage(file, file.getName(), folderLocation, resourceType);
        }
    }

    @Override
    public List<FileUploadInfoDTO> getUploadInfos(List<Long> uploadIds) {
        FileUploadExample example = new FileUploadExample();
        example.createCriteria().andDeletedEqualTo((byte) 0).andUploadIdIn(uploadIds);
        List<FileUploadDO> fileUploadDOS = fileUploadRepository.getFileUploadsByExample(example);
        if (CollectionUtils.isEmpty(fileUploadDOS)) {
            return Lists.newArrayList();
        }
        return fileUploadDOS.stream().map(fileUploadDO -> FileUploadInfoDTO.builder()
                .fileName(fileUploadDO.getFilename())
                .uploadId(fileUploadDO.getUploadId())
                .statusEnum(UploadStatusEnum.getByValue(fileUploadDO.getStatus().intValue()))
                .encryptResourcePath(UploadStatusEnum.getByValue(fileUploadDO.getStatus().intValue()) == UploadStatusEnum.COMPLETED ? encryptResourcePath(fileUploadDO.getRid(), fileUploadDO.getFilename(), ResourceTypeEnum.getByValue(fileUploadDO.getType().intValue())) : null)
                .build()).collect(Lists::newArrayList, List::add, List::addAll);
    }

    private String getResourceFolderLocation(ResourceTypeEnum type, Long rid) {
        return type.name().toLowerCase() + '/' + rid + '/';
    }

    private FileInfo update2CloudStorage(Object file, String filename, String folderLocation, ResourceTypeEnum resourceType) {
        UploadPretreatment uploadPretreatment = fileStorageService.of(file)
                .setSaveFilename(filename)
                .setPath(folderLocation)
                .setAcl(Constant.ACL.PUBLIC_READ);
        if (resourceType == ResourceTypeEnum.IMAGE) {
            uploadPretreatment.thumbnail(th -> th.scale(1f).outputQuality(0.3f));//生成缩略图
        }
        return uploadPretreatment.upload();
    }

    private Long persistResource(String filename, ResourceTypeEnum type) {
        ResourceDO resourceDO = new ResourceDO();
        resourceDO.setFilename(filename);
        resourceDO.setFolder(type.name().toLowerCase());
        resourceDO.setType((short) type.getValue());
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

    private File saveMultipartFileInDisk(MultipartFile multipartFile) {
        File localFile = new File(System.getProperty("user.home") + File.separator + "tmp" + File.separator + multipartFile.getOriginalFilename());
        try (OutputStream os = new FileOutputStream(localFile)) {
            os.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("fail to save multipartFile in disk", e);
        }
        return localFile;
    }

    private String getResourcePath(String folderLocation, String filename) {
        return '/' + folderLocation + filename;
    }
}
