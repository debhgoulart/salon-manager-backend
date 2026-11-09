package com.manager.salon.modules.client.mapper;

import com.manager.salon.modules.client.dto.ClientCreateRequest;
import com.manager.salon.modules.client.dto.ClientResponse;
import com.manager.salon.modules.client.dto.ClientUpdateRequest;
import com.manager.salon.modules.client.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client toEntity(ClientCreateRequest request);

    ClientResponse toResponse(Client entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ClientUpdateRequest request, @MappingTarget Client entity);
}
