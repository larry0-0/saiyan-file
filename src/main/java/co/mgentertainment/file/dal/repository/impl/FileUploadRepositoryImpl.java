package co.mgentertainment.file.dal.repository.impl;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.uidgen.impl.CachedUidGenerator;
import co.mgentertainment.file.dal.enums.UploadStatusEnum;
import co.mgentertainment.file.dal.mapper.FileUploadMapper;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author auto
 * @description FileUploadRepositoryImpl
 */
@Repository("fileUploadRepository")
@RequiredArgsConstructor
public class FileUploadRepositoryImpl implements FileUploadRepository {

    private final CachedUidGenerator cachedUidGenerator;

    private final FileUploadMapper fileUploadMapper;

    @Override
    public Long addFileUpload(FileUploadDO fileUploadDO) {
        if (fileUploadDO != null) {
            fileUploadDO.setUploadId(cachedUidGenerator.getUID());
        }
        fileUploadMapper.insertSelective(fileUploadDO);
        return fileUploadDO.getUploadId();
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
    public Boolean updateUploadStatus(Long uploadId, UploadStatusEnum status, @Nullable Long rid) {
        FileUploadDO upload = new FileUploadDO();
        upload.setUploadId(uploadId);
        upload.setRid(rid);
        upload.setStatus(Optional.ofNullable(status.getValue()).orElse(0).shortValue());
        fileUploadMapper.updateByPrimaryKeySelective(upload);
        return null;
    }
}
