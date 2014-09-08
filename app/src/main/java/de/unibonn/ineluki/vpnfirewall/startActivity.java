package de.unibonn.ineluki.vpnfirewall;

import android.app.Activity;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ineluki on 13/08/2014.
 */
public class startActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "VPN2 startActivity";
    boolean start = false;
    public TextView textLog;
    public EditText input;
    public Button btn, btn2;
    public Button rules1Btn, rules2Btn;
    public Button connBtn;

    public static ArrayList<String> rules;


    public Button btnTestList, btnTrafficStat,btnRules;

    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(this);

        btnTrafficStat = (Button)findViewById(R.id.buttonTrafficStats);
        btnTrafficStat.setOnClickListener(this);

        btnRules = (Button)findViewById(R.id.buttonRules);
        btnRules.setOnClickListener(this);

        rules2Btn = (Button)findViewById(R.id.buttonRules2);
        rules2Btn.setOnClickListener(this);


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra("newKey");
                CharSequence a =  textLog.getText();
                s = s +a ;
                textLog.setText(s);
            }
        };

        if(rules==null) {
            rules = new ArrayList<String>();
            rules.add("192.168.178.22");
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        Intent i;
        switch (view.getId()) {


            case R.id.button1:
                if (!start) {
                    start = true;
                    Intent intent = VpnService.prepare(this);
                    if (intent != null) {
                        startActivityForResult(intent, 0);
                    } else {
                        onActivityResult(0, RESULT_OK, null);
                    }
                }
                else {
                    start = false;
                    Intent intent = new Intent(this, VPN2.class);
                    //intent.putExtra("keyName", textLog);
                    Log.d(TAG, "stop VPNservice");
                    stopService(intent);
                }

                break;

            case R.id.buttonRules:
                i = new Intent(getApplicationContext(), RulesActivity.class);
                this.startActivity(i);
                //i.putExtra("key", "value");
                break;

            case R.id.buttonRules2:
                for (String s : rules){
                    Log.d("rules list content: ", s);
                }
                break;


            case R.id.buttonTrafficStats:
                i = new Intent(getApplicationContext(), ConnectionListActivity.class);
                this.startActivity(i);
                //i.putExtra("key", "value");
                break;

        }

    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            String prefix = getPackageName();

            /*
            Intent intent = new Intent(this, FirewallVpnService.class)
                    .putExtra(prefix + ".ADDRESS", mServerAddress.getText().toString())
                    .putExtra(prefix + ".PORT", mServerPort.getText().toString())
                    .putExtra(prefix + ".SECRET", mSharedSecret.getText().toString());
            */

            //Intent intent = new Intent(this, FirewallVpnService.class);

            Intent intent = new Intent(this, VPN2.class);
            //intent.putExtra("keyName", textLog);
            startService(intent);
        }
    }


    /*
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
*/

}
