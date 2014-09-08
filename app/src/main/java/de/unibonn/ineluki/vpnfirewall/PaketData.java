package de.unibonn.ineluki.vpnfirewall;

import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by Ineluki on 04/08/2014.
 */
public class PaketData {

    private static final String TAG = "VPN2 PaketData";

    public InetAddress srcAddr;
    public InetAddress destAddr;
    public int srcPort;
    public int destPort;
    public int prot;
    public ByteBuffer data;

    public void printValues(){
        Log.e(TAG, "srcAddr: " + srcAddr.getHostAddress());
        Log.e(TAG, "destAddr: " + destAddr.getHostAddress());
        Log.e(TAG, "srcPort: " +  srcPort);
        Log.e(TAG, "destPort: " + destPort );
        Log.e(TAG, "prot: " + prot);
    }

}
