package com.academia.auth.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.mysql.MySQLContainer;

public interface TestContainers {
    
    @Container
    @ServiceConnection
    static MySQLContainer mysql = 
        new MySQLContainer("mysql:8.4")
            .withDatabaseName("academia")
            .withUsername("test")
            .withPassword("test");
}
