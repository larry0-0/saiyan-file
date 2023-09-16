package co.mgentertainment.file.service;

import co.mgentertainment.file.dal.enums.ResourceTypeEnum;
import co.mgentertainment.file.service.dto.FileUploadInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * @author larry
 * @createTime 2023/9/14
 * @description FileService
 */
public interface FileService {

    FileUploadInfoDTO upload(MultipartFile file);

    String file2CloudStorage(MultipartFile multipartFile, ResourceTypeEnum resourceType);

    void folder2CloudStorage(File folder, ResourceTypeEnum resourceType);

    List<FileUploadInfoDTO> getUploadInfos(List<Long> uploadIds);
}
