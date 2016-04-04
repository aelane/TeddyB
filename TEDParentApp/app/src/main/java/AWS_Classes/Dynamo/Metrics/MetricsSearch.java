package AWS_Classes.Dynamo.Metrics;

import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import AWS_Classes.Dynamo.Settings.AsyncResponse;

/**
 * Created by Niko on 4/3/2016.
 */
public class MetricsSearch extends AsyncTask<String, Void, Metrics> {

    protected CognitoCachingCredentialsProvider credentialsProvider;
    public MetricsResponse delegate = null;

    public MetricsSearch(CognitoCachingCredentialsProvider credentials){
        credentialsProvider = credentials;
    }

    @Override
    protected Metrics doInBackground(String... args) {
        //Set up our credentials and pass it to our db client.

        AmazonDynamoDB ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);


        return null;
    }

    @Override
    protected void onPostExecute(Metrics result) {
        delegate.metricsFinish(result);
    }


}
