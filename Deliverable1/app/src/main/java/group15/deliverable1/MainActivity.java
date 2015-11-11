package group15.deliverable1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.util.Log;
import android.app.Activity;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner1, spinner2;
    private Button btnSubmit;
    EditText transBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        spinner1 = (Spinner) findViewById(R.id.langSpinn);
        spinner2 = (Spinner) findViewById(R.id.wordSpinn);
        btnSubmit = (Button) findViewById(R.id.transButton);
        transBox = (EditText) findViewById(R.id.translated);
        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String languageStr = spinner1.getSelectedItem().toString();
                String wordStr = spinner2.getSelectedItem().toString();
                String translatedStr = "Test";
                transBox.setText(translatedStr);
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
