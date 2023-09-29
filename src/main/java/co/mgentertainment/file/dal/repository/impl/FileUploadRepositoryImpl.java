package co.mgentertainment.file.dal.repository.impl;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.model.media.ResourceTypeEnum;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.uidgen.impl.CachedUidGenerator;
import co.mgentertainment.file.dal.mapper.FileUploadExtMapper;
import co.mgentertainment.file.dal.mapper.FileUploadMapper;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author auto
 * @description FileUploadRepositoryImpl
 */
@Repository("fileUploadRepository")
@RequiredArgsConstructor
public class FileUploadRepositoryImpl implements FileUploadRepository {

    private final CachedUidGenerator cachedUidGenerator;

    private final FileUploadMapper fileUploadMapper;

    private final FileUploadExtMapper fileUploadExtMapper;

    @Override
    public Long addFileUpload(FileUploadDO fileUploadDO) {
        if (fileUploadDO != null && fileUploadDO.getUploadId() == null) {
            fileUploadDO.setUploadId(cachedUidGenerator.getUID());
        }
        fileUploadMapper.insertSelective(fileUploadDO);
        return fileUploadDO.getUploadId();
    }

    @Override
    public Map<String, Long> batchAddFileUpload(List<String> filenames, ResourceTypeEnum resourceTypeEnum, String appCode) {
        if (CollectionUtils.isEmpty(filenames) || resourceTypeEnum == null) {
            return Maps.newHashMap();
        }
        List<FileUploadDO> list = filenames.stream().map(fn -> {
            FileUploadDO fileUploadDO = new FileUploadDO();
            fileUploadDO.setUploadId(cachedUidGenerator.getUID());
            fileUploadDO.setFilename(fn);
            fileUploadDO.setType(Integer.valueOf(resourceTypeEnum.getValue()).shortValue());
            fileUploadDO.setStatus(Integer.valueOf(UploadStatusEnum.CONVERTING.getValue()).shortValue());
            if (StringUtils.isNotBlank(appCode)) {
                fileUploadDO.setAppCode(appCode);
            }
            return fileUploadDO;
        }).collect(Collectors.toList());
        int rowcount = fileUploadExtMapper.batchInsert(list);
        if (rowcount > 0) {
            return list.stream().collect(Collectors.toMap(FileUploadDO::getFilename, FileUploadDO::getUploadId));
        }
        return Maps.newHashMap();
    }

    @Override
    public Boolean updateFileUpload(FileUploadDO fileUploadDO, FileUploadExample fileUploadExample) {
        Assert.notNull(fileUploadDO, "fileUploadDO can not be null");
        Assert.notNull(fileUploadDO.getUploadId(), "uploadId can not be null");
        int rowcount = fileUploadMapper.updateByExampleSelective(fileUploadDO, fileUploadExample);
        return rowcount > 0;
    }

    @Override
    public Long saveFileUpload(FileUploadDO fileUploadDO) {
        Assert.notNull(fileUploadDO, "fileUploadDO can not be null");
        Assert.notNull(fileUploadDO.getUploadId(), "uploadId can not be null");
        FileUploadExample example = new FileUploadExample();
        example.createCriteria().andUploadIdEqualTo(fileUploadDO.getUploadId());
        boolean exists = fileUploadMapper.countByExample(example) > 0;
        if (exists) {
            updateFileUpload(fileUploadDO, example);
            return fileUploadDO.getUploadId();
        } else {
            return addFileUpload(fileUploadDO);
        }
    }

    @Override
    public FileUploadDO getFileUploadByUploadId(Long uploadId) {
        return fileUploadMapper.selectByPrimaryKey(uploadId);
    }

    @Override
    public List<FileUploadDO> getFileUploadsByExample(FileUploadExample example) {
        return fileUploadMapper.selectByExample(example);
    }

    @Override
    public PageResult<FileUploadDO> queryFileUpload(FileUploadExample example) {
        List<FileUploadDO> fileUploadDOS = null;
        Long count = fileUploadMapper.countByExample(example);
        if (count > 0) {
            fileUploadDOS = fileUploadMapper.selectByExample(example);
        }
        int pageNo = example.getLimit() > 0 ? example.getOffset() / example.getLimit() + 1 : 0;
        return PageResult.createPageResult(pageNo, example.getLimit(), count.intValue(), fileUploadDOS);
    }

    @Override
    public Boolean removeFileUpload(Long uploadId) {
        FileUploadExample example = new FileUploadExample();
        example.createCriteria().andUploadIdEqualTo(uploadId);
        FileUploadDO update = new FileUploadDO();
        update.setDeleted(Byte.valueOf("1"));
        return fileUploadMapper.updateByExample(update, example) > 0;
    }

    @Override
    public Boolean updateFileUploadByPrimaryKey(FileUploadDO uploadDO) {
        Assert.notNull(uploadDO, "fileUploadDO can not be null");
        Assert.notNull(uploadDO.getUploadId(), "uploadId can not be null");
        return fileUploadMapper.updateByPrimaryKeySelective(uploadDO) > 0;
    }
}
