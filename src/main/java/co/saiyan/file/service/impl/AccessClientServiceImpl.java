package co.saiyan.file.service.impl;

import co.saiyan.common.utils.SecurityHelper;
import co.saiyan.file.dal.po.AccessClientDO;
import co.saiyan.file.dal.po.AccessClientExample;
import co.saiyan.file.dal.repository.AccessClientRepository;
import co.saiyan.file.service.AccessClientService;
import co.saiyan.file.service.config.MgfsProperties;
import co.saiyan.file.service.converter.FileObjectMapper;
import co.saiyan.file.service.dto.AccessClientDTO;
import co.saiyan.file.service.dto.ApplyAppAccessDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author larry
 * @createTime 2023/9/22
 * @description AccessClientServiceImpl
 */
@Service
@RequiredArgsConstructor
public class AccessClientServiceImpl implements AccessClientService {

    private final AccessClientRepository accessClientRepository;
    private final MgfsProperties mgfsProperties;

    @Override
    public String applyAccess(ApplyAppAccessDTO applyAppAccessDTO) {
        AccessClientDO accessClientDO = new AccessClientDO();
        accessClientDO.setAppName(applyAppAccessDTO.getAppName());
        accessClientDO.setEncryptAlgorithm(Optional.ofNullable(applyAppAccessDTO.getAlgorithmType()).orElse(MgfsProperties.AlgorithmType.RSA.name()));
        String appCode = accessClientRepository.saveAccessClient(accessClientDO);
        if (MgfsProperties.AlgorithmType.RSA.name().equalsIgnoreCase(accessClientDO.getEncryptAlgorithm())) {
            Date expiredDate = applyAppAccessDTO.getExpiredDate();
            return SecurityHelper.rsaPeriodEncrypt(appCode, mgfsProperties.getAuthentication().getRsaPublicKey(), expiredDate);
        }
        return SecurityHelper.hyperEncrypt(appCode, mgfsProperties.getAuthentication().getAesSecret());
    }

    @Override
    public Boolean disableAccess(String appCode) {
        return accessClientRepository.disableAccessClient(appCode);
    }

    @Override
    public List<AccessClientDTO> getAllClients() {
        AccessClientExample example = new AccessClientExample();
        example.createCriteria().andDisabledEqualTo((byte) 0);
        List<AccessClientDO> accessClientDOList = accessClientRepository.getAccessClientsByExample(example);
        if (CollectionUtils.isEmpty(accessClientDOList)) {
            return Lists.newArrayList();
        }
        return FileObjectMapper.INSTANCE.toAccessClientDTOList(accessClientDOList);
    }
}
