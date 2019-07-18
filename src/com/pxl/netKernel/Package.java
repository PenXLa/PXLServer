package com.pxl.netKernel;
import com.pxl.netKernel.PackageUtils;

import java.util.ArrayList;

//Package用作网络传输的基本单位。
//Package只可以被用作读和写其中一种用途，不可以同时读写，也就是说从远端接受的Package只能读，自己创建的Package只能写
//强行违反读写规则可能引发预料之外的后果
public class Package {
    public byte[] data;

    class StringPos {
        public String str;
        public int pos;
    }
    ArrayList<Byte> bin;
    ArrayList<StringPos> stringPool;

    //用作接受包的构造方法
    public Package(byte[] data) {
        this.data = data;
    }

    //用作创建包的构造方法,所以实例化用于写包的两个类
    public Package(int type) {
        bin = new ArrayList<Byte>();
        stringPool = new ArrayList<StringPos>();
        putInt(type);
    }

    public int getType() {
        return (int) PackageUtils.bin2integer(data, 0, ClientConnection.POINTER_SIZE);
    }
    public byte readByte(int offset) {
        return (byte)PackageUtils.bin2integer(data,offset,Byte.SIZE/8);
    }
    public short readShort(int offset) {
        return (short)PackageUtils.bin2integer(data,offset,Short.SIZE/8);
    }
    public int readInt(int offset) {
        return (int)PackageUtils.bin2integer(data,offset,Integer.SIZE/8);
    }
    public long readLong(int offset) {
        return PackageUtils.bin2integer(data,offset,Long.SIZE/8);
    }
    public float readFloat(int offset) {
        return PackageUtils.b2f(data,offset);
    }
    public double readDoube(int offset) {
        return PackageUtils.b2d(data,offset);
    }
    public String readString(int offset) {
        int pos = (int)PackageUtils.bin2integer(data, offset, ClientConnection.POINTER_SIZE), len = (int)PackageUtils.bin2integer(data, offset+ClientConnection.POINTER_SIZE, ClientConnection.POINTER_SIZE);
        String str = null;
        try{
            str = new String(data, pos, len, "utf-8");
        } catch (Exception e) {e.printStackTrace();}
        return str;
    }
    public String[] readStrings(int offset) {
        String[] strs = new String[readInt(offset)];
        for (int i=0; i<strs.length; ++i)
            strs[i] = readString(offset+ClientConnection.POINTER_SIZE + i*2*ClientConnection.POINTER_SIZE);
        return strs;
    }


    public void putByte(byte val) {
        byte[] data = PackageUtils.integer2bin(val,Byte.SIZE/8);
        for (byte b : data) bin.add(b);
    }
    public void putShort(short val) {
        byte[] data = PackageUtils.integer2bin(val,Short.SIZE/8);
        for (byte b : data) bin.add(b);
    }
    public void putInt(int val) {
        byte[] data = PackageUtils.integer2bin(val,Integer.SIZE/8);
        for (byte b : data) bin.add(b);
    }
    public void putLong(long val) {
        byte[] data = PackageUtils.integer2bin(val,Long.SIZE/8);
        for (byte b : data) bin.add(b);
    }
    public void putFloat(float val) {
        byte[] data = PackageUtils.f2b(val);
        for (byte b : data) bin.add(b);
    }
    public void putDouble(double val) {
        byte[] data = PackageUtils.d2b(val);
        for (byte b : data) bin.add(b);
    }

    public void putString(String str) {
        StringPos sp = new StringPos();
        sp.pos = bin.size();
        sp.str = str;
        for (int i=0; i<ClientConnection.POINTER_SIZE*2; ++i) bin.add((byte)0);//为 字符串起始位置 和 字符串长度 占个位
        stringPool.add(sp);
    }
    public void putStrings(String[] strs) {
        putInt(strs.length);
        for (String s:strs)
            putString(s);
    }

    public void pack() {
        int size = 0;
        try {
            for (StringPos sp : stringPool) size += sp.str.getBytes("utf-8").length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] data = new byte[bin.size() + size];
        byte[] strdata = null, posdata = null, lendata = null;
        int pos = 0;
        for (; pos<bin.size(); ++pos) data[pos] = bin.get(pos);//基本数据

        for (StringPos sp : stringPool) {
            try {
                strdata = sp.str.getBytes("utf-8");
                posdata = PackageUtils.integer2bin(pos, ClientConnection.POINTER_SIZE);
                lendata = PackageUtils.integer2bin(strdata.length, ClientConnection.POINTER_SIZE);
                for (int i=0; i<ClientConnection.POINTER_SIZE; ++i) data[sp.pos+i] = posdata[i];//修改刚开始占了位置的str起始位置
                for (int i=0; i<ClientConnection.POINTER_SIZE; ++i) data[sp.pos+ClientConnection.POINTER_SIZE+i] = lendata[i];//修改刚开始占了位置的str长度
                for (int i=0; i<strdata.length; ++i) data[pos+i] = strdata[i];
                pos += strdata.length;
            } catch (Exception e) {e.printStackTrace();}
        }
        this.data = data;
    }

    public void clear() {
        bin.clear();
        stringPool.clear();
        data = null;
    }

}