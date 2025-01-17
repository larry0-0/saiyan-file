package co.saiyan.file.web.controller;

import cn.hutool.core.map.MapBuilder;
import co.saiyan.common.model.R;
import co.saiyan.common.syslog.annotation.SysLog;
import co.saiyan.file.service.AccessClientService;
import co.saiyan.file.service.config.MgfsProperties;
import co.saiyan.file.service.dto.AccessClientDTO;
import co.saiyan.file.service.dto.ApplyAppAccessDTO;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
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
    private final MgfsProperties mgfsProperties;

    private final AccessClientService accessClientService;

    @PostMapping("/apply")
    @Operation(summary = "申请接入")
    @SysLog("申请接入")
    public R<Map<String, String>> applyClientAccess(@RequestBody @Valid ApplyAppAccessDTO applyAppAccessDTO) {
        String token = accessClientService.applyAccess(applyAppAccessDTO);
        MapBuilder<String, String> mapBuilder = MapBuilder.create();
        return R.ok(mapBuilder.put(mgfsProperties.getApiToken(), token).build());
    }

    @PostMapping("/disable/{appCode}")
    @Operation(summary = "让接入失效")
    @SysLog("disable接入")
    public R<Boolean> disableAccess(@PathVariable("appCode") @NotNull String appCode) {
        return R.ok(accessClientService.disableAccess(appCode));
    }

    @PostMapping("/clients")
    @Operation(summary = "查询所有接入客户端")
    @SysLog("查询所有接入客户端")
    public R<List<AccessClientDTO>> getAllClients() {
        return R.ok(accessClientService.getAllClients());
    }
}
