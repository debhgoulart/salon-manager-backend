package com.manager.salon.modules.client;

import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.modules.client.dto.ClientCreateRequest;
import com.manager.salon.modules.client.dto.ClientResponse;
import com.manager.salon.modules.client.entity.Client;
import com.manager.salon.modules.client.mapper.ClientMapper;
import com.manager.salon.modules.client.repository.ClientRepository;
import com.manager.salon.modules.client.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        client = new Client("Ana Clara Lima", "11912345678", "ana@email.com", LocalDate.now(), "Nota", true);
        client.setId(clientId);
    }

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void shouldCreateClientSuccessfully() {
        ClientCreateRequest request = new ClientCreateRequest("Ana Clara Lima", "11912345678", "ana@email.com", "Nota");

        when(clientMapper.toEntity(request)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponse(client)).thenReturn(new ClientResponse(
                clientId, "Ana Clara Lima", "11912345678", "ana@email.com", LocalDate.now(), "Nota", true, null, null
        ));

        ClientResponse response = clientService.create(request);

        assertNotNull(response);
        assertEquals("Ana Clara Lima", response.name());
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("Deve desativar cliente logicamente")
    void shouldDeactivateClient() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        clientService.deactivate(clientId);

        assertFalse(client.isActive());
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para cliente inexistente")
    void shouldThrowNotFoundWhenClientDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(clientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(nonExistentId));
    }
}
