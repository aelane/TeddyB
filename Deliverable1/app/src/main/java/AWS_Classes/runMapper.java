package AWS_Classes;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by Niko on 11/8/2015.
 */
public class runMapper extends AsyncTask<String, Void, Vocab> {

    protected CognitoCachingCredentialsProvider credentialsProvider;
    public Vocab word;
    public AsyncResponse delegate = null;

    public runMapper(){}
    public runMapper(CognitoCachingCredentialsProvider credentials){
        credentialsProvider = credentials;
    }
    public void setCredentials(CognitoCachingCredentialsProvider credentials){
        credentialsProvider = credentials;
    }

    @Override
    public Vocab doInBackground(String... args) {
        //Set up our credentials and pass it to our db client.

        AmazonDynamoDB ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        Metrics stuff = new Metrics();
        stuff.UserID = "001";
        stuff.Language = args[1];
        mapper.save(stuff);

        Vocab selectedWord = mapper.load(Vocab.class, args[0].toLowerCase());


        return selectedWord;
    }

    @Override
    protected void onPostExecute(Vocab result) {
        delegate.processFinish(result);
    }
}