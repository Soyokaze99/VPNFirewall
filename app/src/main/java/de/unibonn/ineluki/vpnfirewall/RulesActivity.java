package de.unibonn.ineluki.vpnfirewall;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ineluki on 20/08/2014.
 */


import java.util.ArrayList;
        import java.util.List;

        import android.os.Bundle;
        import android.app.Activity;
        import android.app.ListActivity;
        import android.view.Menu;
        import android.view.View;
        import android.widget.ArrayAdapter;
        import android.widget.EditText;

public class RulesActivity extends ListActivity {
    EditText et;
    String listItem[] = {"192.168.178.22"};
    List values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_list1);
        et = (EditText) findViewById(R.id.editText);

        values = startActivity.rules;



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    public void onClick(View view) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) getListAdapter();
        String str;
        switch (view.getId()) {
            case R.id.addItem:
                //List myList = new ArrayList();
                str = et.getText().toString();
                //myList.add(str);
                adapter.add(str);
                et.setText("");
                break;
            case R.id.exit:
                finish();
                break;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu., menu);
        return false;
    }
}