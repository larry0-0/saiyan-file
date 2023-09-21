package co.mgentertainment.file.service;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.file.dal.enums.ResourceTypeEnum;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.ResourcePathType;
import co.mgentertainment.file.service.dto.QueryUploadConditionDTO;
import co.mgentertainment.file.service.dto.UploadedFileDTO;
import co.mgentertainment.file.service.dto.UploadedImageDTO;
import co.mgentertainment.file.service.dto.VideoUploadInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author larry
 * @createTime 2023/9/14
 * @description FileService
 */
public interface FileService {

    UploadedImageDTO uploadImage(MultipartFile file);

    UploadedFileDTO uploadFile(MultipartFile file);

    VideoUploadInfoDTO uploadVideo(MultipartFile file, CuttingSetting cuttingSetting);

    Map<ResourcePathType, String> file2CloudStorage(MultipartFile multipartFile, ResourceTypeEnum resourceType);

    Long media2CloudStorage(File media, ResourceTypeEnum resourceType, String appName);

    List<VideoUploadInfoDTO> getUploadInfos(List<Long> uploadIds);

    PageResult<VideoUploadInfoDTO> queryFileUpload(QueryUploadConditionDTO condition);

    void uploadLocalTrailUnderResource(Long rid, File localFile);
}
