package co.mgentertainment.file.dal.repository.impl;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.uidgen.impl.CachedUidGenerator;
import co.mgentertainment.file.dal.mapper.ResourceMapper;
import co.mgentertainment.file.dal.po.ResourceDO;
import co.mgentertainment.file.dal.po.ResourceExample;
import co.mgentertainment.file.dal.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author auto
 * @description ResourceRepositoryImpl
 */
@Repository("resourceRepository")
@RequiredArgsConstructor
public class ResourceRepositoryImpl implements ResourceRepository {

    private final CachedUidGenerator cachedUidGenerator;

    private final ResourceMapper resourceMapper;

    @Override
    public Long addResource(ResourceDO resourceDO) {
        if (resourceDO != null) {
            resourceDO.setRid(cachedUidGenerator.getUID());
        }
        resourceMapper.insertSelective(resourceDO);
        return resourceDO.getRid();
    }

    @Override
    public Boolean updateResource(ResourceDO resourceDO, ResourceExample resourceExample) {
        Assert.notNull(resourceDO, "resourceDO can not be null");
        Assert.notNull(resourceDO.getRid(), "rid can not be null");
        int rowcount = resourceMapper.updateByExampleSelective(resourceDO, resourceExample);
        return rowcount > 0;
    }

    @Override
    public Long saveResource(ResourceDO resourceDO) {
        Assert.notNull(resourceDO, "resourceDO can not be null");
        Assert.notNull(resourceDO.getRid(), "rid can not be null");
        ResourceExample example = new ResourceExample();
        example.createCriteria().andRidEqualTo(resourceDO.getRid());
        boolean exists = resourceMapper.countByExample(example) > 0;
        if (exists) {
            updateResource(resourceDO, example);
            return resourceDO.getRid();
        } else {
            return addResource(resourceDO);
        }
    }

    @Override
    public ResourceDO getResourceByRid(Long rid) {
        return resourceMapper.selectByPrimaryKey(rid);
    }

    @Override
    public List<ResourceDO> getResourcesByExample(ResourceExample example) {
        return resourceMapper.selectByExample(example);
    }

    @Override
    public PageResult<ResourceDO> queryResource(ResourceExample example) {
        List<ResourceDO> resourceDOS = null;
        Long count = resourceMapper.countByExample(example);
        if (count > 0) {
            resourceDOS = resourceMapper.selectByExample(example);
        }
        int pageNo = example.getLimit() > 0 ? example.getOffset() / example.getLimit() + 1 : 0;
        return PageResult.createPageResult(pageNo, example.getLimit(), count.intValue(), resourceDOS);
    }

    @Override
    public Boolean removeResource(Long rid) {
        ResourceExample example = new ResourceExample();
        example.createCriteria().andRidEqualTo(rid);
        ResourceDO update = new ResourceDO();
        update.setDeleted(Byte.valueOf("1"));
        return resourceMapper.updateByExample(update, example) > 0;
    }
}
