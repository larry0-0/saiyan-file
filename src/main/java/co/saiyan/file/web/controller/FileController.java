package co.saiyan.file.web.controller;

import cn.hutool.core.lang.mutable.MutablePair;
import co.saiyan.common.devent.DistributedEventProvider;
import co.saiyan.common.model.PageResult;
import co.saiyan.common.model.R;
import co.saiyan.common.model.media.ResourceTypeEnum;
import co.saiyan.common.model.media.UploadStatusEnum;
import co.saiyan.common.model.media.UploadSubStatusEnum;
import co.saiyan.common.syslog.annotation.SysLog;
import co.saiyan.file.service.FileService;
import co.saiyan.file.service.UploadWorkflowService;
import co.saiyan.file.service.config.CuttingSetting;
import co.saiyan.file.service.config.MgfsProperties;
import co.saiyan.file.service.dto.*;
import co.saiyan.file.service.dto.*;
import co.saiyan.file.service.event.listener.DistributedEventKey;
import co.saiyan.file.service.impl.ResourceLineService;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author larry
 * @createTime 2023/9/8
 * @description FileController
 */
@RestController
@RequestMapping("/file")
@Api(tags = "文件服务")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;
    private final UploadWorkflowService uploadWorkflowService;
    private final ResourceLineService resourceLineService;
    private final DistributedEventProvider distributedEventProvider;
    private final MgfsProperties mgfsProperties;

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
    public R<VideoUploadInfoDTO> uploadVideo(@RequestParam(value = "file") MultipartFile file, CuttingSetting cuttingSetting, Boolean isShortVideo) {
        return R.ok(uploadWorkflowService.startUploadingWithMultipartFile(file, cuttingSetting, isShortVideo));
    }

    @PostMapping("/upload/retry")
    @Operation(summary = "重试失败的视频")
    @SysLog("失败重试")
    public R<Void> retryUploadVideo(@RequestBody RetryVideoUploadDTO retryVideoUploadDTO) {
        uploadWorkflowService.recoverUploading(retryVideoUploadDTO.getUploadId(), retryVideoUploadDTO.getCuttingSetting());
        return R.ok();
    }

    @PostMapping("/upload/videos")
    @Operation(summary = "批量视频上传")
    @SysLog(value = "批量上传视频", ignoredArgs = true)
    public R<List<VideoUploadInfoDTO>> batchUploadVideo(@NotNull @RequestParam("files") MultipartFile[] files, CuttingSetting cuttingSetting, Boolean isShortVideo) {
        List<VideoUploadInfoDTO> list = new ArrayList<>();
        for (MultipartFile file : files) {
            R<VideoUploadInfoDTO> res = uploadVideo(file, cuttingSetting, isShortVideo);
            list.add(res.getData());
        }
        return R.ok(list);
    }

    @PostMapping("/upload/start/{type}")
    @Operation(summary = "开始服务器内部上传")
    @SysLog(value = "开始服务器内部上传", ignoredArgs = true)
    public R<Void> startInnerUploads(@PathVariable("type") Integer type) {
        try {
            distributedEventProvider.fire(DistributedEventKey.UPLOADS,
                    ResourceTypeEnum.getByValue(type) == ResourceTypeEnum.SHORT ?
                            mgfsProperties.getServerFilePath().getSv() : mgfsProperties.getServerFilePath().getMzk());
        } catch (NacosException e) {
            log.error("添加定时任务事件失败", e);
        }
        return R.ok();
    }

    @PostMapping("/upload/reset/{appCode}")
    @Operation(summary = "失败重置")
    @SysLog(value = "失败重置", ignoredArgs = true)
    public R<Void> resetFailedUploads(@PathVariable("appCode") String appCode) {
        fileService.resetFailedUploads(Optional.ofNullable(appCode));
        return R.ok();
    }

    @PostMapping("/listUploadProgress")
    @Operation(summary = "根据uploadId获取视频上传进展")
    public R<List<VideoUploadInfoDTO>> listUploadInfo(@RequestBody List<Long> uploadIds) {
        if (CollectionUtils.isEmpty(uploadIds)) {
            return R.ok(Lists.newArrayList());
        }
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

    @PostMapping("/list/status")
    @Operation(summary = "获取状态列表")
    public R<ListStatusResponse> getUploadStatus() {
        return R.ok(new ListStatusResponse(Arrays.stream(UploadStatusEnum.values()).map(e -> new MutablePair(e.getValue(), e.getDesc())).collect(Collectors.toList()),
                Arrays.stream(UploadSubStatusEnum.values()).map(e -> new MutablePair(e.getValue(), e.getDesc())).collect(Collectors.toList())));
    }


    @PostMapping("/batchAddUploadRecord")
    @Operation(summary = "供上传器批量添加上传记录")
    public R<Map<String, Long>> batchAddUploadRecord(@RequestBody AddUploadsRequest request) {
        int defaultTrailerTimeLength = mgfsProperties.getUserTrailerTimeLength();
        CuttingSetting cuttingSetting = CuttingSetting.builder()
                .trailerDuration(Optional.ofNullable(request.getTrailerDuration()).orElse(defaultTrailerTimeLength))
                .trailerStartFromProportion(Optional.ofNullable(request.getTrailerStartFromProportion()).orElse(0))
                .build();
        return R.ok(fileService.batchAddUploadVideoRecord(request.filenames, cuttingSetting, request.isShortVideo));
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

    @GetMapping("/getUpload/{uploadId}")
    public R<UploadResourceDTO> getUpload(@PathVariable("uploadId") @NotNull Long uploadId) {
        UploadResourceDTO uploadResource = fileService.getUploadResource(uploadId);
        if (uploadResource == null || uploadResource.getUploadId() == null) {
            return R.failed("参数异常,未找到uploadId");
        }
        return R.ok(uploadResource);
    }

    @Data
    public static class AddUploadsRequest {
        private List<String> filenames;
        private Integer trailerDuration;
        private Integer trailerStartFromProportion;
        private Boolean isShortVideo;
    }

    @Data
    public static class UpdateUploadsRequest {
        private List<Long> uploadIds;
        private Integer statusCode;
    }

    @Data
    @AllArgsConstructor
    public static class ListStatusResponse {
        private List<MutablePair> mainStatus;
        private List<MutablePair> viceStatus;
    }
}
