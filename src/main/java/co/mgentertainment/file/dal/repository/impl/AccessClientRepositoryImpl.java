package co.mgentertainment.file.dal.repository.impl;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.file.dal.mapper.AccessClientMapper;
import co.mgentertainment.file.dal.po.AccessClientDO;
import co.mgentertainment.file.dal.po.AccessClientExample;
import co.mgentertainment.file.dal.repository.AccessClientRepository;
import co.mgentertainment.file.service.config.MgfsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author auto
 * @description AccessClientRepositoryImpl
 */
@Repository("accessClientRepository")
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AccessClientRepositoryImpl implements AccessClientRepository {

    private final AccessClientMapper accessClientMapper;

    @Override
    public String addAccessClient(AccessClientDO accessClientDO) {
        if (accessClientDO != null) {
            accessClientDO.setAppCode(RandomStringUtils.randomNumeric(6));
            if (accessClientDO.getEncryptAlgorithm() == null) {
                accessClientDO.setEncryptAlgorithm(MgfsProperties.AlgorithmType.RSA.name());
            }
        }
        accessClientMapper.insertSelective(accessClientDO);
        return accessClientDO.getAppCode();
    }

    @Override
    public Boolean updateAccessClient(AccessClientDO accessClientDO, AccessClientExample accessClientExample) {
        Assert.notNull(accessClientDO, "accessClientDO can not be null");
        Assert.notNull(accessClientDO.getAppName(), "appName can not be null");
        int rowcount = accessClientMapper.updateByExampleSelective(accessClientDO, accessClientExample);
        return rowcount > 0;
    }

    @Override
    public String saveAccessClient(AccessClientDO accessClientDO) {
        Assert.notNull(accessClientDO, "accessClientDO can not be null");
        Assert.notNull(accessClientDO.getAppName(), "appName can not be null");
        AccessClientExample example = new AccessClientExample();
        example.createCriteria().andAppNameEqualTo(accessClientDO.getAppName()).andDisabledEqualTo((byte) 0);
        List<AccessClientDO> accessClientDOS = accessClientMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(accessClientDOS)) {
            updateAccessClient(accessClientDO, example);
            return accessClientDOS.get(0).getAppCode();
        } else {
            return addAccessClient(accessClientDO);
        }
    }

    @Override
    public AccessClientDO getAccessClientById(Long id) {
        return accessClientMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean existsAppCode(String appCode) {
        AccessClientExample example = new AccessClientExample();
        example.createCriteria().andAppCodeEqualTo(appCode).andDisabledEqualTo((byte) 0);
        long count = accessClientMapper.countByExample(example);
        return count > 0;
    }

    @Override
    public List<AccessClientDO> getAccessClientsByExample(AccessClientExample example) {
        return accessClientMapper.selectByExample(example);
    }

    @Override
    public PageResult<AccessClientDO> queryAccessClient(AccessClientExample example) {
        List<AccessClientDO> accessClientDOS = null;
        Long count = accessClientMapper.countByExample(example);
        if (count > 0) {
            accessClientDOS = accessClientMapper.selectByExample(example);
        }
        int pageNo = example.getLimit() > 0 ? example.getOffset() / example.getLimit() + 1 : 0;
        return PageResult.createPageResult(pageNo, example.getLimit(), count.intValue(), accessClientDOS);
    }

    @Override
    public Boolean disableAccessClient(String appCode) {
        AccessClientExample example = new AccessClientExample();
        example.createCriteria().andAppCodeEqualTo(appCode);
        AccessClientDO update = new AccessClientDO();
        update.setDisabled(Byte.valueOf("1"));
        return accessClientMapper.updateByExample(update, example) > 0;
    }
}
