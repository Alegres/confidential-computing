package com.demo.confidential.domain.measurement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDO {
    private String id;
    private String name;
    private String surname;
}
