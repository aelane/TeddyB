package AWS_Classes.Dynamo.Settings;

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

public class SettingsUpdate extends AsyncTask<String, Void, BearData> {
    protected CognitoCachingCredentialsProvider credentialsProvider;
    public AsyncResponse delegate = null;

    public SettingsUpdate(CognitoCachingCredentialsProvider credentials){
        credentialsProvider = credentials;
    }

    @Override
    protected BearData doInBackground(String... args) {
        //Set up our credentials and pass it to our db client.

        AmazonDynamoDB ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        BearData userData = mapper.load(BearData.class, "001");
        userData.Language = args[0];
        userData.Topic = args[1];
        userData.TeachingMode = args[2];
        mapper.save(userData);

        BearData blank = new BearData();
        return blank;
    }

    @Override
    protected void onPostExecute(BearData result) {
        delegate.processFinish(result);
    }
}