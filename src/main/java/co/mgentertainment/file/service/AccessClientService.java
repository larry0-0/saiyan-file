package co.mgentertainment.file.service;

import co.mgentertainment.file.service.dto.ApplyAppAccessDTO;

/**
 * @author larry
 * @createTime 2023/9/22
 * @description AccessClientService
 */
public interface AccessClientService {

    /**
     * apply client app Access
     *
     * @param applyAppAccessDTO
     * @return
     */
    String applyAccess(ApplyAppAccessDTO applyAppAccessDTO);

    /**
     * disable client app Access
     *
     * @param appCode
     * @return
     */
    Boolean disableAccess(String appCode);
}
