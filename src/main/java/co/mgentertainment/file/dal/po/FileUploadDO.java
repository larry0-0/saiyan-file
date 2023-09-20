package co.mgentertainment.file.dal.po;

import java.util.Date;

public class FileUploadDO {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.upload_id
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    private Long uploadId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.filename
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    private String filename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.type
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    private Short type;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.status
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    private Short status;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.rid
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    private Long rid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.create_time
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    private Date createTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.updated_time
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    private Date updatedTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.deleted
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    private Byte deleted;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.upload_id
     *
     * @return the value of file_upload.upload_id
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public Long getUploadId() {
        return uploadId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.upload_id
     *
     * @param uploadId the value for file_upload.upload_id
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public void setUploadId(Long uploadId) {
        this.uploadId = uploadId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.filename
     *
     * @return the value of file_upload.filename
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public String getFilename() {
        return filename;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.filename
     *
     * @param filename the value for file_upload.filename
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public void setFilename(String filename) {
        this.filename = filename == null ? null : filename.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.type
     *
     * @return the value of file_upload.type
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public Short getType() {
        return type;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.type
     *
     * @param type the value for file_upload.type
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public void setType(Short type) {
        this.type = type;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.status
     *
     * @return the value of file_upload.status
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public Short getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.status
     *
     * @param status the value for file_upload.status
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.rid
     *
     * @return the value of file_upload.rid
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public Long getRid() {
        return rid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.rid
     *
     * @param rid the value for file_upload.rid
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public void setRid(Long rid) {
        this.rid = rid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.create_time
     *
     * @return the value of file_upload.create_time
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.create_time
     *
     * @param createTime the value for file_upload.create_time
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.updated_time
     *
     * @return the value of file_upload.updated_time
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.updated_time
     *
     * @param updatedTime the value for file_upload.updated_time
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.deleted
     *
     * @return the value of file_upload.deleted
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public Byte getDeleted() {
        return deleted;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.deleted
     *
     * @param deleted the value for file_upload.deleted
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    public void setDeleted(Byte deleted) {
        this.deleted = deleted;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Wed Sep 20 19:35:26 GST 2023
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", uploadId=").append(uploadId);
        sb.append(", filename=").append(filename);
        sb.append(", type=").append(type);
        sb.append(", status=").append(status);
        sb.append(", rid=").append(rid);
        sb.append(", createTime=").append(createTime);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", deleted=").append(deleted);
        sb.append("]");
        return sb.toString();
    }
}