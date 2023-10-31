package co.mgentertainment.file.dal.mapper;

import co.mgentertainment.file.dal.po.ResourceExtDO;

public interface ResourceExtMapper {
    ResourceExtDO selectByUploadId(Long uploadId);
}