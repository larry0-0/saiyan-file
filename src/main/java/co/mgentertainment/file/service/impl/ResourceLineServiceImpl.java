package co.mgentertainment.file.service.impl;

import co.mgentertainment.file.dal.po.ResourceLineDO;
import co.mgentertainment.file.dal.po.ResourceLineExample;
import co.mgentertainment.file.dal.repository.ResourceLineRepository;
import co.mgentertainment.file.service.converter.FileObjectMapper;
import co.mgentertainment.file.service.dto.ResourceLineDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author larry
 * @createTime 2023/9/19
 * @description ResourceLineServiceImpl
 */
@Service
public class ResourceLineServiceImpl implements ResourceLineService {

    @Resource
    private ResourceLineRepository resourceLineRepository;

    @Override
    public List<ResourceLineDTO> listResourceLine() {
        ResourceLineExample example = new ResourceLineExample();
        example.createCriteria().andDeletedEqualTo((byte) 0);
        example.setOrderByClause("priority asc");
        List<ResourceLineDO> resourceLineDOList = resourceLineRepository.getResourceLinesByExample(example);
        return FileObjectMapper.INSTANCE.toResourceLineDTOList(resourceLineDOList);
    }
}
