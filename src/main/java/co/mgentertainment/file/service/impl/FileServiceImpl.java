package co.mgentertainment.file.service.impl;

import cn.hutool.core.io.FileTypeUtil;
import cn.xuyanwu.spring.file.storage.FileInfo;
import cn.xuyanwu.spring.file.storage.FileStorageProperties;
import cn.xuyanwu.spring.file.storage.FileStorageService;
import cn.xuyanwu.spring.file.storage.UploadPretreatment;
import cn.xuyanwu.spring.file.storage.constant.Constant;
import cn.xuyanwu.spring.file.storage.spring.SpringFileStorageProperties;
import co.mgentertainment.file.dal.enums.ResourceTypeEnum;
import co.mgentertainment.file.dal.po.ResourceDO;
import co.mgentertainment.file.dal.repository.ResourceRepository;
import co.mgentertainment.file.service.FileService;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author larry
 * @createTime 2023/9/14
 * @description FileServiceImpl
 */
@Service
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
    @Transactional(rollbackFor = Exception.class)
    public Long upload(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String fileType = StringUtils.lowerCase(FileTypeUtil.getType(filename));
        ResourceTypeEnum resourceType = imageTypes.contains(fileType) ?
                ResourceTypeEnum.IMAGE : videoTypes.contains(fileType) ? ResourceTypeEnum.VIDEO :
                packageTypes.contains(fileType) ? ResourceTypeEnum.PACKAGE : ResourceTypeEnum.OTHER;
        Long rid = persistResource(filename, resourceType);
        uploadFile(file, rid, resourceType);
        return rid;
    }

    private FileInfo uploadFile(MultipartFile file, Long rid, ResourceTypeEnum resourceType) {
        UploadPretreatment uploadPretreatment = fileStorageService.of(file)
                .setPath(getFileFolder(resourceType, rid))
                .setAcl(Constant.ACL.PUBLIC_READ);
        if (resourceType == ResourceTypeEnum.IMAGE) {
            uploadPretreatment.thumbnail(th -> th.scale(1f).outputQuality(0.3f));//生成缩略图
        }
        return uploadPretreatment.upload();
    }

    private String getFileFolder(ResourceTypeEnum type, Long rid) {
        return type.name().toLowerCase() + '/' + rid;
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
            s3Client.createBucket(new CreateBucketRequest(bucketName, s3Config.getRegion()));
        } catch (Exception ignored) {
        }
    }


}
