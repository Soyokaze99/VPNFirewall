package de.unibonn.ineluki.vpnfirewall;

/**
 * Created by Ineluki on 07/08/2014.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class ConnectionListActivity extends Activity {

    private ListView listView1;
    //ConnectionData[] conn_data;
    //ConnectionDataAdapter adapter;

    List values = new ArrayList();

    CDReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wlist);

/*
        ConnectionData[] conn_data = new ConnectionData[10];

        ConnectionDataAdapter adapter = new ConnectionDataAdapter(this,
                R.layout.listview_item_row, conn_data);
*/

        listView1 = (ListView) findViewById(R.id.listView1);

        View header = (View) getLayoutInflater().inflate(R.layout.listview_header_row, null);
        listView1.addHeaderView(header);


        values.add("0.0.0.0");


        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        listView1.setAdapter(stringArrayAdapter);
        //listView1.setAdapter(adapter);



        receiver = new CDReceiver() ;
        receiver.adapter = stringArrayAdapter;


    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(VPN2.VPN_RESULT));
    }

    @Override
    protected void onStop() {
        //  LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

}


class CDReceiver extends BroadcastReceiver {

    public ArrayAdapter<String> adapter;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context, "Broadcast Intent Detected.",
        //        Toast.LENGTH_LONG).show();
        String s = intent.getStringExtra("newKey");
        Log.d("CListAct","new data:"+s);
        //ConnectionData d = new ConnectionData();
        //d.destAddr = InetAddress.getByAddress (s.getBytes())

        adapter.add(s);
        adapter.notifyDataSetChanged();
    }
}
