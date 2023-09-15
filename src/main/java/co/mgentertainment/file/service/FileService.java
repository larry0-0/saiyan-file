package co.mgentertainment.file.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author larry
 * @createTime 2023/9/14
 * @description FileService
 */
public interface FileService {

    Long upload(MultipartFile file);
}
