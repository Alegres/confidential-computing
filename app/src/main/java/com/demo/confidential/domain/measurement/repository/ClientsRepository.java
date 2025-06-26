package com.demo.confidential.domain.measurement.repository;

import java.util.List;

import com.demo.confidential.domain.measurement.model.ClientDO;

public interface ClientsRepository {
    void save(ClientDO measurement);

    List<ClientDO> findAll();
}
