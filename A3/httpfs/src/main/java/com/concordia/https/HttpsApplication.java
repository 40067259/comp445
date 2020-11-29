package com.concordia.https;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class HttpsApplication {

    public static void main(String[] args) throws IOException {

        SpringApplication.run(HttpsApplication.class, args);

        System.out.println("\nStarting a server...");

        Server server = new Server();

    }

}
