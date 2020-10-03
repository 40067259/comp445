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

    public static void main(String[] args) throws MalformedURLException {
        try {
            BufferedReader in = new BufferedReader(new FileReader("../resources/static/files/content.txt"));
            String str ="niu a niu a";
            while ((str = in.readLine()) != null) {
                System.out.println(str);
            }
            System.out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResponseAndPrint responseAndPrint = new ResponseAndPrint();
        SpringApplication.run(HttpcApplication.class, args);
        if(responseAndPrint.isNoConnection(args))
            responseAndPrint.handleNoConnection(args);
        // no -v and no -h
        else if(responseAndPrint.isNoVerboseConnection(args))
            responseAndPrint.handleNoVerboseConnection(args);
        if(responseAndPrint.isGetVConnection(args)) responseAndPrint.handleVConnection(args);
        if(responseAndPrint.isGetHConnection(args)) responseAndPrint.handleHConnection(args);
        if(responseAndPrint.isDAddBody(args)) responseAndPrint.handleDAddBody(args);

        /**
        System.out.println(args[0]);
        String param = args[args.length - 1];
        //String param = "http://httpbin.org/get?course=networking&assignment=1";
        URL url = new URL(param);
        System.out.println("********************");
        System.out.println(param);
        System.out.println(url.getQuery());
        System.out.println("********************");
        Httpc httpc = new Httpc();
        httpc.getConnection(args[0], url.getPath(), url.getHost(), url.getQuery());**/



    }

}
