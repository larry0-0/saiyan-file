package co.saiyan.file.service.converter;

import co.saiyan.file.dal.po.*;
import co.saiyan.file.service.dto.*;
import co.saiyan.file.dal.po.*;
import co.saiyan.file.service.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author larry
 * @createTime 2023/9/19
 * @description FileObjectMapper
 */
@Mapper
public interface FileObjectMapper {

    FileObjectMapper INSTANCE = Mappers.getMapper(FileObjectMapper.class);

    List<ResourceLineDTO> toResourceLineDTOList(List<ResourceLineDO> resourceLineDOList);

    FileUploadDO toFileUploadDO(FileUploadDTO fileUploadDTO);

    ResourceDO toResourceDO(ResourceDTO resourceDTO);

    List<AccessClientDTO> toAccessClientDTOList(List<AccessClientDO> accessClientDOList);

    UploadResourceDTO toUploadResourceDTO(ResourceExtDO resourceExtDO);
}
