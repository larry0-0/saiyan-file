package co.mgentertainment.file.dal.mapper;

import co.mgentertainment.file.dal.po.ResourceDO;
import co.mgentertainment.file.dal.po.ResourceExtDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ResourceExtMapper {
    ResourceExtDO selectByUploadId(Long uploadId);

    List<ResourceDO> selectByUploadIds(@Param("uploadIds") List<Long> uploadIds);
}