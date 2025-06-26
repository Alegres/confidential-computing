package com.demo.confidential.users;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(value = "backend.security")
public class Users {
    public static final String USER = "USER";
    private List<User> users = new ArrayList<>();

    /**
     *
     */
    @Data
    public static class User {
        private String username;
        private String password;
        private Set<String> roles = new HashSet<>();
    }
}
