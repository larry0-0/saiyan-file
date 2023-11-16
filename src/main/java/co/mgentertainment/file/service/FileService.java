package co.mgentertainment.file.service;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.model.media.ResourcePathType;
import co.mgentertainment.common.model.media.ResourceTypeEnum;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.model.media.UploadSubStatusEnum;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author larry
 * @createTime 2023/9/14
 * @description FileService
 */
public interface FileService {
    String SERVER_INNER_APP_CODE = "888888";

    UploadedImageDTO uploadImage(MultipartFile file);

    UploadedFileDTO uploadFile(MultipartFile file);

    VideoUploadInfoDTO addVideoUploadRecord(MultipartFile file, CuttingSetting cuttingSetting);

    void files2CloudStorage(File[] files, ResourceTypeEnum resourceType, String subDirName, Long rid, boolean canRetry);

    List<VideoUploadInfoDTO> getUploadInfos(List<Long> uploadIds);

    PageResult<VideoUploadInfoDTO> queryFileUpload(QueryUploadConditionDTO condition);

    void uploadLocalFile2Cloud(File file, ResourceTypeEnum resourceType, String subDirName, Long rid, ResourcePathType pathType);

    Map<String, Long> batchAddUploadVideoRecord(List<String> filename, CuttingSetting cuttingSetting);

    Long addUploadVideoRecord(String title, CuttingSetting cuttingSetting, Optional<String> appCode);

    void batchUpdateUploadStatus(List<Long> uploadIds, UploadStatusEnum status);

    void updateUpload(FileUploadDTO fileUploadDTO);

    void updateUploadStatus(Long uploadId, UploadStatusEnum status);

    void updateSubStatus(Long uploadId, UploadSubStatusEnum subStatus);

    void updateUploadStatusAndRid(Long uploadId, UploadStatusEnum status, Long rid);

    void updateUploadRid(Long uploadId, Long rid);

    void updateStatus(Long uploadId, UploadStatusEnum status, UploadSubStatusEnum subStatus);

    Long saveResource(ResourceDTO resourceDTO);

    File getMainOriginFile(Long uploadId);

    File getViceOriginFile(Long uploadId);

    FileUploadDO getUploadRecord(Long uploadId);

    File getOriginFile(FileUploadDO fileUploadDO);

    File getConvertedFilmDir(Long uploadId);

    File getWatermarkFile(Long uploadId);

    File getTrailerFile(Long uploadId);

    File getShortVideoFile(Long uploadId);

    UploadResourceDTO getUploadResource(Long uploadId);

    boolean existsRid(Long rid);
}
