package de.unibonn.ineluki.vpnfirewall;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.InetAddress;

public class ConnectionDataAdapter extends ArrayAdapter<ConnectionData> {

    Context context;
    int layoutResourceId;
    ConnectionData data[] = null;

    public ConnectionDataAdapter(Context context, int layoutResourceId, ConnectionData[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ConnectionDataHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ConnectionDataHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);

            row.setTag(holder);
        }
        else
        {
            holder = (ConnectionDataHolder)row.getTag();
        }

        ConnectionData connData = data[position];
        //holder.imgIcon = connData.destAddr;
        holder.txtTitle.setText(connData.destAddr.getHostAddress() + connData.destPort);

        return row;
    }

    static class ConnectionDataHolder {

        ImageView imgIcon;
        TextView txtTitle;
        /*
        public InetAddress srcAddr;
        public InetAddress destAddr;
        public int srcPort;
        public int destPort;
        public int prot;
        */
    }
}