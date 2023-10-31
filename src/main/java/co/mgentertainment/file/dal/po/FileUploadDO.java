package co.mgentertainment.file.dal.po;

import java.util.Date;

public class FileUploadDO {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.upload_id
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Long uploadId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.filename
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private String filename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.type
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Short type;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.status
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Short status;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.sub_status
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Short subStatus;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.app_code
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private String appCode;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.rid
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Long rid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.has_trailer
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Byte hasTrailer;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.has_short
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Byte hasShort;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.has_cover
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Byte hasCover;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.trailer_duration
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Integer trailerDuration;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.short_duration
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Integer shortDuration;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.trailer_start_pos
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Integer trailerStartPos;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.short_start_pos
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Integer shortStartPos;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.create_time
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Date createTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.updated_time
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Date updatedTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column file_upload.deleted
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private Byte deleted;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.upload_id
     *
     * @return the value of file_upload.upload_id
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.sub_status
     *
     * @return the value of file_upload.sub_status
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public Short getSubStatus() {
        return subStatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.sub_status
     *
     * @param subStatus the value for file_upload.sub_status
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setSubStatus(Short subStatus) {
        this.subStatus = subStatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.app_code
     *
     * @return the value of file_upload.app_code
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.app_code
     *
     * @param appCode the value for file_upload.app_code
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setAppCode(String appCode) {
        this.appCode = appCode == null ? null : appCode.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.rid
     *
     * @return the value of file_upload.rid
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setRid(Long rid) {
        this.rid = rid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.has_trailer
     *
     * @return the value of file_upload.has_trailer
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public Byte getHasTrailer() {
        return hasTrailer;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.has_trailer
     *
     * @param hasTrailer the value for file_upload.has_trailer
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setHasTrailer(Byte hasTrailer) {
        this.hasTrailer = hasTrailer;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.has_short
     *
     * @return the value of file_upload.has_short
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public Byte getHasShort() {
        return hasShort;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.has_short
     *
     * @param hasShort the value for file_upload.has_short
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setHasShort(Byte hasShort) {
        this.hasShort = hasShort;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.has_cover
     *
     * @return the value of file_upload.has_cover
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public Byte getHasCover() {
        return hasCover;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.has_cover
     *
     * @param hasCover the value for file_upload.has_cover
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setHasCover(Byte hasCover) {
        this.hasCover = hasCover;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.trailer_duration
     *
     * @return the value of file_upload.trailer_duration
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public Integer getTrailerDuration() {
        return trailerDuration;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.trailer_duration
     *
     * @param trailerDuration the value for file_upload.trailer_duration
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setTrailerDuration(Integer trailerDuration) {
        this.trailerDuration = trailerDuration;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.short_duration
     *
     * @return the value of file_upload.short_duration
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public Integer getShortDuration() {
        return shortDuration;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.short_duration
     *
     * @param shortDuration the value for file_upload.short_duration
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setShortDuration(Integer shortDuration) {
        this.shortDuration = shortDuration;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.trailer_start_pos
     *
     * @return the value of file_upload.trailer_start_pos
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public Integer getTrailerStartPos() {
        return trailerStartPos;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.trailer_start_pos
     *
     * @param trailerStartPos the value for file_upload.trailer_start_pos
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setTrailerStartPos(Integer trailerStartPos) {
        this.trailerStartPos = trailerStartPos;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.short_start_pos
     *
     * @return the value of file_upload.short_start_pos
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public Integer getShortStartPos() {
        return shortStartPos;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column file_upload.short_start_pos
     *
     * @param shortStartPos the value for file_upload.short_start_pos
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setShortStartPos(Integer shortStartPos) {
        this.shortStartPos = shortStartPos;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column file_upload.create_time
     *
     * @return the value of file_upload.create_time
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    public void setDeleted(Byte deleted) {
        this.deleted = deleted;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
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
        sb.append(", subStatus=").append(subStatus);
        sb.append(", appCode=").append(appCode);
        sb.append(", rid=").append(rid);
        sb.append(", hasTrailer=").append(hasTrailer);
        sb.append(", hasShort=").append(hasShort);
        sb.append(", hasCover=").append(hasCover);
        sb.append(", trailerDuration=").append(trailerDuration);
        sb.append(", shortDuration=").append(shortDuration);
        sb.append(", trailerStartPos=").append(trailerStartPos);
        sb.append(", shortStartPos=").append(shortStartPos);
        sb.append(", createTime=").append(createTime);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", deleted=").append(deleted);
        sb.append("]");
        return sb.toString();
    }
}