package com.concordia.httpfs;

import org.springframework.http.ContentDisposition;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.locks.ReadWriteLock;
import javax.activation.MimetypesFileTypeMap;

public class HttpfsClientHandler extends Thread {
    private final Socket socket;
    private final InputStream iS;
    private final OutputStream oS;
    private String request;
    private String statusLine;
    private String headers;
    private String body;
    private String response;
    private ReadWriteLock readWriteLock;

    public HttpfsClientHandler(Socket socket, InputStream iS, OutputStream oS, int threadCounter, ReadWriteLock readWriteLock) {
        this.socket = socket;
        this.iS = iS;
        this.oS = oS;
        this.setName("T" + threadCounter);
        this.readWriteLock = readWriteLock;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] bytes = new byte[2048 * 2];
                int length = iS.read(bytes);
                //get the request
                request = new String(bytes, 0, length);
                System.out.println("================================\n");
                System.out.println("Received the request from client " + this.getName() + ":\n" + request);
                System.out.println("================================\n");
                //is.close();
                //process the request
                if (request.substring(0, 3).equalsIgnoreCase("GET")) getFile();
                else if (request.substring(0, 4).equalsIgnoreCase("POST")) postFile();
                else response = "Please provide a method of GET or POST.";
                System.out.println("\n- - - - Response for " + this.getName() + " - - - - \n");
                System.out.print(response);
                //response to client side
                OutputStream os = socket.getOutputStream();
                os.write(response.getBytes());
                iS.close();
                os.flush();
                os.close();
                System.out.println("================================");
                System.out.println("Response sent to client " + this.getName() + "!\n");
                clearResponse();
                System.out.println("Listening for more request...\n");
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    public void clearResponse() {
        request = null;
        statusLine = null;
        headers = null;
        body = null;
        response = null;
    }

    public void getFile() throws IOException, InterruptedException {
        File file = new File("../home" + getPath());

        if (file.exists() || file.isDirectory()) {
            readFileToResponse(file);
        } else {
            statusLine = "HTTP/1.0 403 Forbidden\r\n\r\n";
            headers = "User-Agent: Concordia\r\n\r\n";
            body = "";
        }
        response = statusLine + headers + body;
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

    public void readFileToResponse(File file) {
        if (file.isFile()) {
            readWriteLock.readLock().lock();
            synchronized (this) {
                System.out.println("The file is now locked for read only!");
                try {
                    InputStream is = new FileInputStream(file.getPath());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuffer buffer = new StringBuffer();
                    line = reader.readLine();
                    while (line != null) {
                        buffer.append(line);
                        buffer.append("\n");
                        line = reader.readLine();
                        /*
                        try {
                            System.out.println(getName() + " sleeps every 2s to test concurrency with readLock() with actual file!");
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        */
                    }
                    body = buffer.toString();
                    statusLine = "HTTP/1.0 200 OK\r\n\r\n";
                    headers = "User-Agent: Concordia\r\n";
                    headers += "Content-Length: " + body.length() + "\r\n";
                    headers += buildContentTypeDisposition(file);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            readWriteLock.readLock().unlock();
            System.out.println("The file is unlocked now!");
        } else if (file.isDirectory()) {
            String[] files = file.list();
            for (String fileOrFolder : files) {
                body += fileOrFolder + "\n";
            }
            statusLine = "HTTP/1.0 200 OK\r\n\r\n";
            headers = "User-Agent: Concordia\r\n";
            headers += "Below are the available file(s) and folder(s):\n";
            body += "\n";
        } else {
            statusLine = "HTTP/1.0 404 Not Found\r\n\r\n";
            headers = "User-Agent: Concordia\r\n\r\n";
            body = "";
            System.out.println("File is not found");
        }

    }

    private String buildContentTypeDisposition(File file) {
        // https://docs.oracle.com/javase/7/docs/api/javax/activation/MimetypesFileTypeMap.html
        // MimetypesFileTypeMap looks in various places in the user's system for MIME types file entries
        // Searches MIME types files that added in the class instance, .mime.types file,
        // <java.home>/lib/mime.types file, META-INF/mime.types file, and META-INF/mimetypes.default file
        // returns application/octet-stream if file is not found
        MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
        String mimeType = "Content-Type: " + fileTypeMap.getContentType(file.getName()) + "\r\n\r\n";

        // https://github.com/spring-projects/spring-framework/blob/master/spring-web/src/main/java/org/springframework/http/ContentDisposition.java
        // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ContentDisposition.html
        String contentDispositionType = "inline";
        if (mimeType.contains("application/") || mimeType.contains("model/") || mimeType.contains("multipart/"))
            contentDispositionType = "attachment";

        ContentDisposition contentDisposition = ContentDisposition.builder(contentDispositionType)
                .filename(file.getName()).build();

        String mimeDisposition = "Content-Disposition: " + contentDisposition + "\r\n\r\n";

        return mimeType + mimeDisposition;
    }

    public void postFile() throws IOException {
        File file = new File("../home" + getPath());

        if (file.exists() && file.isFile()) {
            writeFileToResponse(file);
        } else {
            statusLine = "HTTP/1.0 404 NOT FOUND\r\n\r\n";
            headers = "User-Agent: Concordia\r\n\r\n";
            body = "";
        }
        response = statusLine + headers + body;

    }

    private void writeFileToResponse(File file) throws IOException {
        readWriteLock.writeLock().lock();
        synchronized (this) {
            String[] str = request.split("\r\n\r\n");
            String requestBody = "";
            if (str.length >= 3) requestBody = str[2];
            String[] data = requestBody.split(",");

            FileWriter fw = null;
            BufferedWriter bw = null;
            fw = new FileWriter(file.getPath(), false);
            bw = new BufferedWriter(fw);
            for (String content : data) {
                //bw.append(content);
                bw.write(content + "\n");
                /*
                try {
                    System.out.println(getName() + " sleeps every 2s to test concurrency with writeLock() with actual file!");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                */
            }
            bw.close();
            fw.close();
        }
        statusLine = "HTTP/1.0 201 Created\r\n\r\n";
        headers = "User-Agent: Concordia\r\n";
        body = "";
        readWriteLock.writeLock().unlock();
    }
}
