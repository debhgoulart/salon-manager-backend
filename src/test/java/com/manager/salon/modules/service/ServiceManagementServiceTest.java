package com.manager.salon.modules.service;

import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.service.dto.ServiceCreateRequest;
import com.manager.salon.modules.service.dto.ServiceResponse;
import com.manager.salon.modules.service.entity.ServiceEntity;
import com.manager.salon.modules.service.mapper.ServiceMapper;
import com.manager.salon.modules.service.repository.ServiceRepository;
import com.manager.salon.modules.service.service.ServiceManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceManagementServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceMapper serviceMapper;

    @InjectMocks
    private ServiceManagementService serviceManagementService;

    private ServiceEntity serviceEntity;
    private UUID serviceId;

    @BeforeEach
    void setUp() {
        serviceId = UUID.randomUUID();
        serviceEntity = new ServiceEntity(
                "Corte Feminino",
                "Descrição do corte",
                new BigDecimal("120.00"),
                45,
                new BigDecimal("50.00"),
                true
        );
        serviceEntity.setId(serviceId);
    }

    @Test
    @DisplayName("Deve criar serviço com sucesso")
    void shouldCreateServiceSuccessfully() {
        ServiceCreateRequest request = new ServiceCreateRequest(
                "Corte Feminino",
                "Descrição do corte",
                new BigDecimal("120.00"),
                45,
                new BigDecimal("50.00")
        );

        when(serviceMapper.toEntity(request)).thenReturn(serviceEntity);
        when(serviceRepository.save(serviceEntity)).thenReturn(serviceEntity);
        when(serviceMapper.toResponse(serviceEntity)).thenReturn(new ServiceResponse(
                serviceId, "Corte Feminino", "Descrição do corte", new BigDecimal("120.00"), 45, new BigDecimal("50.00"), true, null, null
        ));

        ServiceResponse response = serviceManagementService.create(request);

        assertNotNull(response);
        assertEquals("Corte Feminino", response.name());
        verify(serviceRepository).save(serviceEntity);
    }

    @Test
    @DisplayName("Deve desativar serviço logicamente")
    void shouldDeactivateService() {
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceEntity));

        serviceManagementService.deactivate(serviceId);

        assertFalse(serviceEntity.isActive());
        verify(serviceRepository).save(serviceEntity);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para serviço inexistente")
    void shouldThrowNotFoundWhenServiceDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(serviceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceManagementService.findById(nonExistentId));
    }
}
