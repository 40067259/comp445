package com.concordia.httpfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class HttpfsApplication {

    public static void main(String[] args) throws IOException {

        SpringApplication.run(HttpfsApplication.class, args);

        System.out.println("Server has started...");

        TcpServer server = new TcpServer(8088);
        while(true){
            server.serverGetConnection();
        }

    }

}
