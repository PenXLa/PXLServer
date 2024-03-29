package com.pxl.netKernel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientConnection {
    public static final byte POINTER_SIZE = Integer.SIZE/8;//指针/package头长度，无法修改，因为代码中很多地方都是直接申请的int
    protected Socket socket;
    private Receiver receiver;

    public ClientConnection(Socket s) {
        socket = s;
        receiver = new Receiver(this);
        receiver.start();
    }




    protected void onReceived(byte[] pak) {

    }
    protected void onDisconnect() {

    }
    //读取消息的线程抛出异常是调用这个，默认是断开连接，但是可以通过override重写
    protected void onLostConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onDisconnect();
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPackage(byte[] pak) throws IOException {
        byte[] dataWithHead = new byte[pak.length + POINTER_SIZE];
        System.arraycopy(PackageUtils.integer2bin(pak.length, POINTER_SIZE), 0, dataWithHead, 0, POINTER_SIZE);
        System.arraycopy(pak, 0, dataWithHead, POINTER_SIZE, pak.length);
        PackageUtils.sendRawData(socket, dataWithHead);
    }


}


class Receiver extends Thread {
    private ClientConnection cli;

    public Receiver(ClientConnection cli) {
        this.cli = cli;
    }

    @Override
    public void run() {
        try {
            InputStream is = cli.socket.getInputStream();
            while(true) {
                byte[] pak = getPack(is, cli.POINTER_SIZE);
                if (pak!=null) cli.onReceived(pak);
            }
        } catch (IOException e) {
            cli.onLostConnection();
        }
    }



    private byte[] getPack(InputStream is, int headSize) throws IOException {
        byte[] data = null;

        byte[] sizeb = new byte[headSize];
        for (int i=0; i<headSize; ++i) {
            int b = is.read();
            if (b==-1) throw new SocketException();//读到-1，throw一个exception表示连接断开
            sizeb[i] = (byte)b;
        }
        int binsize= (int) PackageUtils.bin2integer(sizeb, 0, headSize);

        data = new byte[binsize];

        for (int readlen = 0; readlen < binsize;) {
            int len = is.read(data, readlen, binsize-readlen);
            if (len==-1) throw new SocketException();//read数据顺便判断是否断开连接
            readlen += len;
        }

        return data;
    }
}

class PackageUtils {
    public static byte[] integer2bin(long n, int size) {
        byte[] bin = new byte[size];
        for (int i=0; i<size; ++i) {
            bin[i] = (byte)(n&0xFF);
            n>>=8;
        }
        return bin;
    }
    //函数返回long，数值上是unsigned的int或short，但使用强制转换可以自动转换成signed的
    public static long bin2integer(byte[] bin, int off, int len) {
        long n = 0;
        for (int i=off; i<off+len; ++i)
            n |= ((bin[i]&0xFFL)<<((i-off)*8));

        return n;
    }
    public static byte[] i2b(int n) {
        return integer2bin(n,4);
    }
    public static byte[] l2b(long n) {
        return integer2bin(n,8);
    }
    public static byte[] s2b(short n) {
        return integer2bin(n,2);
    }
    public static byte[] d2b(double v) {
        return l2b(Double.doubleToLongBits(v));
    }
    public static byte[] f2b(float v) {
        return i2b(Float.floatToIntBits(v));
    }
    public static float b2f(byte[] bin, int off) {
        return Float.intBitsToFloat((int)bin2integer(bin, off, 4));
    }
    public static double b2d(byte[] bin, int off) {
        return Double.longBitsToDouble(bin2integer(bin, off, 8));
    }

    public static void sendRawData(Socket s, byte[] data) throws IOException{
        OutputStream os = s.getOutputStream();
        os.write(data);
        os.flush();
    }
}