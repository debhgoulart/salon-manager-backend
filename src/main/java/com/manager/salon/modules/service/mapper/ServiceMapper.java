package com.manager.salon.modules.service.mapper;

import com.manager.salon.modules.service.dto.ServiceCreateRequest;
import com.manager.salon.modules.service.dto.ServiceResponse;
import com.manager.salon.modules.service.dto.ServiceUpdateRequest;
import com.manager.salon.modules.service.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ServiceEntity toEntity(ServiceCreateRequest request);

    ServiceResponse toResponse(ServiceEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ServiceUpdateRequest request, @MappingTarget ServiceEntity entity);
}
