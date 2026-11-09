package com.manager.salon.modules.employee;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.auth.repository.UserRepository;
import com.manager.salon.modules.employee.dto.EmployeeCreateRequest;
import com.manager.salon.modules.employee.dto.EmployeeResponse;
import com.manager.salon.modules.employee.dto.EmployeeUpdateRequest;
import com.manager.salon.modules.employee.dto.UpdateCommissionRateRequest;
import com.manager.salon.modules.employee.entity.Employee;
import com.manager.salon.modules.employee.mapper.EmployeeMapper;
import com.manager.salon.modules.employee.repository.EmployeeRepository;
import com.manager.salon.modules.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        employee = new Employee(
                null,
                "Carlos Souza",
                "12345678901",
                "11987654321",
                "carlos@salon.com",
                "Cabeleireiro",
                LocalDate.now().minusMonths(6),
                new BigDecimal("40.00"),
                true
        );
        employee.setId(employeeId);
    }

    @Test
    @DisplayName("Deve cadastrar colaborador sem login com sucesso")
    void shouldCreateEmployeeWithoutLogin() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Carlos Souza",
                "12345678901",
                "11987654321",
                "carlos@salon.com",
                "Cabeleireiro",
                LocalDate.now().minusMonths(6),
                new BigDecimal("40.00"),
                false,
                null
        );

        when(employeeRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(employeeMapper.toEntity(request)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(new EmployeeResponse(
                employeeId, null, "Carlos Souza", "12345678901", "11987654321",
                "carlos@salon.com", "Cabeleireiro", LocalDate.now().minusMonths(6),
                new BigDecimal("40.00"), true, null, null
        ));

        EmployeeResponse response = employeeService.create(request);

        assertNotNull(response);
        assertEquals("Carlos Souza", response.name());
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com CPF duplicado")
    void shouldThrowExceptionWhenCpfIsDuplicate() {
        EmployeeCreateRequest request = new EmployeeCreateRequest(
                "Carlos Souza",
                "12345678901",
                "11987654321",
                "carlos@salon.com",
                "Cabeleireiro",
                LocalDate.now().minusMonths(6),
                new BigDecimal("40.00"),
                false,
                null
        );

        when(employeeRepository.existsByCpf(request.cpf())).thenReturn(true);

        assertThrows(BusinessException.class, () -> employeeService.create(request));
    }

    @Test
    @DisplayName("Deve atualizar taxa de comissão padrão do colaborador")
    void shouldUpdateCommissionRate() {
        UpdateCommissionRateRequest request = new UpdateCommissionRateRequest(new BigDecimal("50.00"));

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponse(employee)).thenReturn(new EmployeeResponse(
                employeeId, null, "Carlos Souza", "12345678901", "11987654321",
                "carlos@salon.com", "Cabeleireiro", LocalDate.now().minusMonths(6),
                new BigDecimal("50.00"), true, null, null
        ));

        EmployeeResponse response = employeeService.updateCommissionRate(employeeId, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("50.00"), employee.getDefaultCommissionRate());
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Deve desativar colaborador logicamente")
    void shouldDeactivateEmployee() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        employeeService.deactivate(employeeId);

        assertFalse(employee.isActive());
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar colaborador inexistente")
    void shouldThrowNotFoundWhenEmployeeDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.findById(nonExistentId));
    }
}
