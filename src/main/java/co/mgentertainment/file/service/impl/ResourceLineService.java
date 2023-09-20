package co.mgentertainment.file.service.impl;

import co.mgentertainment.file.service.dto.ResourceLineDTO;

import java.util.List;

/**
 * @author larry
 * @createTime 2023/9/19
 * @description ResourceLineService
 */
public interface ResourceLineService {

    /**
     * list resource line
     *
     * @return
     */
    List<ResourceLineDTO> listResourceLine();
}
