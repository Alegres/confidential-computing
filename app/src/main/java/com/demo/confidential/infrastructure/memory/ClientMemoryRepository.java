package com.demo.confidential.infrastructure.memory;

import java.util.ArrayList;
import java.util.List;

import com.demo.confidential.domain.measurement.model.ClientDO;
import com.demo.confidential.domain.measurement.repository.ClientsRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ClientMemoryRepository implements ClientsRepository {
    private final List<ClientEntity> clients = new ArrayList<>();

    @Override
    public void save(final ClientDO clientDO) {
        clients.add(toEntity(clientDO));
    }

    @Override
    public List<ClientDO> findAll() {
        return clients.stream()
                .map(this::toDO)
                .toList();
    }

    private ClientDO toDO(final ClientEntity clientEntity) {
        final ClientDO clientDO = new ClientDO();
        clientDO.setId(clientEntity.getId());
        clientDO.setName(clientEntity.getName());
        clientDO.setSurname(clientEntity.getSurname());
        return clientDO;
    }

    private ClientEntity toEntity(final ClientDO clientDO) {
        final ClientEntity clientEntity = new ClientEntity();
        clientEntity.setId(clientDO.getId());
        clientEntity.setName(clientDO.getName());
        clientEntity.setSurname(clientDO.getSurname());
        return clientEntity;
    }
}
