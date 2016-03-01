package group15.deliverable1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

import AWS_Classes.Dynamo.AsyncResponse;
import AWS_Classes.Dynamo.runMapper;
import AWS_Classes.Dynamo.Vocab;

public class MainActivity extends AppCompatActivity implements AsyncResponse{

    private Spinner spinner1, spinner2;
    private Button btnSubmit, changeView;
    EditText transBox;

    AsyncResponse myContext;

    String languageStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        spinner2 = (Spinner) findViewById(R.id.wordSpinn);
        btnSubmit = (Button) findViewById(R.id.transButton);
        changeView = (Button) findViewById(R.id.loginViewButton);
        transBox = (EditText) findViewById(R.id.translated);

        myContext = this;


    }

    public void translateClick(View v) {

        String topicStr = spinner2.getSelectedItem().toString();
            //Check if we are connected to wifi
        if (isNetworkAvailable()) {
            //Get our credentials in order to talk to our AWS database
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            //Create a new Async since it can only be used once.
            runMapper myMapper = new runMapper(credentialsProvider);
            myMapper.delegate = myContext;
            myMapper.execute(topicStr);
        } else {
            transBox.setText("N/A");
        }


    }
    public void viewClick(View v){
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }
    public void processFinish(Vocab output){
        //Here we will receive the result from our async class
        //of onPostExecute(result) method.
        /*
        if (output != null) {
            switch (languageStr) {
                case "French":
                    transBox.setText(output.getFrench());
                    break;
                case "Spanish":
                    transBox.setText(output.getSpanish());
                    break;
                case "Greek":
                    transBox.setText(output.getGreek());
                    break;
            }
        }
        else{
            transBox.setText("N/A");
        }
        */
    }

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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
