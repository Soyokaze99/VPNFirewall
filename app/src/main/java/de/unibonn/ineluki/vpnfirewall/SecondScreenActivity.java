package de.unibonn.ineluki.vpnfirewall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ineluki on 13/08/2014.
 */
public class SecondScreenActivity extends Activity {

    //ListView mainListView;
    private ListView mainListView ;
    private ArrayAdapter<String> listAdapter ;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen2);

        Log.e("Second Screen","onCreate");

        Button btnClose = (Button) findViewById(R.id.btnClose);

        mainListView = (ListView) findViewById( R.id.mainListView );

        btnClose.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //Closing SecondScreen Activity
                finish();
            }
        });


        // Create and populate a List of planet names.
        String[] planets = new String[] { "Mercury", "Venus", "Earth", "Mars",
                                              "Jupiter", "Saturn", "Uranus", "Neptune"};
        ArrayList<String> planetList = new ArrayList<String>();
        planetList.addAll( Arrays.asList(planets) );
        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<String>(this, R.layout.rowlayout, planetList);

            // Add more planets. If you passed a String[] instead of a List<String>
            // into the ArrayAdapter constructor, you must not add more items.
            // Otherwise an exception will occur.
           listAdapter.add( "Ceres" );
            listAdapter.add( "Pluto" );
            listAdapter.add( "Haumea" );
            listAdapter.add( "Makemake" );
            listAdapter.add( "Eris" );
// Set the ArrayAdapter as the ListView's adapter.
           mainListView.setAdapter( listAdapter );


        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a,
                                    View v, int position, long id) {
                String str = (String) a.getItemAtPosition(position);
                Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                intent.putExtra("deunibonnvpn", str);
                startActivity(intent);
            }
        });



    }


}
