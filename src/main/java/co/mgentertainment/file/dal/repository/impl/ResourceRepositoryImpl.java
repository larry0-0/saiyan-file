package co.mgentertainment.file.dal.repository.impl;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.uidgen.impl.CachedUidGenerator;
import co.mgentertainment.file.dal.mapper.FileUploadMapper;
import co.mgentertainment.file.dal.mapper.ResourceExtMapper;
import co.mgentertainment.file.dal.mapper.ResourceMapper;
import co.mgentertainment.file.dal.po.*;
import co.mgentertainment.file.dal.repository.ResourceRepository;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author auto
 * @description ResourceRepositoryImpl
 */
@Repository("resourceRepository")
@RequiredArgsConstructor
public class ResourceRepositoryImpl implements ResourceRepository {

    private final CachedUidGenerator cachedUidGenerator;

    private final ResourceMapper resourceMapper;

    private final FileUploadMapper fileUploadMapper;

    private final ResourceExtMapper resourceExtMapper;

    @Override
    public Long addResource(ResourceDO resourceDO) {
        if (resourceDO != null && resourceDO.getRid() == null) {
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
        if (resourceDO.getRid() != null) {
            ResourceExample example = new ResourceExample();
            example.createCriteria().andRidEqualTo(resourceDO.getRid());
            if (resourceMapper.countByExample(example) > 0) {
                updateResource(resourceDO, example);
                return resourceDO.getRid();
            }
        }
        return addResource(resourceDO);
    }

    @Override
    public ResourceDO getResourceByRid(Long rid) {
        if (rid == null) {
            return null;
        }
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

    @Override
    public List<ResourceDO> getResourceByUploadIds(List<Long> uploadIds) {
        FileUploadExample example = new FileUploadExample();
        FileUploadExample.Criteria criteria = example.createCriteria().andDeletedEqualTo((byte) 0);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(uploadIds)) {
            criteria.andUploadIdIn(uploadIds);
        }
        List<FileUploadDO> fileUploadDOS = fileUploadMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(fileUploadDOS)) {
            return Lists.newArrayList();
        }
        ResourceExample resourceExample = new ResourceExample();
        ResourceExample.Criteria criteria2 = resourceExample.createCriteria().andDeletedEqualTo((byte) 0);
        List<Long> rids = fileUploadDOS.stream().filter(dO -> dO.getRid() != null).map(dO -> dO.getRid())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(rids)) {
            criteria2.andRidIn(rids);
        }
        return resourceMapper.selectByExample(resourceExample);
    }

    @Override
    public ResourceExtDO getUploadResource(Long uploadId) {
        return resourceExtMapper.selectByUploadId(uploadId);
    }
}
