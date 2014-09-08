package de.unibonn.ineluki.vpnfirewall;

import android.content.Intent;
import android.net.VpnService;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Created by Ineluki on 17/08/2014.
 */
public class VPN2 extends VpnService implements Handler.Callback, Runnable {
    private static final String TAG = "VPN2";

    private Handler mHandler;
    private Thread mThread;

    public TextView textLog;

    private ParcelFileDescriptor mInterface;
    private String localIP;

    LocalBroadcastManager broadcaster;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = new Handler(this);
        }

        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
        }

        /*
        Bundle extras = intent.getExtras();
        if(extras != null) textLog = (TextView) extras.get("keyName");
        */
        broadcaster = LocalBroadcastManager.getInstance(this);

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        localIP = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        Log.i(TAG, "local IP on start: " + localIP);

        // Start a new session by creating a new thread.
        mThread = new Thread(this, "TestVpnThread");
        mThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy:");
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public synchronized void run() {
        Log.i(TAG, "running vpnService");
        try {
            runVpnConnection();
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e(TAG, "Got " + e.toString());
        } finally {
            try {
                mInterface.close();
            } catch (Exception e) {
                // ignore
            }
            mInterface = null;

            //mHandler.sendEmptyMessage(R.string.disconnected);
            Log.i(TAG, "Exiting");
        }
    }


    private boolean shouldBeBlocked(InetAddress addr)
    {
       if(startActivity.rules.contains(addr.getHostAddress() ))
           return true;
        else
            return false;
    }



    private void runVpnConnection() {

        configure();

        FileInputStream in = new FileInputStream(mInterface.getFileDescriptor());
        FileOutputStream out = new FileOutputStream(mInterface.getFileDescriptor());

        // Allocate the buffer for a single packet.
        ByteBuffer packet = ByteBuffer.allocate(32767);

        Socket tcpSocket = new Socket();

        boolean ok = true;

        //forwarding packets
        while (ok) {
            // Assume no progress in this iteration.
            try {
                // Read the outgoing packet from the input stream.
                int length = in.read(packet.array());
                if (length > 0) {
                    Log.i(TAG, "packet received");
                    packet.limit(length);
                    String serverIP = "192.168.178.20";//getDestinationIP(packet);
                    int port = 5002; //getDestinationPort(packet, getHeaderLength(packet));
                    //Log.d(TAG, "destination IP: " + serverIP + " port: " + port);
                    InetAddress serverAddr = InetAddress.getByName(serverIP);
                    SocketAddress socketadd = new InetSocketAddress(serverAddr, port);
                    /*
                    tcpSocket.connect(socketadd);
                    OutputStream outBuffer = tcpSocket.getOutputStream();
                    outBuffer.write(packet.array());
                    outBuffer.flush();
                    outBuffer.close();
                    packet.clear();
                    */
                    //ok = false;

                    ByteBuffer packet2 = packet.duplicate();
                    PaketData pdata = parsePacket(packet);
                    pdata.printValues();
                    //debugPacket(packet2);
                    //serverIP = pdata.destAddr//getDestinationIP(packet);
                    port = pdata.destPort; //getDestinationPort(packet, getHeaderLength(packet));

                    Log.d(TAG, "prot:"+pdata.prot);
                    if(pdata.prot==17) {
                        Log.d(TAG, "udp");
                        //socketadd = new InetSocketAddress(pdata.destAddr, port);
                        DatagramSocket s = new DatagramSocket();

                        byte[] data= new byte[pdata.data.capacity()];
                        pdata.data.get(data);

                        if(shouldBeBlocked(pdata.destAddr))
                        {
                            sendResult("blocked: "+ pdata.destAddr.toString()+":"+pdata.destPort);
                        }else{
                            sendResult(pdata.destAddr.toString()+":"+pdata.destPort);
                            if (protect(s)){
                                DatagramPacket p = new DatagramPacket(data, data.length, pdata.destAddr, port);
                                Log.d("SendOutTest","try sending");
                                try {
                                    s.send(p);
                                    Log.d("SendOutTest","send out!");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.d("SendOutTest", "message:");

                            }else{
                                Log.d("SendOutTest", "protection of socket failed!");
                            }
                        }
                    }

                    if(pdata.prot==6) {
                        Log.d(TAG, "tcp");

                        byte[] data= new byte[pdata.data.capacity()];
                        pdata.data.get(data);


                        Socket s = new Socket(pdata.destAddr,pdata.destPort);

                        if(shouldBeBlocked(pdata.destAddr))
                        {
                            sendResult("blocked: "+ pdata.destAddr.toString()+":"+pdata.destPort);
                        }else{
                            sendResult(pdata.destAddr.toString()+":"+pdata.destPort);
                            if (protect(s)){

                                Log.d(TAG, "try sending");
                                try {
                                    OutputStream outStream = s.getOutputStream();
                                    DataOutputStream dos = new DataOutputStream(out);

                                    int dataLength = data.length;
                                    dos.writeInt(dataLength);
                                    if (dataLength > 0) {
                                        dos.write(data, 0, dataLength);
                                    }

                                    Log.d(TAG, "send out!");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.d(TAG, "message:");

                            }else{
                                Log.d(TAG, "protection of socket failed!");
                            }
                        }


                    }

                    if(pdata.prot==1) Log.d(TAG,"ICMP");


                    packet.clear();
                }
                /*
                if (tcpSocket.isConnected()) {
                    InputStream inBuffer = tcpSocket.getInputStream();
                    byte[] bufferOutput = new byte[32767];
                    inBuffer.read(bufferOutput);
                    if (bufferOutput.length > 0) {
                        String recPacketString = new String(bufferOutput, 0, bufferOutput.length, "UTF-8");
                        Log.d(TAG, "recPacketString : " + recPacketString);
                        out.write(bufferOutput);
                    }

                    inBuffer.close();
                }*/
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e(TAG, "exception: " + e.toString());
                ok = false;
            }

        }
    }


    private void configure() {
        // If the old interface has exactly the same parameters, use it!
        if (mInterface != null) {
            Log.i(TAG, "Using the previous interface");
            return;
        }

        // Configure a builder while parsing the parameters.
        Builder builder = new Builder();
        builder.setMtu(1500);
        builder.addAddress("192.168.178.90", 24);
        //builder.addAddress("10.0.2.0", 32);
        //builder.addDnsServer("8.8.8.8");
        builder.addRoute("0.0.0.0", 0);  // to intercept packets
        try {
            mInterface.close();
        } catch (Exception e) {
            // ignore
        }
        mInterface = builder.establish();
    }


    private String bytesToString (int[] inArray){
        StringBuffer sb = new StringBuffer();
        for (int b : inArray) {
            sb.append(String.format("%02d.", b & 0xFF)); // print the byte as a 0 padded, two digit, hexadecimal String
        }
        return (sb.toString());
    }

    private String bytesToString (byte[] inArray){
        StringBuffer sb = new StringBuffer();
        for (int b : inArray) {
            sb.append(String.format("%02d.", b & 0xFF)); // print the byte as a 0 padded, two digit, hexadecimal String
        }
        return (sb.toString());
    }


    public static String getIpAddress(byte[] rawBytes) {
        int i = 4;
        String ipAddress = "";
        for (byte raw : rawBytes)
        {
            ipAddress += (raw & 0xFF);
            if (--i > 0)
            {
                ipAddress += ".";
            }
        }

        return ipAddress;
    }


    public static String getPortHex(byte[] rawBytes) {
        int i = 2;
        String ipAddress = "";
        for (byte raw : rawBytes)
        {
            ipAddress += (raw & 0xFF);
            if (--i > 0)
            {
                ipAddress += ".";
            }
        }

        return ipAddress;
    }

    public int TwoBytesToInt(Byte[] bytes){

       return  (int)((bytes[0]& 0xFF)) *256 + (bytes[1]& 0xFF);

    }


    private PaketData parsePacket(ByteBuffer packet)
    {
        PaketData ret = new PaketData();

        ByteBuffer dupl = packet.duplicate();
        ByteBuffer dupl2 = packet.duplicate();

        int buffer = packet.get();
        int version;
        int headerLength;
        version = buffer >> 4;
        headerLength =(buffer & 0x0F)*4 ;
        Log.d(TAG, "IP Version:"+version);
        Log.d(TAG, "Header Length in Byte:"+headerLength);

        byte[] rest = new byte[11];
        byte[] srcAdd = new byte[4];
        byte[] destAdd = new byte[4];

        byte[] srcPortBytes = new byte[2];
        byte[] destPortBytes = new byte[2];

        int srcPort;
        int destPort;

        packet.get(rest,0,11);
        packet.get(srcAdd,0,4);
        packet.get(destAdd,0,4);

        byte prot = rest[8];

        ret.prot = prot & 0xFF ;

        packet.get(rest,0,4);

        //srcPort = packet.getInt();
        //destPort = packet.getInt();

/*
        byte buff;
        int i =1;
        while (dupl.position() < dupl.limit()) {

            buff = dupl.get();

            Log.d(TAG, "byte " + String.format("%03d.", i) + ":" + String.format("%02x.", buff & 0xFF));
            i++;
            if(i==headerLength) Log.d(TAG, "end of IP header------------------------------------------ ");
        }
        */
        srcPortBytes[0] = dupl.get(headerLength);
        srcPortBytes[1] = dupl.get(headerLength+1);

        destPortBytes[0] = dupl.get(headerLength+2);
        destPortBytes[1] = dupl.get(headerLength+3);

        Log.d(TAG, "srcPortBytes[0] " + String.format("%02x.", srcPortBytes[0] & 0xFF));
        Log.d(TAG, "srcPortBytes[1] " + String.format("%02x.", srcPortBytes[1] & 0xFF));

        Log.d(TAG, "destPortBytes[0] " + String.format("%02x.", destPortBytes[0] & 0xFF));
        Log.d(TAG, "destPortBytes[1] " + String.format("%02x.", destPortBytes[1] & 0xFF));

        srcPort = (int)((srcPortBytes[0]& 0xFF)) *256 + (srcPortBytes[1]& 0xFF);
        destPort = (int)(destPortBytes[0] & 0xFF) *256 + (destPortBytes[1]& 0xFF);

        ret.destPort = destPort;
        ret.srcPort = srcPort;

        Log.d(TAG, "srcAdd:"+getIpAddress(srcAdd));
        Log.d(TAG, "destAdd:"+getIpAddress(destAdd));

        Log.d(TAG, "srcPort:"+srcPort);
        Log.d(TAG, "destPort:"+destPort);

        Log.d(TAG, "prot:"+prot);

        byte[] header = new byte[headerLength+8];
        byte[] data =  new byte[dupl.limit()-headerLength-8];

        if(prot==17){
            header = new byte[headerLength+8];
            data =  new byte[dupl.limit()-headerLength-8];
            dupl2.get(header,0,headerLength+8);
            dupl2.get(data,0,dupl.limit()-headerLength-8);
        }

        if(prot==6){
            int dataOffset = ((dupl2.get(headerLength+12))& 0xFF)>>>4;
            Log.d(TAG, "dataOffset " + dataOffset  + ":" + String.format("%02x.", dupl2.get(headerLength+12) & 0xFF));
            Log.d(TAG, "dupl2 limit " + dupl2.limit());
            Log.d(TAG, "dupl2 capa " + dupl2.capacity());
            header = new byte[headerLength+dataOffset];
            data =  new byte[dupl.limit()-headerLength-dataOffset];
            dupl2.get(header,0,headerLength+dataOffset);
            dupl2.get(data,0,dupl.limit()-headerLength-dataOffset);
        }


        byte buff;
        int i =0;
        while (i < data.length) {

            buff = data[i];

            Log.d(TAG, "byte " + String.format("%03d.", i) + ":" + String.format("%02x.", buff & 0xFF));
            i++;

        }


        try {
            ret.srcAddr = InetAddress.getByAddress(srcAdd);
            ret.destAddr = InetAddress.getByAddress(destAdd);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        ret.data = ByteBuffer.wrap(data);

        return ret;

    }


    private void debugPacket(ByteBuffer packet)
    {
        int buffer = packet.get();
        int version;
        int headerLength;
        version = buffer >> 4;
        headerLength = buffer & 0x0F;
        headerLength *= 4;
        Log.d(TAG, "IP Version:"+version);
        Log.d(TAG, "Header Length:"+headerLength);

        String status = "";
        status += "Header Length:"+headerLength;

        buffer = packet.get();      //DSCP + EN
        buffer = packet.getChar();  //Total Length

        Log.d(TAG, "Total Length:"+buffer);

        buffer = packet.getChar();  //Identification
        buffer = packet.getChar();  //Flags + Fragment Offset
        buffer = packet.get();      //Time to Live
        buffer = packet.get();      //Protocol

        Log.d(TAG, "Protocol:"+buffer);

        status += "  Protocol:"+buffer;

        buffer = packet.getChar();  //Header checksum

        int[] buff = new int[4];

        String sourceIP  = "";
        buffer = packet.get();  //Source IP 1st Octet
        sourceIP += (byte)buffer;
        sourceIP += ".";
        buff[0] = buffer;

        buffer = packet.get();  //Source IP 2nd Octet
        sourceIP += (byte)buffer;
        sourceIP += ".";
        buff[1] = buffer;

        buffer = packet.get();  //Source IP 3rd Octet
        sourceIP += (byte)buffer;
        sourceIP += ".";
        buff[2] = buffer;

        buffer = packet.get();  //Source IP 4th Octet
        sourceIP += (byte)buffer;
        buff[3] = buffer;

        Log.d(TAG, "Source IP:"+sourceIP);

        Log.d(TAG, "Source IP new :"+bytesToString(buff));

        status += "   Source IP:"+sourceIP;

        String destIP  = "";
        buffer = packet.get();  //Destination IP 1st Octet
        destIP += (byte)buffer;
        destIP += ".";
        buff[0] = buffer;

        buffer = packet.get();  //Destination IP 2nd Octet
        destIP += (byte)buffer;
        destIP += ".";
        buff[1] = buffer;

        buffer = packet.get();  //Destination IP 3rd Octet
        destIP += (byte)buffer;
        destIP += ".";
        buff[2] = buffer;

        buffer = packet.get();  //Destination IP 4th Octet
        destIP += (byte)buffer;
        buff[3] = buffer;


        Log.d(TAG, "Destination IP new :"+bytesToString(buff));
        Log.d(TAG, "Destination IP:"+destIP);

        status += "Destination IP:"+destIP;
        /*
        msgObj = mHandler.obtainMessage();
        msgObj.obj = status;
        mHandler.sendMessage(msgObj);
        */

        String value = new String(packet.array());

        Log.d(TAG, "value:"+value);

        sendResult(" Destination IP:"+destIP + " Source IP:"+sourceIP );


    }

    static final public String VPN_RESULT = "de.unibonn.ineluki.VPN_RESULT";

    public void sendResult(String message) {
        Intent intent = new Intent(VPN_RESULT);
        if(message != null)
            intent.putExtra("newKey", message);
        broadcaster.sendBroadcast(intent);
    }


    public void  onRevoke (){
        Log.d(TAG, "revoked:");
    }


}
