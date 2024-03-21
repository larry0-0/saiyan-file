package co.saiyan.file.dal.repository;

import co.saiyan.common.model.PageResult;
import co.saiyan.file.dal.po.ResourceDO;
import co.saiyan.file.dal.po.ResourceExample;
import co.saiyan.file.dal.po.ResourceExtDO;

import java.util.List;

/**
 * @author auto
 * @description ResourceRepository
 */
public interface ResourceRepository {

    /**
     * add Resource
     *
     * @param resourceDO
     * @return rid
     */
    Long addResource(ResourceDO resourceDO);

    /**
     * update Resource
     *
     * @param resourceDO
     * @param resourceExample
     * @return
     */
    Boolean updateResource(ResourceDO resourceDO, ResourceExample resourceExample);

    /**
     * save Resource
     *
     * @param resourceDO
     * @return
     */
    Long saveResource(ResourceDO resourceDO);

    /**
     * get Resource by rid
     *
     * @param rid
     * @return
     */
    ResourceDO getResourceByRid(Long rid);

    /**
     * list Resource by example
     *
     * @param example
     * @return
     */
    List<ResourceDO> getResourcesByExample(ResourceExample example);

    /**
     * query Resource
     *
     * @param example
     * @return
     */
    PageResult<ResourceDO> queryResource(ResourceExample example);

    /**
     * remove Resource by rid
     *
     * @param rid
     * @return
     */
    Boolean removeResource(Long rid);

    /**
     * get Resource by uploadIds
     *
     * @param uploadIds
     * @return
     */
    List<ResourceDO> getResourceByUploadIds(List<Long> uploadIds);

    /**
     * get ResourceExt by uploadId
     * @param uploadId
     * @return
     */
    ResourceExtDO getUploadResource(Long uploadId);
}
