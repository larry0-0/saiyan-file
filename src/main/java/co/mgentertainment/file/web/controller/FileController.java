package co.mgentertainment.file.web.controller;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.model.R;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.syslog.annotation.SysLog;
import co.mgentertainment.file.service.FileService;
import co.mgentertainment.file.service.config.CuttingSetting;
import co.mgentertainment.file.service.dto.*;
import co.mgentertainment.file.service.impl.ResourceLineService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @SysLog("上传图片")
    public R<UploadedImageDTO> uploadImage(@RequestParam(value = "file") MultipartFile file) {
        return R.ok(fileService.uploadImage(file));
    }

    @PostMapping("/upload/file")
    @Operation(summary = "普通文件上传")
    @SysLog("上传普通文件")
    public R<UploadedFileDTO> uploadFile(@RequestParam(value = "file") MultipartFile file) {
        return R.ok(fileService.uploadFile(file));
    }

    @PostMapping("/upload/video")
    @Operation(summary = "视频上传")
    @SysLog("上传视频")
    public R<VideoUploadInfoDTO> uploadVideo(@RequestParam(value = "file") MultipartFile file, CuttingSetting cuttingSetting) {
        return R.ok(fileService.uploadVideo(file, cuttingSetting));
    }

    @PostMapping("/upload/retry")
    @Operation(summary = "重试失败的视频")
    @SysLog("失败重试")
    public R<Void> retryUploadVideo(@RequestBody RetryVideoUploadDTO retryVideoUploadDTO) {
        fileService.reuploadVideo(retryVideoUploadDTO.getUploadId(),
                CuttingSetting.builder()
                        .trailerDuration(retryVideoUploadDTO.getDuration())
                        .trailerStartFromProportion(retryVideoUploadDTO.getStartFromProportion())
                        .build());
        return R.ok();
    }

    @PostMapping("/upload/videos")
    @Operation(summary = "批量视频上传")
    @SysLog(value = "批量上传视频", ignoredArgs = true)
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

    @PostMapping("/batchAddUploadRecord")
    @Operation(summary = "供上传器批量添加上传记录")
    public R<Map<String, Long>> batchAddUploadRecord(@RequestBody AddUploadsRequest request) {
        CuttingSetting cuttingSetting = CuttingSetting.builder()
                .trailerDuration(Optional.ofNullable(request.getTrailerDuration()).orElse(30))
                .trailerStartFromProportion(Optional.ofNullable(request.getTrailerStartFromProportion()).orElse(0))
                .build();
        return R.ok(fileService.batchAddUploadVideoRecord(request.filenames, cuttingSetting));
    }

    @PostMapping("/batchUpdateUploadStatus")
    @Operation(summary = "供上传器批量更新上传状态")
    public R<Void> batchUpdateUploadStatus(@RequestBody UpdateUploadsRequest request) {
        fileService.batchUpdateUploadStatus(request.getUploadIds(), UploadStatusEnum.getByValue(request.getStatusCode()));
        return R.ok();
    }

    @PostMapping("/updateUploadStatus")
    @Operation(summary = "供上传器更新上传状态")
    public R<Void> updateUploadStatus(@RequestBody FileUploadDTO fileUploadDTO) {
        fileService.updateUpload(fileUploadDTO);
        return R.ok();
    }

    @PostMapping("/addResource")
    @Operation(summary = "供上传器添加媒资")
    public R<Long> addResource(@RequestBody ResourceDTO resourceDTO) {
        return R.ok(fileService.saveResource(resourceDTO));
    }

    @Data
    public static class AddUploadsRequest {
        private List<String> filenames;
        private Integer trailerDuration;
        private Integer trailerStartFromProportion;
    }

    @Data
    public static class UpdateUploadsRequest {
        private List<Long> uploadIds;
        private Integer statusCode;
    }
}
