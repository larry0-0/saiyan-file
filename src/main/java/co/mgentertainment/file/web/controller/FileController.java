package co.mgentertainment.file.web.controller;

import co.mgentertainment.common.model.R;
import co.mgentertainment.file.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

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
    public R<Long> upload(@RequestPart(value = "file") MultipartFile file) {
        return R.ok(fileService.upload(file));
    }

    @PostMapping("/batchUpload")
    public R<Map<String, Long>> batchUpload(@NotNull @RequestParam("files") MultipartFile[] files) {
        Map<String, Long> map = new HashMap<>();
        for (MultipartFile file : files) {
            R<Long> res = upload(file);
            map.put(file.getOriginalFilename(), res.getData());
        }
        return R.ok(map);
    }

}
