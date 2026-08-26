package com.academia.auth.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;


import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

@TestConfiguration(proxyBeanMethods = false)
@ImportTestcontainers(TestContainers.class)
public class TestContainersConfig {

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                AbandonedConnectionCleanupThread.checkedShutdown();
            } catch (Exception ignorar) {
            
            }
        }, "mysql-cleanup-shutdown-hook"));
    }
}
