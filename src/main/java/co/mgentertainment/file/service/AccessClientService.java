package co.mgentertainment.file.service;

import co.mgentertainment.file.service.dto.AccessClientDTO;
import co.mgentertainment.file.service.dto.ApplyAppAccessDTO;

import java.util.List;

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

    /**
     * get all clients
     *
     * @return
     */
    List<AccessClientDTO> getAllClients();
}
