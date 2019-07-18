package com.pxl.netKernel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener extends Thread{
    int port;
    public Listener(int port) {
        this.port = port;
    }
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(port);
            while(true) {
                onAccepted(ss.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onAccepted(Socket socket) {

    }
}
