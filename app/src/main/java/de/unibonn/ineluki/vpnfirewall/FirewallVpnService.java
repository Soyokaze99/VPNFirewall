package de.unibonn.ineluki.vpnfirewall;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by Ineluki on 16/08/2014.
 */
public class FirewallVpnService extends VpnService {
    private static final String TAG = "VpnService";
    private Thread mThread;
    private ParcelFileDescriptor mInterface;
    Builder builder = new Builder();

    public TextView textLog;

    // Services interface
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Configure  TUN and get the interface.
                    mInterface = builder.setSession("VPNService")
                            .addAddress("192.168.0.99", 24)
                            //.addDnsServer("8.8.8.8")
                            .addRoute("192.0.0.0", 8).establish();
                    // Packets to be sent
                    FileInputStream in = new FileInputStream(
                            mInterface.getFileDescriptor());
                    // Packets received need to be written to this output stream.
                    FileOutputStream out = new FileOutputStream(
                            mInterface.getFileDescriptor());

                    ByteBuffer packet = ByteBuffer.allocate(32767);

                    int length;

                    //The UDP channel can be used to pass/get ip package
                    DatagramChannel tunnel = DatagramChannel.open();
                    tunnel.connect(new InetSocketAddress("127.0.0.1", 8087));
                    //Protect this socket, so package send by it will not be feedback to the vpn service.
                    protect(tunnel.socket());

                    while (true) {
                        //get packet with in
                        while ((length = in.read(packet.array())) > 0) {
                            // Write the outgoing packet to the tunnel.
                            packet.limit(length);
                            debugPacket(packet);    // Packet size, Protocol, source, destination
                            tunnel.write(packet);
                            packet.clear();

                        }
                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (mInterface != null) {
                            mInterface.close();
                            mInterface = null;
                        }
                    } catch (Exception e) {

                    }
                }
            }

        }, "VpnServiceRunnable");

        //start the service
        mThread.start();
        return START_STICKY;

    }


    private void debugPacket(ByteBuffer packet)
    {
        /*
        for(int i = 0; i < length; ++i)
        {
            byte buffer = packet.get();
            Log.d(TAG, "byte:"+buffer);
        }*/



        int buffer = packet.get();
        int version;
        int headerlength;
        version = buffer >> 4;
        headerlength = buffer & 0x0F;
        headerlength *= 4;
        Log.d(TAG, "IP Version:"+version);
        Log.d(TAG, "Header Length:"+headerlength);

        String status = "";
        status += "Header Length:"+headerlength;

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

        String sourceIP  = "";
        buffer = packet.get();  //Source IP 1st Octet
        sourceIP += buffer;
        sourceIP += ".";

        buffer = packet.get();  //Source IP 2nd Octet
        sourceIP += buffer;
        sourceIP += ".";

        buffer = packet.get();  //Source IP 3rd Octet
        sourceIP += buffer;
        sourceIP += ".";

        buffer = packet.get();  //Source IP 4th Octet
        sourceIP += buffer;

        Log.d(TAG, "Source IP:"+sourceIP);

        status += "   Source IP:"+sourceIP;

        String destIP  = "";
        buffer = packet.get();  //Destination IP 1st Octet
        destIP += buffer;
        destIP += ".";

        buffer = packet.get();  //Destination IP 2nd Octet
        destIP += buffer;
        destIP += ".";

        buffer = packet.get();  //Destination IP 3rd Octet
        destIP += buffer;
        destIP += ".";

        buffer = packet.get();  //Destination IP 4th Octet
        destIP += buffer;

        Log.d(TAG, "Destination IP:" + destIP);

        status += "   Destination IP:"+destIP;
        /*
        msgObj = mHandler.obtainMessage();
        msgObj.obj = status;
        mHandler.sendMessage(msgObj);
        */

        //Log.d(TAG, "version:"+packet.getInt());
        //Log.d(TAG, "version:"+packet.getInt());
        //Log.d(TAG, "version:"+packet.getInt());

    }

    @Override
    public void  onRevoke (){
        Log.d(TAG, "revoked:");
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mThread != null) {
            mThread.interrupt();
        }
        super.onDestroy();
    }

}
