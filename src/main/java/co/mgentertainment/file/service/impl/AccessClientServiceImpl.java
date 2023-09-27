package co.mgentertainment.file.service.impl;

import co.mgentertainment.common.utils.SecurityHelper;
import co.mgentertainment.file.dal.po.AccessClientDO;
import co.mgentertainment.file.dal.repository.AccessClientRepository;
import co.mgentertainment.file.service.AccessClientService;
import co.mgentertainment.file.service.config.MgfsProperties;
import co.mgentertainment.file.service.dto.ApplyAppAccessDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

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
            String encrypt = SecurityHelper.rsaEncrypt(appCode, mgfsProperties.getAuthentication().getRsaPublicKey());
            return String.format("%s;%d", encrypt, (System.currentTimeMillis() + RandomUtils.nextInt(0, 9000)));
        }
        return SecurityHelper.hyperEncrypt(appCode, mgfsProperties.getAuthentication().getAesSecret());
    }

    @Override
    public Boolean disableAccess(String appCode) {
        return accessClientRepository.disableAccessClient(appCode);
    }
}
