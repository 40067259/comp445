package com.concordia.https;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

    public UDPServer(int localPort) {
        this.localAddr = new InetSocketAddress("localhost", localPort);
        this.routerAddr = new InetSocketAddress("localhost", 3000);
        isChannelBound = false;
    }

    public void listenAndServe(Server server) throws IOException {
        try (DatagramChannel datagramChannel = DatagramChannel.open()) {
            if (!isChannelBound) {
                datagramChannel.bind(routerAddr);
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
                buf.clear();
                routerAddr = datagramChannel.receive(buf);
                System.out.println("New packet arrived! Now processing...");

                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                buf.flip();

                if (packet.getType() == 0) {
                    String payload = new String(packet.getPayload(), UTF_8);
                    System.out.println("Packet: " + packet);
                    System.out.println("Payload: " + payload);
                    System.out.println("Router: " + routerAddr);

                    String serverResponsePayload = this.server.handleClientPacket(payload);

                    // Send the response to the router not the client.
                    // The peer address of the packet is the address of the client already.
                    // We can use toBuilder to copy properties of the current packet.
                    // This demonstrate how to create a new packet from an existing packet.
                    Packet resp = packet.toBuilder()
                            .setType(Packet.ACK)
                            .setSequenceNumber(packet.getSequenceNumber() + 1)
                            .setPayload(serverResponsePayload.getBytes())
                            .create();
                    datagramChannel.send(resp.toBuffer(), routerAddr);
                } else if (packet.getType() == Packet.SYN) {
                    System.out.println("3-way handshaking with incoming packet!");
                    System.out.println("Message from package : " + new String(packet.getPayload(), StandardCharsets.UTF_8));
                    Packet response = packet.toBuilder().setSequenceNumber(packet.getSequenceNumber() + 1)
                            .setType(Packet.SYN_ACK)
                            .setPayload("Server received 3-way handshaking request!".getBytes())
                            .create();
                    datagramChannel.send(response.toBuffer(), routerAddr);
                    System.out.println("Sending out 3-way handshaking response!");
                }
                System.out.println("Done processing for a packet...\n");
            }
        }
    }
}