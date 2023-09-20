package co.mgentertainment.file.dal.repository.impl;

import co.mgentertainment.file.dal.mapper.ResourceLineMapper;
import co.mgentertainment.file.dal.po.ResourceLineDO;
import co.mgentertainment.file.dal.po.ResourceLineExample;
import co.mgentertainment.file.dal.repository.ResourceLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author auto
 * @description ResourceLineRepositoryImpl
 */
@Repository("resourceLineRepository")
@RequiredArgsConstructor
public class ResourceLineRepositoryImpl implements ResourceLineRepository {

    private final ResourceLineMapper resourceLineMapper;

    @Override
    public List<ResourceLineDO> getResourceLinesByExample(ResourceLineExample example) {
        return resourceLineMapper.selectByExample(example);
    }
}
