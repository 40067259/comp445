package com.concordia.httpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_READ;

public class UDPClient {

    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);

    private SocketAddress localAddr;
    private SocketAddress routerAddr;
    private Long sequenceNumber = 1L;
    private boolean isHandShaken;
    private boolean isChannelBound;

    public UDPClient(int localPort) {
        this.localAddr = new InetSocketAddress("localhost", localPort);
        this.routerAddr = new InetSocketAddress("localhost", 3000);
    }

    public String runClient(InetSocketAddress serverAddr, String request) throws IOException {
        String payload = null;
        try (DatagramChannel channel = DatagramChannel.open()) {
            if (!isChannelBound) {
                channel.bind(localAddr);
                isChannelBound = true;
                sequenceNumber = threeWayHandShake(channel, serverAddr);
                return "Three-way handshaking established!";
            } else {
                Packet packet = null;
                if (request.getBytes().length <= Packet.MAX_LEN) {
                    sequenceNumber++;
                    packet = new Packet.Builder()
                            .setType(Packet.DATA)
                            .setSequenceNumber(sequenceNumber)
                            .setPortNumber(serverAddr.getPort())
                            .setPeerAddress(serverAddr.getAddress())
                            .setPayload(request.getBytes())
                            .create();
                }
                channel.send(packet.toBuffer(), routerAddr);
                logger.info("\nSending \"{}\" to router at {}", request, routerAddr);

                timer(packet, channel);

                ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
                routerAddr = channel.receive(buf);
                buf.flip();
                Packet resp = Packet.fromBuffer(buf);
                sequenceNumber++;
                logger.info("Packet: {}", resp);
                logger.info("Router: {}", routerAddr);
                payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
                logger.info("Payload: {}", payload);
                return payload;
            }
        }
    }

    private Long threeWayHandShake(DatagramChannel channel, InetSocketAddress serverAddr) throws IOException {
        System.out.println("Trying to 3-way handshaking...");
        Packet packet = new Packet.Builder()
                .setType(Packet.SYN)
                .setSequenceNumber(sequenceNumber)
                .setPortNumber(serverAddr.getPort())
                .setPeerAddress(serverAddr.getAddress())
                .setPayload("Three-way handshaking request!".getBytes())
                .create();
        channel.send(packet.toBuffer(), routerAddr);

        timer(packet, channel);

        ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
        buf.clear();
        channel.receive(buf);
        buf.flip();

        return Packet.fromBuffer(buf).getSequenceNumber();
    }

    private void timer(Packet packet, DatagramChannel channel) throws IOException {
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, OP_READ);
        selector.select(5000);

        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty()) {
            logger.error("No response after timeout");
            channel.send(packet.toBuffer(), routerAddr);
            timer(packet, channel);
        }
        keys.clear();
    }
}