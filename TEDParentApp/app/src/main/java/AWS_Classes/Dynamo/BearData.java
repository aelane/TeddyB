package AWS_Classes.Dynamo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Niko on 11/10/2015.
 * Updated by Paige on 2/29/2016
 */
@DynamoDBTable(tableName = "BearData")
public class BearData {

    String BearID;
    String Language;
    String Topic;
    String TeachingMode;

    @DynamoDBHashKey(attributeName = "BearID")
    public String getBearID() {
        return BearID;
    }
    public void setBearID(String s){
        this.BearID = s;
    }

    @DynamoDBAttribute(attributeName = "Language")
    public String getLanguage(){
        return Language;
    }
    public void setLanguage(String s){this.Language = s;}

    @DynamoDBAttribute(attributeName = "Topic")
    public String getTopic() { return Topic; }
    public void setTopic(String s) {this.Topic = s; }

    @DynamoDBAttribute(attributeName = "TeachingMode")
    public String getTeachingMode() { return Topic; }
    public void setTeachingMode(String s) {this.Topic = s; }

}
