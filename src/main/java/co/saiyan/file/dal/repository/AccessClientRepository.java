package co.saiyan.file.dal.repository;

import co.saiyan.common.model.PageResult;
import co.saiyan.file.dal.po.AccessClientDO;
import co.saiyan.file.dal.po.AccessClientExample;

import java.util.List;

/**
 * @author auto
 * @description AccessClientRepository
 */
public interface AccessClientRepository {

    /**
     * add AccessClient
     *
     * @param accessClientDO
     * @return id
     */
    String addAccessClient(AccessClientDO accessClientDO);

    /**
     * update AccessClient
     *
     * @param accessClientDO
     * @return
     */
    Boolean updateAccessClient(AccessClientDO accessClientDO);

    /**
     * save AccessClient
     *
     * @param accessClientDO
     * @return
     */
    String saveAccessClient(AccessClientDO accessClientDO);

    /**
     * get AccessClient by id
     *
     * @param id
     * @return
     */
    AccessClientDO getAccessClientById(Long id);

    /**
     * existsAppCode
     *
     * @param appCode
     * @return
     */
    boolean validateAppCode(String appCode);

    /**
     * list AccessClient by example
     *
     * @param example
     * @return
     */
    List<AccessClientDO> getAccessClientsByExample(AccessClientExample example);

    /**
     * query AccessClient
     *
     * @param example
     * @return
     */
    PageResult<AccessClientDO> queryAccessClient(AccessClientExample example);

    /**
     * disable AccessClient by appCode
     *
     * @param appCode
     * @return
     */
    Boolean disableAccessClient(String appCode);
}
