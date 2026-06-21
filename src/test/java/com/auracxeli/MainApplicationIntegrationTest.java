package com.auracxeli;

import org.junit.jupiter.api.Test;

class MainApplicationIntegrationTest extends AbstractIntegrationTest {

    // Wiring check only: confirms the Spring context starts against the
    // Testcontainers MySQL instance. Not a real test of application behavior.
    @Test
    void contextLoads() {
    }
}
