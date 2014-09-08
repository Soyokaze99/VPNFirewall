package de.unibonn.ineluki.vpnfirewall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

/**
 * Created by Ineluki on 13/08/2014.
 */

public class ConnectionStats extends ExpandableListActivity {

    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.conn_stats_main);

            SimpleExpandableListAdapter expListAdapter =
                    new SimpleExpandableListAdapter(
                            this,
                            createGroupList(),              // Creating group List.
                            R.layout.group_row,             // Group item layout XML.
                            new String[] { "Group Item" },  // the key of group item.
                            new int[] { R.id.row_name },    // ID of each group item.-Data under the key goes into this TextView.
                            createChildList(),              // childData describes second-level entries.
                            R.layout.child_row,             // Layout for sub-level entries(second level).
                            new String[] {"Sub Item"},      // Keys in childData maps to display.
                            new int[] { R.id.grp_child}     // Data under the keys above go into these TextViews.
                    );
            setListAdapter( expListAdapter );       // setting the adapter in the list.

        }catch(Exception e){
            System.out.println("Errrr +++ " + e.getMessage());
        }
    }

    /* Creating the Hashmap for the row */
    @SuppressWarnings("unchecked")
    private List createGroupList() {
        ArrayList result = new ArrayList();
        for( int i = 0 ; i < 15 ; ++i ) { // 15 groups........
            HashMap m = new HashMap();
            m.put( "Group Item","Group Item " + i ); // the key and it's value.
            result.add( m );
        }
        return (List)result;
    }

    /* creatin the HashMap for the children */
    @SuppressWarnings("unchecked")
    private List createChildList() {

        ArrayList result = new ArrayList();
        for( int i = 0 ; i < 15 ; ++i ) { // this -15 is the number of groups(Here it's fifteen)
          /* each group need each HashMap-Here for each group we have 3 subgroups */
            ArrayList secList = new ArrayList();
            for( int n = 0 ; n < 3 ; n++ ) {
                HashMap child = new HashMap();
                child.put( "Sub Item", "Sub Item " + n );
                secList.add( child );
            }
            result.add( secList );
        }
        return result;
    }
    public void  onContentChanged  () {
        System.out.println("onContentChanged");
        super.onContentChanged();
    }
    /* This function is called on each child click */
    public boolean onChildClick( ExpandableListView parent, View v, int groupPosition,int childPosition,long id) {
        System.out.println("Inside onChildClick at groupPosition = " + groupPosition +" Child clicked at position " + childPosition);
        return true;
    }

    /* This function is called on expansion of the group */
    public void  onGroupExpand  (int groupPosition) {
        try{
            System.out.println("Group exapanding Listener => groupPosition = " + groupPosition);
        }catch(Exception e){
            System.out.println(" groupPosition Errrr +++ " + e.getMessage());
        }
    }
}
