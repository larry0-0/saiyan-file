package co.mgentertainment.file.dal.repository;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.common.model.media.ResourceTypeEnum;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;

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
    Map<String, Long> batchAddFileUpload(List<String> filenames, ResourceTypeEnum resourceTypeEnum, String appCode);

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
}
