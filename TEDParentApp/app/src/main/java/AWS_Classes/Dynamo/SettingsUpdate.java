package AWS_Classes.Dynamo;

import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;


/**
 * Created by pwdarby on 3/21/16.
 *
 * Updates the settings in the AWS database
 */

public class SettingsUpdate extends AsyncTask<String, Void, Metrics> {
    protected CognitoCachingCredentialsProvider credentialsProvider;
    public AsyncResponse delegate = null;

    public SettingsUpdate(CognitoCachingCredentialsProvider credentials){
        credentialsProvider = credentials;
    }

    @Override
    protected Metrics doInBackground(String... args) {
        //Set up our credentials and pass it to our db client.

        AmazonDynamoDB ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        Metrics userData = mapper.load(Metrics.class, "001");
        userData.Language = args[0];
        userData.Topic = args[1];
        mapper.save(userData);

        Metrics blank = new Metrics();
        return blank;
    }

    @Override
    protected void onPostExecute(Metrics result) {
        delegate.processFinish(result);
    }
}