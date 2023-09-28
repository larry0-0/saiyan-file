package co.mgentertainment.file.dal.mapper;

import co.mgentertainment.file.dal.po.ResourceDO;
import co.mgentertainment.file.dal.po.ResourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ResourceMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    long countByExample(ResourceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    int deleteByExample(ResourceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    int deleteByPrimaryKey(Long rid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    int insert(ResourceDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    int insertSelective(ResourceDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    List<ResourceDO> selectByExample(ResourceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    ResourceDO selectByPrimaryKey(Long rid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    int updateByExampleSelective(@Param("row") ResourceDO row, @Param("example") ResourceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    int updateByExample(@Param("row") ResourceDO row, @Param("example") ResourceExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    int updateByPrimaryKeySelective(ResourceDO row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table resource
     *
     * @mbg.generated Thu Sep 28 17:22:56 GST 2023
     */
    int updateByPrimaryKey(ResourceDO row);
}