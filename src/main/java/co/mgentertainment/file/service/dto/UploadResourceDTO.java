package co.mgentertainment.file.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author larry
 * @createTime 2023/9/23
 * @description UploadResourceDTO
 */
@Data
@Builder
public class UploadResourceDTO {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column resource.rid
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    private Long rid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column resource.filename
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    private String filename;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column resource.folder
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    private String folder;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column resource.size
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    private BigDecimal size;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column resource.duration
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    private Integer duration;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column resource.app_code
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    private String appCode;

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
     * This field corresponds to the database column file_upload.title
     *
     * @mbg.generated Tue Oct 31 06:16:47 GST 2023
     */
    private String title;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column resource.type
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
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
}
