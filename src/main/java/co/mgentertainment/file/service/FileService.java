package co.mgentertainment.file.service;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.model.media.ResourceTypeEnum;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.config.ResourcePathType;
import co.mgentertainment.file.service.dto.*;
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

    void reuploadVideo(Long uploadId, CuttingSetting cuttingSetting);

    void files2CloudStorage(File[] files, ResourceTypeEnum resourceType, String subDirName, Long rid);

    List<VideoUploadInfoDTO> getUploadInfos(List<Long> uploadIds);

    PageResult<VideoUploadInfoDTO> queryFileUpload(QueryUploadConditionDTO condition);

    void uploadLocalFile2Cloud(File file, ResourceTypeEnum resourceType, ResourcePathType pathType, Long rid, String subDirName);

    Map<String, Long> batchAddUploadVideoRecord(List<String> filename, CuttingSetting cuttingSetting);

    Long addUploadVideoRecord(String filename, CuttingSetting cuttingSetting);

    void batchUpdateUploadStatus(List<Long> uploadIds, UploadStatusEnum status);

    void updateUpload(FileUploadDTO fileUploadDTO);

    void updateUploadStatus(Long uploadId, UploadStatusEnum status);

    void updateUploadRid(Long uploadId, UploadStatusEnum status, Long rid);

    Long saveResource(ResourceDTO resourceDTO);

}
