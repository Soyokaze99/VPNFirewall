package de.unibonn.ineluki.vpnfirewall;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by Ineluki on 02/08/2014.
 */
public class VPN1 extends VpnService implements Handler.Callback, Runnable {
    private static final String TAG = "GITVpnService";

    private String mServerAddress = "127.0.0.1";
    private int mServerPort = 55555;
    private PendingIntent mConfigureIntent;

    private Handler mHandler;
    private Thread mThread;

    private ParcelFileDescriptor mInterface;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The handler is only used to show messages.

        //broadcaster = LocalBroadcastManager.getInstance(this);

        if (mHandler == null) {
            mHandler = new Handler(this);
        }

        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
        }
        // Start a new session by creating a new thread.
        mThread = new Thread(this, "VpnThread");
        mThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Toast.makeText(this, (String) message.obj, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public synchronized void run() {
        try {
            Log.i(TAG, "Starting");
            InetSocketAddress server = new InetSocketAddress(
                    mServerAddress, mServerPort);

            run(server);

        } catch (Exception e) {
            Log.e(TAG, "Got " + e.toString());
            try {
                mInterface.close();
            } catch (Exception e2) {
                // ignore
            }
            Message msgObj = mHandler.obtainMessage();
            msgObj.obj = "Disconnected";
            mHandler.sendMessage(msgObj);

        } finally {

        }
    }

    DatagramChannel mTunnel = null; //???


    private boolean run(InetSocketAddress server) throws Exception {
        boolean connected = false;

        android.os.Debug.waitForDebugger();

        // Create a DatagramChannel as the VPN tunnel.
        mTunnel = DatagramChannel.open();

        if (!protect(mTunnel.socket())) {
            throw new IllegalStateException("Cannot protect the tunnel");
        }

        // Connect to the server.
        mTunnel.connect(server);

        // For simplicity, we use the same thread for both reading and
        // writing. Here we put the tunnel into non-blocking mode.
        mTunnel.configureBlocking(false);

        // Authenticate and configure the virtual network interface.
        handshake();

        // Now we are connected. Set the flag and show the message.
        connected = true;
        Message msgObj = mHandler.obtainMessage();
        msgObj.obj = "Connected";
        mHandler.sendMessage(msgObj);

        new Thread ()
        {
            public void run ()
            {
                // Packets to be sent are queued in this input stream.
                FileInputStream in = new FileInputStream(mInterface.getFileDescriptor());
                // Allocate the buffer for a single packet.
                ByteBuffer packet = ByteBuffer.allocate(32767);
                int length;
                try
                {
                    while (true)
                    {
                        while ((length = in.read(packet.array())) > 0) {
                            // Write the outgoing packet to the tunnel.
                            packet.limit(length);
                            debugPacket(packet);    // Packet size, Protocol, source, destination
                            mTunnel.write(packet);
                            packet.clear();

                        }
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }.start();

        new Thread ()
        {

            public void run ()
            {
                DatagramChannel tunnel = mTunnel;
                // Allocate the buffer for a single packet.
                ByteBuffer packet = ByteBuffer.allocate(8096);
                // Packets received need to be written to this output stream.
                FileOutputStream out = new FileOutputStream(mInterface.getFileDescriptor());

                while (true)
                {
                    try
                    {
                        // Read the incoming packet from the tunnel.
                        int length;
                        while ((length = tunnel.read(packet)) > 0)
                        {
                            // Write the incoming packet to the output stream.
                            out.write(packet.array(), 0, length);

                            packet.clear();

                        }
                    }
                    catch (IOException ioe)
                    {
                        ioe.printStackTrace();
                    }
                }
            }
        }.start();

        return connected;
    }

    private void handshake() throws Exception {

        if (mInterface == null)
        {
            Builder builder = new Builder();

            builder.setMtu(1500);
            builder.addAddress("10.0.0.2",32);
            builder.addRoute("0.0.0.0", 0);
            //builder.addRoute("192.168.2.0",24);
            //builder.addDnsServer("8.8.8.8");

            // Close the old interface since the parameters have been changed.
            try {
                mInterface.close();
            } catch (Exception e) {
                // ignore
            }


            // Create a new interface using the builder and save the parameters.
            mInterface = builder.setSession("VPN1")
                    .setConfigureIntent(mConfigureIntent)
                    .establish();
        }
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

        Log.d(TAG, "Destination IP:"+destIP);

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



}//End of class
