package com.manager.salon.modules.commission.service;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.appointment.entity.Appointment;
import com.manager.salon.modules.commission.dto.CommissionResponse;
import com.manager.salon.modules.commission.entity.Commission;
import com.manager.salon.modules.commission.entity.CommissionStatus;
import com.manager.salon.modules.commission.mapper.CommissionMapper;
import com.manager.salon.modules.commission.repository.CommissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommissionCalculatorService {

    private final CommissionRepository commissionRepository;
    private final CommissionMapper commissionMapper;

    public CommissionCalculatorService(CommissionRepository commissionRepository, CommissionMapper commissionMapper) {
        this.commissionRepository = commissionRepository;
        this.commissionMapper = commissionMapper;
    }

    @Transactional
    public Commission calculateAndRecordCommission(Appointment appointment) {
        Optional<Commission> existing = commissionRepository.findByAppointmentId(appointment.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        BigDecimal serviceAmount = appointment.getEstimatedPrice();
        BigDecimal appliedRate;

        // Regra de Precedência: Taxa do serviço sobrescreve a taxa padrão do funcionário
        if (appointment.getService().getCustomCommissionRate() != null) {
            appliedRate = appointment.getService().getCustomCommissionRate();
        } else {
            appliedRate = appointment.getEmployee().getDefaultCommissionRate();
        }

        BigDecimal commissionAmount = serviceAmount
                .multiply(appliedRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);

        BigDecimal salonAmount = serviceAmount.subtract(commissionAmount);

        Commission commission = new Commission(
                appointment,
                appointment.getEmployee(),
                null,
                appliedRate,
                serviceAmount,
                commissionAmount,
                salonAmount,
                CommissionStatus.PENDING
        );

        return commissionRepository.save(commission);
    }

    @Transactional
    public CommissionResponse markAsPaid(UUID commissionId) {
        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Comissão não encontrada com o ID: " + commissionId));

        if (commission.getStatus() == CommissionStatus.CANCELLED) {
            throw new BusinessException("Não é possível pagar uma comissão cancelada.");
        }

        commission.setStatus(CommissionStatus.PAID);
        Commission updated = commissionRepository.save(commission);
        return commissionMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public CommissionResponse findById(UUID id) {
        Commission commission = commissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comissão não encontrada com o ID: " + id));
        return commissionMapper.toResponse(commission);
    }

    @Transactional(readOnly = true)
    public Page<CommissionResponse> findAll(UUID employeeId, CommissionStatus status, OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable) {
        return commissionRepository.findAllWithFilters(employeeId, status, startDate, endDate, pageable)
                .map(commissionMapper::toResponse);
    }

    @Transactional
    public void cancelCommissionByAppointment(UUID appointmentId) {
        commissionRepository.findByAppointmentId(appointmentId).ifPresent(commission -> {
            commission.setStatus(CommissionStatus.CANCELLED);
            commissionRepository.save(commission);
        });
    }
}
