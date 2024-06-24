package io.shawlynot.producer;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled //can be re-enabled if a local kafka is running
public class AppTest {

    @Test
    void applicationStarts() {
        System.out.println("TEST");
    }
}
