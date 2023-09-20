package co.mgentertainment.file.web.controller;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.model.R;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.dto.UploadedImageDTO;
import co.mgentertainment.file.service.dto.VideoUploadInfoDTO;
import co.mgentertainment.file.service.dto.QueryUploadConditionDTO;
import co.mgentertainment.file.service.dto.ResourceLineDTO;
import co.mgentertainment.file.service.impl.ResourceLineService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author larry
 * @createTime 2023/9/8
 * @description FileController
 */
@RestController
@RequestMapping("/file")
@Api(tags = "文件服务")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final ResourceLineService resourceLineService;

    @PostMapping("/upload/image")
    @Operation(summary = "图片上传")
    public R<UploadedImageDTO> uploadImage(@RequestParam(value = "file") MultipartFile file) {
        return R.ok(fileService.uploadImage(file));
    }

    @PostMapping("/upload/video")
    @Operation(summary = "视频上传")
    public R<VideoUploadInfoDTO> uploadVideo(@RequestParam(value = "file") MultipartFile file, CuttingSetting cuttingSetting) {
        return R.ok(fileService.uploadVideo(file, cuttingSetting));
    }

    @PostMapping("/upload/videos")
    @Operation(summary = "批量视频上传")
    public R<List<VideoUploadInfoDTO>> batchUploadVideo(@NotNull @RequestParam("files") MultipartFile[] files, CuttingSetting cuttingSetting) {
        List<VideoUploadInfoDTO> list = new ArrayList<>();
        for (MultipartFile file : files) {
            R<VideoUploadInfoDTO> res = uploadVideo(file, cuttingSetting);
            list.add(res.getData());
        }
        return R.ok(list);
    }

    @PostMapping("/listUploadProgress")
    @Operation(summary = "根据uploadId获取视频上传进展")
    public R<List<VideoUploadInfoDTO>> listUploadInfo(@RequestBody List<Long> uploadIds) {
        return R.ok(fileService.getUploadInfos(uploadIds));
    }

    @PostMapping("/queryUploadProgress")
    @Operation(summary = "查询视频上传进展")
    public R<PageResult<VideoUploadInfoDTO>> queryUploads(@RequestBody QueryUploadConditionDTO condition) {
        return R.ok(fileService.queryFileUpload(condition));
    }

    @PostMapping("/lines")
    @Operation(summary = "查询媒资线路")
    public R<List<ResourceLineDTO>> getResourceLines() {
        return R.ok(resourceLineService.listResourceLine());
    }
}
