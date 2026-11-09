package com.manager.salon.modules.client.service;

import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.client.dto.ClientCreateRequest;
import com.manager.salon.modules.client.dto.ClientResponse;
import com.manager.salon.modules.client.dto.ClientUpdateRequest;
import com.manager.salon.modules.client.entity.Client;
import com.manager.salon.modules.client.mapper.ClientMapper;
import com.manager.salon.modules.client.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    @Transactional
    public ClientResponse create(ClientCreateRequest request) {
        Client client = clientMapper.toEntity(request);
        Client saved = clientRepository.save(client);
        return clientMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        return clientMapper.toResponse(client);
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> findAll(Boolean active, String query, Pageable pageable) {
        return clientRepository.searchClients(active, query, pageable)
                .map(clientMapper::toResponse);
    }

    @Transactional
    public ClientResponse update(UUID id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));

        clientMapper.updateEntityFromDto(request, client);
        Client updated = clientRepository.save(client);
        return clientMapper.toResponse(updated);
    }

    @Transactional
    public void deactivate(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        client.setActive(false);
        clientRepository.save(client);
    }
}
