package com.pxl;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pxl.netKernel.ClientConnection;
import com.pxl.netKernel.Listener;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    static Connection c;
    public static void main(String[] args) throws IOException {

        new Listener(6667){
            @Override
            protected void onAccepted(Socket socket) {
                c = new Connection(socket);
                System.out.println("Connected");
            }
        }.start();

        Scanner in = new Scanner(System.in);
        while(true) {
            String s = in.nextLine();
            if (s.equals("disconnect")) {
                c.disconnect();
            } else {
                byte[] pak = Package.ChatMessage.newBuilder()
                        .setTxt(s)
                        .setTag(System.currentTimeMillis()).build().toByteArray();
                c.sendPackage(pak);
            }

        }
    }
}

class Connection extends ClientConnection {
    public Connection(Socket socket) {
        super(socket);
    }

    @Override
    protected void onReceived(byte[] pak) {
        Package.ChatMessage cm = null;
        try {
            cm = Package.ChatMessage.parseFrom(pak);
            System.out.println(cm.getTxt() + " " + cm.getTag());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDisconnect() {
        System.out.println("DISCONNECTED");
    }
}
