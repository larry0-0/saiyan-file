package co.mgentertainment.file.dal.mapper;

import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.FileUploadExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileUploadMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    long countByExample(FileUploadExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    int deleteByExample(FileUploadExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    int deleteByPrimaryKey(Long uploadId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    int insert(FileUploadDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    int insertSelective(FileUploadDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    List<FileUploadDO> selectByExample(FileUploadExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    FileUploadDO selectByPrimaryKey(Long uploadId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    int updateByExampleSelective(@Param("row") FileUploadDO row, @Param("example") FileUploadExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    int updateByExample(@Param("row") FileUploadDO row, @Param("example") FileUploadExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:50 GST 2023
     */
    int updateByPrimaryKeySelective(FileUploadDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table file_upload
     *
     * @mbg.generated Fri Sep 22 01:39:51 GST 2023
     */
    int updateByPrimaryKey(FileUploadDO row);
}