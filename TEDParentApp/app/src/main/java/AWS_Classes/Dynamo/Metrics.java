package AWS_Classes.Dynamo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Niko on 11/10/2015.
 * Updated by Paige on 2/29/2016
 */
@DynamoDBTable(tableName = "Metrics")
public class Metrics {

    String UserID;
    String Language;
    String Topic;

    @DynamoDBHashKey(attributeName = "UserID")
    public String getUserID() {
        return UserID;
    }
    public void setUserID(String s){
        this.UserID = s;
    }

    @DynamoDBAttribute(attributeName = "Language")
    public String getLanguage(){
        return Language;
    }
    public void setLanguage(String s){this.Language = s;}

    @DynamoDBAttribute(attributeName = "Topic")
    public String getTopic() { return Topic; }
    public void setTopic(String s) {this.Topic = s; }

}
