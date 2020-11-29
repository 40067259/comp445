package com.concordia.udp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Httpc {
    private String requestBody;
    private String request;
    private String response;
    private Map<String, String> headers;
    private String body;
    private InetSocketAddress inetSocketAddr;
    private UDPClient udpClient;
    private Boolean isConnected;

    public Httpc(){
        body ="";
        requestBody="";
        headers = new HashMap<>();
        iniHeaders();
        udpClient = null;
        isConnected = false;
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
        if (isConnected){
            try {
                String para = args[args.length - 1];
                URL url = new URL(para);
                String requestMethod = args[0];
                String path = url.getPath();
                String host = url.getHost();
                String query = url.getQuery();
                int port = url.getPort();
                if (port == -1) port = url.getDefaultPort();
                Socket socket = new Socket(host, port);
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                // add request line
                if (query == null) {
                    request = requestMethod.toUpperCase() + " " + "http://" + host + ":" + port + path + " HTTP/1.0\r\n\r\n";
                } else {
                    request = requestMethod.toUpperCase() + " " + "http://" + host + ":" + port + path + "?" + query + " HTTP/1.0\r\n\r\n";
                }

                // add headers
                request += addHeaders();
                request += requestBody;
                //System.out.println(request);

                inetSocketAddr = new InetSocketAddress(host,port);
                response = udpClient.runClient(inetSocketAddr, request);

                //send the request through socket
                outputStream.write(request.getBytes());
                outputStream.flush();

                //get the response
                StringBuilder response = new StringBuilder();

                int data = inputStream.read();

                while (data != -1) {
                    response.append((char) data);
                    data = inputStream.read();
                }
                this.response = response.toString();
                //close the socket
                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            udpClient = new UDPClient(41830);
            isConnected = true;
        }
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public UDPClient getUdpClient() {
        return udpClient;
    }

    public void setResponse(String response) {
        this.response = response;
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
        this.headers.put("Host", "http://localhost:8088/");
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

    public void setRequestBody(String requestBody){this.requestBody = requestBody;}

    //pick up body part from response and assign it to body
    public void pickBody() {
        if(response != null) {
            String[] str = response.split("\r\n\r\n");
            if(str.length > 1)
            body = str[str.length - 1];
        }

    }

}
