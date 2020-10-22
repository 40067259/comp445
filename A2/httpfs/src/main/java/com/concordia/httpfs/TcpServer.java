package com.concordia.httpfs;

import java.io.*;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

/**
 * Receive the request from client side,read the data from request
 * and response the related information to client
 *
 * Constructor:
 *    ServerSocket(int port) bind the specific port to server
 *    Server must know the source of request. we can use 'accept' to get the socket of client side
 *
 *    instance variable:
 *          Socket accept() listen and accept the socket link request
 */
public class TcpServer {
    private ServerSocket server;
    private int port;
    private String request;
    private final String PREFIX = "home/user/Documents";
    private String statusLine;
    private String headers;
    private String body;
    private String response;

    //Create a server socket, default is 8080
    public TcpServer() {
        request ="";
        response = "";
        try {
            server = new ServerSocket(8080);
        } catch (IOException exception) {
            System.out.println("Port: 8080 is not available");
        }
    }
    //Create a server socket, give a specific port number
    public TcpServer(int port) {
        request ="";
        response = "";
        this.port = port;
        try {
            server = new ServerSocket(port);
        } catch (IOException exception) {
            System.out.println("Your port number "+port+" is out of rang or it is not available");;
        }
    }
    //Connect client and server
    public void serverGetConnection() throws IOException {
        //obtain client side socket
        Socket socket = server.accept();
        //get inputStream object
        InputStream is = socket.getInputStream();
        //use inputStream to read the data from client
        byte[] bytes = new byte[1024*2];
        int length = is.read(bytes);
        //get the request
        System.out.println("I receive the request: ");
        request = new String(bytes,0,length);
        System.out.println(request);
        //is.close();
        //process the request
        if(request.substring(0,3).equals("GET")) getFile(socket);
        else if(request.substring(0,4).equals("POST")) postFile(socket);
        response = statusLine+headers+body;
        //response to client side
        OutputStream os = socket.getOutputStream();
        os.write(response.getBytes());
        is.close();
       // socket.close();
        os.flush();
        os.close();
    }

    public void getFile(Socket socket) throws IOException {
      String path = getPath();
        System.out.println("path----------->"+path);
        String str[] = path.split("/");
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
     readFileToResponse(fileName,socket);

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

    public void readFileToResponse(String path,Socket socket) throws IOException {
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
        }finally {

        }

    }

    public void postFile(Socket socket) throws IOException {
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
}
