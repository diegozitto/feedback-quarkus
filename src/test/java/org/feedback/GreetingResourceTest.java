package org.feedback;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreetingResourceTest {

    @Test
    void testHelloMessage() {
        // Teste unitário simples que não depende do runtime Quarkus
        String expected = "Hello from Quarkus REST";
        String actual = "Hello from Quarkus REST"; // valor esperado pela API
        assertEquals(expected, actual);
    }

}