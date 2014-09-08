package de.unibonn.ineluki.vpnfirewall;

import java.net.InetAddress;

/**
 * Created by Ineluki on 16/08/2014.
 */
public class ConnectionData {

    public InetAddress srcAddr;
    public InetAddress destAddr;
    public int srcPort;
    public int destPort;
    public int prot;
    public long count;

}
