package com.example.router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class RouterApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(RouterApplication.class, args);

        Router router = new Router();

        router.recvfrom();
    }

}
