package co.mgentertainment.file.web.controller;

import cn.hutool.core.map.MapBuilder;
import co.mgentertainment.common.model.R;
import co.mgentertainment.common.syslog.annotation.SysLog;
import co.mgentertainment.file.service.AccessClientService;
import co.mgentertainment.file.service.dto.ApplyAppAccessDTO;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author larry
 * @createTime 2023/9/8
 * @description AccessClientController
 */
@RestController
@RequestMapping("/access")
@Api(tags = "客户端接入服务")
@RequiredArgsConstructor
public class AccessClientController {
    public static final String TOKEN_HEADER = "API-Token";

    private final AccessClientService accessClientService;

    @PostMapping("/apply")
    @Operation(summary = "申请接入")
    @SysLog("申请接入")
    public R<Map<String, String>> applyClientAccess(@RequestBody ApplyAppAccessDTO applyAppAccessDTO) {
        String token = accessClientService.applyAccess(applyAppAccessDTO);
        MapBuilder<String, String> mapBuilder = MapBuilder.create();
        return R.ok(mapBuilder.put(TOKEN_HEADER, token).build());
    }
}
