package de.unibonn.ineluki.vpnfirewall;

/**
 * Created by Ineluki on 06/08/2014.
 */

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import android.app.Activity;
        import android.os.Bundle;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TableLayout;
        import android.widget.TableRow;
        import android.widget.TableRow.LayoutParams;
        import android.widget.TextView;
        import android.widget.Toast;

public class DynamicTable extends Activity {

    TableLayout table_layout;
    EditText rowno_et, colno_et;
    Button build_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dynamictable);

        rowno_et = (EditText) findViewById(R.id.rowno_id);
        colno_et = (EditText) findViewById(R.id.colno_id);
        build_btn = (Button) findViewById(R.id.build_btn_id);
        table_layout = (TableLayout) findViewById(R.id.tableLayout1);

        build_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                String rowstring = rowno_et.getText().toString();
                String colstring = colno_et.getText().toString();

                if (!rowstring.equals("") && !colstring.equals("")) {
                    int rows = Integer.parseInt(rowstring);
                    int cols = Integer.parseInt(colstring);
                    table_layout.removeAllViews();
                    BuildTable(rows, cols);
                }

                else {
                    Toast.makeText(DynamicTable.this,
                            "Please Enter the row and col Numbers",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void BuildTable(int rows, int cols) {

        // outer for loop
        for (int i = 1; i <= rows; i++) {

            TableRow row = new TableRow(this);
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));

            // inner for loop
            for (int j = 1; j <= cols; j++) {

                TextView tv = new TextView(this);
                tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
                tv.setBackgroundResource(R.drawable.cell_shape);
                tv.setPadding(5, 5, 5, 5);
                tv.setText("R " + i + ", C" + j);

                row.addView(tv);

            }

            table_layout.addView(row);

        }
    }

}