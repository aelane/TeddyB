package ted.tedparent;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import AWS_Classes.Dynamo.AsyncResponse;
import AWS_Classes.Dynamo.BearStateUpdate;
import AWS_Classes.Dynamo.Metrics;


public class HomeActivity extends AppCompatActivity implements AsyncResponse {

    EditText topicBox;
    EditText languageBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        topicBox = (EditText) findViewById(R.id.CurrTopic);
        languageBox = (EditText) findViewById(R.id.CurrLang);

        updateFields();
    }

    public void updateFields() {

        // Check if we are connected to wifi
        if (isNetworkAvailable()) {
            //Get our credentials in order to talk to our AWS database
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );



            BearStateUpdate myMapper = new BearStateUpdate(credentialsProvider);
            myMapper.delegate = this;
            myMapper.execute();

        }

    }

    public void processFinish(Metrics output){

        topicBox.setText(output.getTopic());
        languageBox.setText(output.getLanguage());

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}


