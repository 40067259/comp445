package com.concordia.httpfs;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Receive the request from client side,read the data from request
 * and response the related information to client
 * <p>
 * Constructor:
 * ServerSocket(int port) bind the specific port to server
 * Server must know the source of request. we can use 'accept' to get the socket of client side
 * <p>
 * instance variable:
 * Socket accept() listen and accept the socket link request
 */
public class TcpServer extends Thread {
    private ServerSocket server;
    private int port;
    private int threadCounter = 0;
    private static ReadWriteLock readWriteLock;

    //Create a server socket, default is 8088
    public TcpServer() {
        try {
            server = new ServerSocket(8088);
        } catch (IOException exception) {
            System.out.println("Port: 8088 is not available");
        }
    }

    //Create a server socket, give a specific port number
    public TcpServer(int port) {
        this.port = port;
        try {
            System.out.println("\n================================");
            System.out.println("Starting a new server with port " + port + "...");
            server = new ServerSocket(this.port);
            readWriteLock = new ReentrantReadWriteLock();
            System.out.println("Server created with port " + port + "! Now listening...");
        } catch (IOException exception) {
            System.out.println("Your port number " + this.port + " is out of range or it is not available!");
            System.exit(0);
        }
    }

    //Connect client and server
    public void serverGetConnection() throws IOException {
        Socket socket = null;
        while (true) {

            try {
                // obtain client side socket
                socket = server.accept();

                System.out.println("\n================================");
                System.out.println("A client is connected : " + socket);

                // getting inputStream and outStream
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();

                System.out.println("Creating a new thread for new client...");

                threadCounter++;
                Thread thread = new HttpfsClientHandler(socket, is, os, threadCounter, readWriteLock);
                thread.start();
            } catch (Exception e) {
                System.exit(0);
            }
        }
    }
}
