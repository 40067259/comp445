package com.example.router;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Router {
    private final String routerAdd = "192.168.2.10";
    private final Integer routerPort = 3000;
    private SocketAddress routerAddr;
    private final String clientAdd = "192.168.2.125";
    private final Integer clientPort = 41830;
    private final String serverAdd = "192.168.2.3";
    private final Integer serverPort = 8007;
    private DatagramChannel channel;
    private ByteBuffer buf;
    private static final Logger logger = LoggerFactory.getLogger(Router.class);


    public Router() throws IOException {
        this.routerAddr = new InetSocketAddress(routerAdd,routerPort);
        this.channel = DatagramChannel.open();
        channel.bind(routerAddr);
        ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
    }
    public void recvfrom() throws IOException {
        if(channel.receive(buf) != null){
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            logger.info("Packet: {}", resp);
            logger.info("Router: {}", routerAddr);
        }else{
            System.out.println("No signal comes");
        }

    }


}
