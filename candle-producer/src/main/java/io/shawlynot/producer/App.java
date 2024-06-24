package io.shawlynot.producer;


import io.shawlynot.core.util.SpringShutdownPreventer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public SpringShutdownPreventer preventSpringShutdown() {
        return SpringShutdownPreventer.get();
    }

}
