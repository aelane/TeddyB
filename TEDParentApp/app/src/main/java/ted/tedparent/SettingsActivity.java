package ted.tedparent;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

import AWS_Classes.Dynamo.AsyncResponse;
import AWS_Classes.Dynamo.Metrics;
import AWS_Classes.Dynamo.SettingsUpdate;

public class SettingsActivity extends AppCompatActivity implements AsyncResponse {

    private Spinner Language, Topic;
    private Button btnSubmit;

    AsyncResponse myContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Language = (Spinner) findViewById(R.id.LangSpinner);
        Topic = (Spinner) findViewById(R.id.TopicSpinner);
        btnSubmit = (Button) findViewById(R.id.SettingsButton);

        myContext = this;
        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String languageStr = Language.getSelectedItem().toString();
                String topicStr = Topic.getSelectedItem().toString();
                //Check if we are connected to wifi
                if (isNetworkAvailable()) {
                    //Get our credentials in order to talk to our AWS database
                    CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                            getApplicationContext(),
                            "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                            Regions.US_EAST_1 // Region
                    );
                    SettingsUpdate myMapper = new SettingsUpdate(credentialsProvider);
                    myMapper.delegate = myContext;
                    myMapper.execute(languageStr, topicStr);
                }
            }
        });
    }

    public void processFinish(Metrics output){}

/*    public boolean onCreateOptionsMenu(Menu menu) {
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
    }*/

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
