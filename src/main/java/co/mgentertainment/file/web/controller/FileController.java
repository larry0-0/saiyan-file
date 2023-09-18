package co.mgentertainment.file.web.controller;

import co.mgentertainment.common.model.R;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.dto.FileUploadInfoDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author larry
 * @createTime 2023/9/8
 * @description UserController
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    public R<FileUploadInfoDTO> upload(@RequestPart(value = "file") MultipartFile file) {
        return R.ok(fileService.upload(file));
    }

    @PostMapping("/batchUpload")
    public R<List<FileUploadInfoDTO>> batchUpload(@NotNull @RequestParam("files") MultipartFile[] files) {
        List<FileUploadInfoDTO> list = new ArrayList<>();
        for (MultipartFile file : files) {
            R<FileUploadInfoDTO> res = upload(file);
            list.add(res.getData());
        }
        return R.ok(list);
    }

    @PostMapping("/queryUploadInfo")
    public R<List<FileUploadInfoDTO>> queryUploadInfo(@RequestBody List<Long> uploadIds) {
        return R.ok(fileService.getUploadInfos(uploadIds));
    }

}
