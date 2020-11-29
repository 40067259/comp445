package com.concordia;

import com.concordia.httpc.ResponseAndPrint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class HttpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpcApplication.class, args);
        ResponseAndPrint responseAndPrint = new ResponseAndPrint();

        responseAndPrint.parse();

    }

}
