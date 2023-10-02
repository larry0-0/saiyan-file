package co.mgentertainment.file.service.converter;

import co.mgentertainment.file.dal.po.AccessClientDO;
import co.mgentertainment.file.dal.po.FileUploadDO;
import co.mgentertainment.file.dal.po.ResourceDO;
import co.mgentertainment.file.dal.po.ResourceLineDO;
import co.mgentertainment.file.service.dto.AccessClientDTO;
import co.mgentertainment.file.service.dto.FileUploadDTO;
import co.mgentertainment.file.service.dto.ResourceDTO;
import co.mgentertainment.file.service.dto.ResourceLineDTO;
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
}
