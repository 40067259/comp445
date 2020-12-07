package com.concordia.httpc;

import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Httpc {
    private String requestBody;
    private String request;
    private String response;
    private Map<String, String> headers;
    private String body;
    private InetSocketAddress inetSocketAddr = null;
    private UDPClient udpClient;
    private Boolean isConnected;
    private String head;

    public Httpc() {
        body = "";
        requestBody = "";
        headers = new HashMap<>();
        udpClient = null;
        isConnected = false;
        head="";
        iniHeaders();
        //add this part
        udpClient = new UDPClient(41830);
    }

    public void setHeaders(List<String> list) {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                String[] str = list.get(i).split(":");
                headers.put(str[0], str[1]);
            }
        }
    }

    //connect request: connection to send request and get response
    public void getConnection(String[] args) {
        try {
            String para = args[args.length - 1];
            URL url = new URL(para);
            String requestMethod = args[0];
            String path = url.getPath();
            String host = url.getHost();
            String query = url.getQuery();
            int port = url.getPort();
            if (port == -1) port = url.getDefaultPort();

            if (query == null) {
                request = requestMethod.toUpperCase() + " " + "http://" + host + ":" + port + path + " HTTP/1.0\r\n\r\n";//remove HTTP/1.0

            } else {
                request = requestMethod.toUpperCase() + " " + "http://" + host + ":" + port + path + "?" + query + " HTTP/1.0\r\n\r\n";
            }

            // TODO check and maybe remove below 2 lines especially addHeaders()

            addHeaders();
            request = request + head;
            request = request+requestBody;
          //  System.out.println("request2********************>>"+request);

           // request += addHeaders();
            //request += requestBody;

            if (inetSocketAddr == null)
                inetSocketAddr = new InetSocketAddress(host, port);
            //here is the key codes to run a udp
            response = udpClient.runClient(inetSocketAddr, request);
            System.out.println("===Response from the server===");
            System.out.println(response);
           // System.out.println("================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setConnection() {
        udpClient = new UDPClient(41830);
        isConnected = true;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }

    public UDPClient getUdpClient() {
        return udpClient;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    //add headers to request
    public void addHeaders() {

        for (Map.Entry<String, String> header : headers.entrySet()) {

            head += header.getKey()+": "+header.getValue()+"\r\n";
        }
        head += "\r\n";

    }

    //initialize headers
    public void iniHeaders() {
        this.headers.put("Host", "http://localhost:41830/");
        this.headers.put("User-Agent", "Concordia-HTTP/1.0");
        this.headers.put("Accept-Language", "en-us,en;q=0.5");
        this.headers.put("Accept-Encoding", "gzip, deflate");
    }

    //setHeader
    public void setHeader(String k, String v) {
        this.headers.put(k, v);
    }

    public String getRequest() {
        return request;
    }

    public String getResponse() {
        return response;
    }
    public String getRequestBody() {
        return requestBody;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    //pick up body part from response and assign it to body
    public void pickBody() {
        if (response != null) {
            String[] str = response.split("\r\n\r\n");
            if (str.length > 1)
                body = str[str.length - 1];
        }

    }
}
