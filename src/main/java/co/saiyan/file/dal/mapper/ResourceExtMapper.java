package co.saiyan.file.dal.mapper;

import co.saiyan.file.dal.po.ResourceDO;
import co.saiyan.file.dal.po.ResourceExtDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ResourceExtMapper {
    ResourceExtDO selectByUploadId(Long uploadId);

    List<ResourceDO> selectByUploadIds(@Param("uploadIds") List<Long> uploadIds);
}