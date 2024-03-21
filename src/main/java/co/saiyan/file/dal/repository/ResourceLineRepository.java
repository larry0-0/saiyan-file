package co.saiyan.file.dal.repository;

import co.saiyan.file.dal.po.ResourceLineDO;
import co.saiyan.file.dal.po.ResourceLineExample;

import java.util.List;

/**
 * @author auto
 * @description ResourceLineRepository
 */
public interface ResourceLineRepository {

    /**
     * list Resource line by example
     *
     * @param example
     * @return
     */
    List<ResourceLineDO> getResourceLinesByExample(ResourceLineExample example);
}
