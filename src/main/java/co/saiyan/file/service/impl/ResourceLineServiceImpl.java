package co.saiyan.file.service.impl;

import co.saiyan.file.dal.po.ResourceLineDO;
import co.saiyan.file.dal.po.ResourceLineExample;
import co.saiyan.file.dal.repository.ResourceLineRepository;
import co.saiyan.file.service.converter.FileObjectMapper;
import co.saiyan.file.service.dto.ResourceLineDTO;
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
