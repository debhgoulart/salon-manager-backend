package com.manager.salon.modules.employee.service;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.auth.entity.User;
import com.manager.salon.modules.auth.entity.UserRole;
import com.manager.salon.modules.auth.repository.UserRepository;
import com.manager.salon.modules.employee.dto.EmployeeCreateRequest;
import com.manager.salon.modules.employee.dto.EmployeeResponse;
import com.manager.salon.modules.employee.dto.EmployeeUpdateRequest;
import com.manager.salon.modules.employee.dto.UpdateCommissionRateRequest;
import com.manager.salon.modules.employee.entity.Employee;
import com.manager.salon.modules.employee.mapper.EmployeeMapper;
import com.manager.salon.modules.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            EmployeeMapper employeeMapper,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest request) {
        if (employeeRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("Já existe um colaborador cadastrado com o CPF: " + request.cpf());
        }

        Employee employee = employeeMapper.toEntity(request);

        if (Boolean.TRUE.equals(request.createLoginUser())) {
            if (request.password() == null || request.password().isBlank()) {
                throw new BusinessException("A senha é obrigatória para criar o usuário de acesso do colaborador.");
            }
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException("Já existe um usuário no sistema com o e-mail: " + request.email());
            }

            User user = new User(
                    request.name(),
                    request.email(),
                    passwordEncoder.encode(request.password()),
                    UserRole.ROLE_EMPLOYEE,
                    true
            );
            User savedUser = userRepository.save(user);
            employee.setUserId(savedUser.getId());
        }

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado com o ID: " + id));
        return employeeMapper.toResponse(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> findAll(Boolean active, String name, Pageable pageable) {
        return employeeRepository.findAllWithFilters(active, name, pageable)
                .map(employeeMapper::toResponse);
    }

    @Transactional
    public EmployeeResponse update(UUID id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado com o ID: " + id));

        if (employeeRepository.existsByCpfAndIdNot(request.cpf(), id)) {
            throw new BusinessException("O CPF " + request.cpf() + " já está em uso por outro colaborador.");
        }

        employeeMapper.updateEntityFromDto(request, employee);
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toResponse(updated);
    }

    @Transactional
    public EmployeeResponse updateCommissionRate(UUID id, UpdateCommissionRateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado com o ID: " + id));

        employee.setDefaultCommissionRate(request.defaultCommissionRate());
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toResponse(updated);
    }

    @Transactional
    public void deactivate(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado com o ID: " + id));

        employee.setActive(false);
        employeeRepository.save(employee);

        if (employee.getUserId() != null) {
            userRepository.findById(employee.getUserId()).ifPresent(user -> {
                user.setActive(false);
                userRepository.save(user);
            });
        }
    }
}
