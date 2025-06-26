package com.demo.confidential.application.client.controller;

import java.util.List;

import com.demo.confidential.domain.measurement.model.ClientDO;
import com.demo.confidential.domain.measurement.service.ClientsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("client/v1")
class ClientController {
    private static final Logger LOGGER = LogManager.getLogger(ClientController.class);

    private final ClientsService service;

    @Autowired
    public ClientController(final ClientsService service) {
        this.service = service;
    }

    @PostMapping
    public ClientDTO createClient(@RequestBody final ClientDTO client) {
        return toDTO(service.save(toDomain(client)));
    }

    @GetMapping
    public List<ClientDTO> getAllClients() {
        return service.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private ClientDO toDomain(final ClientDTO client) {
        final ClientDO clientDO = new ClientDO();
        clientDO.setName(client.getName());
        clientDO.setSurname(client.getSurname());
        clientDO.setId(client.getId());
        return clientDO;
    }

    private ClientDTO toDTO(final ClientDO clientDO) {
        final ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName(clientDO.getName());
        clientDTO.setSurname(clientDO.getSurname());
        clientDTO.setId(clientDO.getId());
        return clientDTO;
    }
}
