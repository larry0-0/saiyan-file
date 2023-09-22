package co.mgentertainment.file.dal.repository;

import co.mgentertainment.common.model.PageResult;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;

import java.util.List;

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
     * @param uploadDO
     * @return
     */
    Boolean updateFileUploadByPrimaryKey(FileUploadDO uploadDO);
}
