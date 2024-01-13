package co.mgentertainment.file.dal.repository.impl;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.model.media.ResourceTypeEnum;
import co.mgentertainment.common.model.media.UploadStatusEnum;
import co.mgentertainment.common.model.media.UploadSubStatusEnum;
import co.mgentertainment.common.uidgen.impl.CachedUidGenerator;
import co.mgentertainment.file.dal.mapper.FileUploadExtMapper;
import co.mgentertainment.file.dal.mapper.FileUploadMapper;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import co.mgentertainment.file.dal.repository.FileUploadRepository;
import co.mgentertainment.file.service.utils.MediaHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author auto
 * @description FileUploadRepositoryImpl
 */
@Repository("fileUploadRepository")
@RequiredArgsConstructor
@Slf4j
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
    public Map<String, Long> batchAddFileUpload(List<String> filenames, ResourceTypeEnum resourceTypeEnum, String appCode, Integer trailerDuration, Integer trailerStartFromProportion) {
        if (CollectionUtils.isEmpty(filenames) || resourceTypeEnum == null) {
            return Maps.newHashMap();
        }
        List<FileUploadDO> list = filenames.stream().map(fn -> {
            long uploadId = cachedUidGenerator.getUID();
            FileUploadDO fileUploadDO = new FileUploadDO();
            fileUploadDO.setUploadId(uploadId);
            fileUploadDO.setTitle(fn);
            fileUploadDO.setFilename(MediaHelper.getUploadIdFilename(fn, uploadId));
            fileUploadDO.setType(Integer.valueOf(resourceTypeEnum.getValue()).shortValue());
            fileUploadDO.setStatus(UploadStatusEnum.CONVERTING.getValue().shortValue());
            fileUploadDO.setAppCode(Optional.ofNullable(appCode).orElse(StringUtils.EMPTY));
            fileUploadDO.setHasTrailer(Objects.nonNull(trailerDuration) ? (byte) 1 : (byte) 0);
            fileUploadDO.setTrailerDuration(Optional.ofNullable(trailerDuration).orElse(0));
            fileUploadDO.setTrailerStartPos(Optional.ofNullable(trailerStartFromProportion).orElse(0));
            return fileUploadDO;
        }).collect(Collectors.toList());
        int rowcount = fileUploadExtMapper.batchInsert(list);
        if (rowcount > 0) {
            return list.stream().collect(Collectors.toMap(FileUploadDO::getFilename, FileUploadDO::getUploadId));
        }
        return Maps.newHashMap();
    }

    @Override
    public void batchUpdateUploadStatus(List<Long> uploadIds, UploadStatusEnum status) {
        if (CollectionUtils.isEmpty(uploadIds) || status == null) {
            return;
        }
        Lists.partition(uploadIds, 200).forEach(sub -> {
            try {
                FileUploadDO update = new FileUploadDO();
                update.setStatus(Integer.valueOf(status.getValue()).shortValue());
                FileUploadExample example = new FileUploadExample();
                example.createCriteria().andUploadIdIn(sub);
                fileUploadMapper.updateByExampleSelective(update, example);
            } catch (Exception e) {
                log.error("批量修改上传状态失败", e);
            }
        });
    }

    @Override
    public Boolean updateFileUpload(FileUploadDO fileUploadDO, FileUploadExample fileUploadExample) {
        Assert.notNull(fileUploadDO, "fileUploadDO can not be null");
        int rowcount = fileUploadMapper.updateByExampleSelective(fileUploadDO, fileUploadExample);
        return rowcount > 0;
    }

    @Override
    public Long saveFileUpload(FileUploadDO fileUploadDO) {
        Assert.notNull(fileUploadDO, "fileUploadDO can not be null");
        if (fileUploadDO.getUploadId() != null) {
            FileUploadExample example = new FileUploadExample();
            example.createCriteria().andUploadIdEqualTo(fileUploadDO.getUploadId());
            boolean exists = fileUploadMapper.countByExample(example) > 0;
            if (exists) {
                updateFileUpload(fileUploadDO, example);
                return fileUploadDO.getUploadId();
            }
        }
        return addFileUpload(fileUploadDO);
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
        return PageResult.createPageResult(pageNo, example.getLimit(), count, fileUploadDOS);
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

    @Override
    public List<FileUploadDO> getUploadsByStatusInTime(List<UploadStatusEnum> statusList, List<UploadSubStatusEnum> subStatusList, Date deadline) {
        List<Integer> statusCollection = statusList.stream().map(UploadStatusEnum::getValue).collect(Collectors.toList());
        List<Integer> subStatusCollection = subStatusList.stream().map(UploadSubStatusEnum::getValue).collect(Collectors.toList());
        return fileUploadExtMapper.selectByStatusAndInTime(statusCollection, subStatusCollection, deadline);
    }

    @Override
    public void resetCreateTimeForFailedUploads(String appCode) {
        fileUploadExtMapper.resetCreateTimeForFailedUploads(appCode,
                Lists.newArrayList(UploadStatusEnum.CONVERT_FAILURE.getValue(),
                        UploadStatusEnum.UPLOAD_FAILURE.getValue(),
                        UploadStatusEnum.CAPTURE_FAILURE.getValue()),
                Lists.newArrayList(UploadSubStatusEnum.PRINT_FAILURE.getValue(),
                        UploadSubStatusEnum.UPLOAD_ORIGIN_FAILURE.getValue(),
                        UploadSubStatusEnum.CUT_TRAILER_FAILURE.getValue(),
                        UploadSubStatusEnum.UPLOAD_TRAILER_FAILURE.getValue()));
    }
}
