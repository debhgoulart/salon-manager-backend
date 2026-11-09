package com.manager.salon.modules.service.service;

import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.service.dto.ServiceCreateRequest;
import com.manager.salon.modules.service.dto.ServiceResponse;
import com.manager.salon.modules.service.dto.ServiceUpdateRequest;
import com.manager.salon.modules.service.entity.ServiceEntity;
import com.manager.salon.modules.service.mapper.ServiceMapper;
import com.manager.salon.modules.service.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    public ServiceManagementService(ServiceRepository serviceRepository, ServiceMapper serviceMapper) {
        this.serviceRepository = serviceRepository;
        this.serviceMapper = serviceMapper;
    }

    @Transactional
    public ServiceResponse create(ServiceCreateRequest request) {
        ServiceEntity entity = serviceMapper.toEntity(request);
        ServiceEntity saved = serviceRepository.save(entity);
        return serviceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ServiceResponse findById(UUID id) {
        ServiceEntity entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o ID: " + id));
        return serviceMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> findAll(Boolean active, String name, Pageable pageable) {
        return serviceRepository.searchServices(active, name, pageable)
                .map(serviceMapper::toResponse);
    }

    @Transactional
    public ServiceResponse update(UUID id, ServiceUpdateRequest request) {
        ServiceEntity entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o ID: " + id));

        serviceMapper.updateEntityFromDto(request, entity);
        ServiceEntity updated = serviceRepository.save(entity);
        return serviceMapper.toResponse(updated);
    }

    @Transactional
    public void deactivate(UUID id) {
        ServiceEntity entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o ID: " + id));
        entity.setActive(false);
        serviceRepository.save(entity);
    }
}
