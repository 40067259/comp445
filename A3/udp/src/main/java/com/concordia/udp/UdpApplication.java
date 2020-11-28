package com.concordia.udp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class UdpApplication {

    public static void main(String[] args) throws IOException {

        SpringApplication.run(UdpApplication.class, args);

        String request = "";
        for(String str: args){
            request += str;
        }

        UDPClient client = new UDPClient();
        client.runClient(client.serverAddr,request);


    }

}
