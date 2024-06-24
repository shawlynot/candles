package io.shawlynot.producer;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Can be re-enabled if a local kafka is running. In a production app, I would use an in-memory kafka for a test like
 * this.
 */
@SpringBootTest
@Disabled
public class AppTest {

    @Test
    void applicationStarts() {
        System.out.println("TEST");
    }
}


