package com.concordia.https;

import java.io.*;
import java.net.*;

public class Server {
    private String request;
    private final String PREFIX = "home/user/Documents";
    private String statusLine;
    private String headers;
    private String body;
    private String response;


    public Server() throws IOException {
        request ="";
        response = "";
        UDPServer udpServer = new UDPServer(8007);
        udpServer.listenAndServe(this);
    }

    public String handleClientPacket(String payload) throws IOException {
        request = payload;
        if (request.substring(0, 3).equals("GET")) getFile();
        else if (request.substring(0, 4).equals("POST")) postFile();
        response = statusLine + headers + body;

        return response;
    }

    public void getFile() throws IOException {
      String path = getPath();
        System.out.println("path----------->"+path);
        String[] str = path.split("/");
        for(String e:str){
            System.out.println("file Element: ---->"+e);
        }
        String fileName = null;
        if(str.length == 5) {
            if(!str[1].equals("home")||!str[2].equals("user")||!str[3].equals("Documents")){
                statusLine = "HTTP/1.0 403 Forbidden\r\n\r\n";
                headers = "User-Agent: Concordia\r\n\r\n";
                body ="";
            }else{
                fileName = path.substring(1,path.length());
            }

        }
        else if(str.length == 2) fileName = PREFIX + path;
        else{
            statusLine = "HTTP/1.0 403 Forbidden\r\n\r\n";
            headers = "User-Agent: Concordia\r\n\r\n";
            body = "";
        }
        System.out.println("After Added: "+fileName);

     if(fileName != null)
     readFileToResponse(fileName);

    }

    public String getPath() throws MalformedURLException {
       // System.out.println("request: "+request);
        String str[] = request.split("\r\n\r\n");
        String status = str[0];
        String statusStr[] = status.split(" ");
        String strUrl = statusStr[1];
        URL url = new URL(strUrl);
        String path = url.getPath();
        return path;
    }

    public void readFileToResponse(String path) throws IOException {
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = new FileInputStream(path);
            String line;
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer buffer = new StringBuffer();
            line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                buffer.append("\n");
                line = reader.readLine();
            }
            body = buffer.toString();
            statusLine = "HTTP/1.0 200 OK\r\n\r\n";
            headers = "User-Agent: Concordia\r\n";
            headers +="Content-Length: "+body.length()+"\r\n";
            headers +="Content-Type: text/html\r\n\r\n";

        } catch (FileNotFoundException e) {
            statusLine = "HTTP/1.0 404 Not Found\r\n\r\n";
            headers = "User-Agent: Concordia\r\n\r\n";
            body ="";
            System.out.println("File is not found");
        }
    }

    public void postFile() throws IOException {
        String path = getPath();
        String filePath = path.substring(1);
        String[] str = request.split("\r\n\r\n");
        String requestBody = "";
        if(str.length >= 3) requestBody = str[2];
        String[] data = requestBody.split(",");
        System.out.println("filePath:-------------->"+filePath);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(filePath, false);
            bw = new BufferedWriter(fw);
            File file = new File(filePath);
            if(!file.exists())
            fw = new FileWriter(file.getName());
            bw = new BufferedWriter(fw);
            for(String content:data){
                //bw.append(content);
                bw.write(content+"\n");
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        statusLine = "HTTP/1.0 201 Created\r\n\r\n";
        headers = "User-Agent: Concordia\r\n";
        body = "";
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Server starting...");
        new Server();
    }
}
