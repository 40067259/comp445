package com.concordia.httpfs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class HttpfsApplication {

    public static void main(String[] args) throws IOException {

        SpringApplication.run(HttpfsApplication.class, args);

        int port = 8088;
        for (String arg : args)
            port = Integer.parseInt(arg);
        TcpServer server = new TcpServer(port);

        server.serverGetConnection();

    }

}
