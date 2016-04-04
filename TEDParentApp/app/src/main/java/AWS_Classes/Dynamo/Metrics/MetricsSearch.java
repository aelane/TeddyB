package AWS_Classes.Dynamo.Metrics;

import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

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

        Metrics metricsToFind = new Metrics();
        metricsToFind.setBearID("Charles Dickens");

        String queryString = "Great";

        Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.BEGINS_WITH.toString())
                .withAttributeValueList(new AttributeValue().withS(queryString.toString()));

        DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                .withHashKeyValues(metricsToFind)
                .withRangeKeyCondition("Title", rangeKeyCondition)
                .withConsistentRead(false);

        PaginatedQueryList<Metrics> result = mapper.query(Metrics.class, queryExpression);

        return null;
    }

    @Override
    protected void onPostExecute(Metrics result) {
        delegate.metricsFinish(result);
    }


}
