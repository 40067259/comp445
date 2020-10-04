package com.concordia.httpc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Httpc {
    private String request;
    private String response;
    private Map<String, String> headers;
    private String body;
    private String outputFilePath;
    public Httpc(){
        headers = new HashMap<>();
        iniHeaders();
    }

    public void setHeaders(List<String> list){
        if(list != null){
            for(int i = 0; i < list.size(); i++){
                String[] str = list.get(i).split(":");
                headers.put(str[0],str[1]);
            }
        }
    }

    //connect request: connection to send request and get response
    public void getConnection(String[]args) {
        try {
            String para = args[args.length - 1];
            URL url = new URL(para);
            String requestMethod = args[0];
            String path = url.getPath();
            String host = url.getHost();
            String query = url.getQuery();

            Socket socket = new Socket(host, 80);

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            // add request line
            if (query == null) {
                request = requestMethod.toUpperCase() + " " + path + " HTTP/1.0\r\n\r\n";
            } else {
                request = requestMethod.toUpperCase() + " " + path + "?" + query + " HTTP/1.0\r\n\r\n";
            }
            // add headers
            request += addHeaders();
            //send the request through socket
            outputStream.write(request.getBytes());
            outputStream.flush();

            //get the response
            StringBuilder response = new StringBuilder();

            int data = inputStream.read();

            // redirect
            /*

            if (data = 200) {

            } else if (Integer.parseInt(Integer.toString(status).substring(0, 1)) == 3)
            {
                // connect response code with 3XX
                if (Integer.parseInt(Integer.toString(data).substring(0, 1)) == 3)
                {
                    System.out.println("Redirecting to new url...");

                    String newUrl = connection.getHeaderField("Location");

                    System.out.println("The new URL is now " + newUrl);

                    ResponseAndPrint responseAndPrint = new ResponseAndPrint();
                    responseAndPrint.parse(args);

                }
            } else  {
                System.out.println("Error " + data);
                System.exit(0);
            }

            */

            while (data != -1) {
                response.append((char) data);
                data = inputStream.read();
            }
            this.response = response.toString();

            if (this.outputFilePath != null) {
                try {
                    FileWriter myWriter = new FileWriter(outputFilePath, false);
                    myWriter.write(this.response);
                    myWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //close the socket
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //add headers to request
    public String addHeaders() {
        StringBuilder headerBuilder = new StringBuilder();

        for (Map.Entry<String, String> header : headers.entrySet()) {
            headerBuilder.append(header.getKey() + ": " + header.getValue() + "\r\n");
        }
        headerBuilder.append("\r\n");

        return headerBuilder.toString();
    }

    //initialize headers
    public void iniHeaders() {
        this.headers.put("Host", "httpbin.org");
        this.headers.put("User-Agent", "Concordia-HTTP/1.0");
        this.headers.put("Accept-Language", "en-us,en;q=0.5");
        this.headers.put("Accept-Encoding", "gzip, deflate");
    }
    //setHeader
    public void setHeader(String k, String v){
        this.headers.put(k,v);
    }

    public String getRequest() {
        return request;
    }

    public String getResponse() {
        return response;
    }

    public String getBody() {
        return body;
    }
    public void setBody(String body){
        this.body = body;
    }

    //pick up body part from response and assign it to body
    public void pickBody() {
        String[] str = response.split("\r\n\r\n");
        body = str[1];
    }
    //post

    // output to file
    public void setOutputFile(String outputFilePath) { this.outputFilePath = outputFilePath; }
}
