package com.pxl;

import com.pxl.netKernel.ClientConnection;
import com.pxl.netKernel.Listener;
import com.pxl.netKernel.Package;

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
                Package pak = new Package(1);
                pak.putString(s);
                pak.pack();
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
    protected void onReceived(Package pak) {
        System.out.println(pak.readString(4));
    }

    @Override
    protected void onDisconnect() {
        System.out.println("DISCONNECTED");
    }
}
