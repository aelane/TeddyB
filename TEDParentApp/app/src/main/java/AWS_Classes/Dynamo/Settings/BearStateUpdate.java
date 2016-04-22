package AWS_Classes.Dynamo.Settings;

import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import Helper_Classes.tedSingleton;

/**
 * Created by Niko on 11/8/2015.
 */
public class BearStateUpdate extends AsyncTask<String, Void, BearData> {

    protected CognitoCachingCredentialsProvider credentialsProvider;
    public AsyncResponse delegate = null;

    public BearStateUpdate(CognitoCachingCredentialsProvider credentials){
        credentialsProvider = credentials;
    }

    @Override
    protected BearData doInBackground(String... args) {
        //Set up our credentials and pass it to our db client.

        AmazonDynamoDB ddbClient = new AmazonDynamoDBClient(tedSingleton.getInstance().getCredentials());
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        BearData userData = mapper.load(BearData.class, tedSingleton.getInstance().getBearID());

        return userData;
    }

    @Override
    protected void onPostExecute(BearData result) {
        delegate.processFinish(result);
    }
}