package co.saiyan.file.dal.repository;

import co.saiyan.common.model.PageResult;
import co.saiyan.common.model.media.ResourceTypeEnum;
import co.saiyan.common.model.media.UploadStatusEnum;
import co.saiyan.common.model.media.UploadSubStatusEnum;
import co.saiyan.file.dal.po.FileUploadDO;
import co.saiyan.file.dal.po.FileUploadExample;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author auto
 * @description FileUploadRepository
 */
public interface FileUploadRepository {

    /**
     * add FileUpload
     *
     * @param fileUploadDO
     * @return uploadId
     */
    Long addFileUpload(FileUploadDO fileUploadDO);

    /**
     * batch add FileUpload
     *
     * @param filenames
     * @param resourceTypeEnum
     * @param appCode
     * @return
     */
    Map<String, Long> batchAddFileUpload(List<String> filenames, ResourceTypeEnum resourceTypeEnum, String appCode, Integer trailerDuration, Integer trailerStartFromProportion);

    /**
     * batch update FileUpload status
     *
     * @param uploadIds
     * @param status
     */
    void batchUpdateUploadStatus(List<Long> uploadIds, UploadStatusEnum status);

    /**
     * update FileUpload
     *
     * @param fileUploadDO
     * @param fileUploadExample
     * @return
     */
    Boolean updateFileUpload(FileUploadDO fileUploadDO, FileUploadExample fileUploadExample);

    /**
     * save FileUpload
     *
     * @param fileUploadDO
     * @return
     */
    Long saveFileUpload(FileUploadDO fileUploadDO);

    /**
     * get FileUpload by uploadId
     *
     * @param uploadId
     * @return
     */
    FileUploadDO getFileUploadByUploadId(Long uploadId);

    /**
     * list FileUpload by example
     *
     * @param example
     * @return
     */
    List<FileUploadDO> getFileUploadsByExample(FileUploadExample example);

    /**
     * query FileUpload
     *
     * @param example
     * @return
     */
    PageResult<FileUploadDO> queryFileUpload(FileUploadExample example);

    /**
     * remove FileUpload by uploadId
     *
     * @param uploadId
     * @return
     */
    Boolean removeFileUpload(Long uploadId);

    /**
     * update FileUpload by uploadId
     *
     * @param uploadDO
     * @return
     */
    Boolean updateFileUploadByPrimaryKey(FileUploadDO uploadDO);

    List<FileUploadDO> getUploadsByStatusInTime(List<UploadStatusEnum> statusList, List<UploadSubStatusEnum> subStatusEnums, Date deadline);

    void resetCreateTimeForFailedUploads(String appCode);
}
