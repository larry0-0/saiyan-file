package co.saiyan.file.dal.repository.impl;

import co.saiyan.file.dal.mapper.ResourceLineMapper;
import co.saiyan.file.dal.po.ResourceLineDO;
import co.saiyan.file.dal.po.ResourceLineExample;
import co.saiyan.file.dal.repository.ResourceLineRepository;
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
