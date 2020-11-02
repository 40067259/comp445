package com.concordia;

import com.concordia.httpc.Httpc;
import com.concordia.httpc.ResponseAndPrint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@SpringBootApplication
public class HttpcApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(HttpcApplication.class, args);

        ResponseAndPrint responseAndPrint = new ResponseAndPrint();
        responseAndPrint.parse(args);

    }

}
