package com.concordia.https;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UDPServer {

    private SocketAddress routerAddr;
    private SocketAddress localAddr;
    private Server server;
    private boolean isChannelBound;
    private long sequenceNumber = 1L;

    public UDPServer(int localPort) throws UnknownHostException {
        this.localAddr = new InetSocketAddress("localhost", localPort);
        isChannelBound = false;
    }

    public void listenAndServe(Server server) throws IOException {

        try (DatagramChannel datagramChannel = DatagramChannel.open()) {
            if (!isChannelBound) {
                datagramChannel.bind(localAddr);
                isChannelBound = true;
            }
            System.out.println("Server is now listening router at " + datagramChannel.getLocalAddress());
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);
            this.server = server;
            while (true) {
                System.out.println("\n================================");
                System.out.println("Server waiting for new packet...");

                /*
                int sleepSeconds = 5000;
                System.out.println("Server going to sleep for " + sleepSeconds + " seconds...");
                try {
                    Thread.sleep(sleepSeconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Server just woke up...");
                 */

                buf.clear();
                routerAddr = datagramChannel.receive(buf);
                System.out.println("New packet arrived! Now processing...");

                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                buf.flip();

                sequenceNumber = packet.getSequenceNumber();

                String payload = new String(packet.getPayload(), UTF_8);
                System.out.println("Packet: " + packet);
                System.out.println("Payload: " + payload);
                System.out.println("Router: " + routerAddr);

                if (packet.getType() == 0 ||packet.getType() == 1) {
                    String serverResponsePayload = this.server.handleClientPacket(payload);

                    // Send the response to the router not the client.
                    // The peer address of the packet is the address of the client already.
                    // We can use toBuilder to copy properties of the current packet.
                    // This demonstrate how to create a new packet from an existing packet.
                    Packet resp = packet.toBuilder()
                            .setType(Packet.ACK)
                            .setSequenceNumber(sequenceNumber + 1)
                            .setPayload(serverResponsePayload.getBytes())
                            //.setPortNumber(41830)
                            //.setPeerAddress(clientSocket)
                            .create();
                    datagramChannel.send(resp.toBuffer(), routerAddr);
                } else if (packet.getType() == Packet.SYN) {
                    System.out.println("3-way handshaking with incoming packet!");
                    System.out.println("Message from package : " + new String(packet.getPayload(), StandardCharsets.UTF_8));
                    Packet response = packet.toBuilder()
                            .setType(Packet.SYN_ACK)
                            .setSequenceNumber(sequenceNumber + 1)
                            .setPayload("Hi!".getBytes())
                            .create();
                    datagramChannel.send(response.toBuffer(), routerAddr);
                    System.out.println("Sending out 3-way handshaking response!");
                }
                System.out.println("Done processing for a packet...\n");
            }
        }
    }
}