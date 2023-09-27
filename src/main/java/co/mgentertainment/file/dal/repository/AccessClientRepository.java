package co.mgentertainment.file.dal.repository;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.file.dal.po.AccessClientDO;
import co.mgentertainment.file.dal.po.AccessClientExample;

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
     * @param accessClientExample
     * @return
     */
    Boolean updateAccessClient(AccessClientDO accessClientDO, AccessClientExample accessClientExample);

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
    boolean existsAppCode(String appCode);

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
