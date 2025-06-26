package com.demo.confidential.domain.measurement.service;

import java.util.List;
import java.util.UUID;

import com.demo.confidential.domain.measurement.model.ClientDO;
import com.demo.confidential.domain.measurement.repository.ClientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientsService {
    private final ClientsRepository repository;

    public ClientDO save(final ClientDO clientDO) {
        clientDO.setId(UUID.randomUUID().toString());
        repository.save(clientDO);
        return clientDO;
    }

    public List<ClientDO> findAll() {
        return repository.findAll();
    }
}
