package co.mgentertainment.file.dal.repository.impl;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.file.dal.mapper.AccessClientMapper;
import co.mgentertainment.file.dal.po.AccessClientDO;
import co.mgentertainment.file.dal.po.AccessClientExample;
import co.mgentertainment.file.dal.repository.AccessClientRepository;
import co.mgentertainment.file.service.config.MgfsProperties;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
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
        Preconditions.checkArgument(accessClientDO != null);
        if (StringUtils.isBlank(accessClientDO.getAppCode())) {
            accessClientDO.setAppCode(RandomStringUtils.randomNumeric(6));
        }
        if (accessClientDO.getEncryptAlgorithm() == null) {
            accessClientDO.setEncryptAlgorithm(MgfsProperties.AlgorithmType.RSA.name());
        }
        accessClientMapper.insertSelective(accessClientDO);
        return accessClientDO.getAppCode();
    }

    @Override
    public Boolean updateAccessClient(AccessClientDO accessClientDO) {
        Assert.notNull(accessClientDO, "accessClientDO can not be null");
        Assert.notNull(accessClientDO.getAppCode(), "appCode can not be null");
        AccessClientExample example = new AccessClientExample();
        example.createCriteria().andAppCodeEqualTo(accessClientDO.getAppCode());
        int rowcount = accessClientMapper.updateByExampleSelective(accessClientDO, example);
        return rowcount > 0;
    }

    @Override
    public String saveAccessClient(AccessClientDO accessClientDO) {
        Preconditions.checkArgument(accessClientDO != null, "accessClientDO can not be null");
        if (StringUtils.isNotBlank(accessClientDO.getAppCode())) {
            AccessClientExample example = new AccessClientExample();
            example.createCriteria().andAppCodeEqualTo(accessClientDO.getAppCode()).andDisabledEqualTo((byte) 0);
            boolean exists = accessClientMapper.countByExample(example) > 0;
            if (exists) {
//                updateAccessClient(accessClientDO);
                return accessClientDO.getAppCode();
            }
        } else if (StringUtils.isNotBlank(accessClientDO.getAppName())) {
            AccessClientExample example = new AccessClientExample();
            example.createCriteria().andAppNameEqualTo(accessClientDO.getAppName()).andDisabledEqualTo((byte) 0);
            boolean exists = accessClientMapper.countByExample(example) > 0;
            if (exists) {
                return accessClientDO.getAppCode();
            }
        }
        return addAccessClient(accessClientDO);
    }

    @Override
    public AccessClientDO getAccessClientById(Long id) {
        return accessClientMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean validateAppCode(String appCode) {
        AccessClientExample example = new AccessClientExample();
        example.createCriteria().andAppCodeEqualTo(appCode).andDisabledEqualTo((byte) 0);
        List<AccessClientDO> accessClientDOS = accessClientMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(accessClientDOS)) {
            return false;
        }
        Date expiredDate = accessClientDOS.get(0).getExpiredDate();
        return expiredDate == null || expiredDate.after(new Date());
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
