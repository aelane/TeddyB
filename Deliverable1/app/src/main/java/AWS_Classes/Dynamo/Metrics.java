package AWS_Classes.Dynamo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Niko on 11/10/2015.
 */
@DynamoDBTable(tableName = "Metrics")
public class Metrics {

    String UserID;
    String Language;

    @DynamoDBHashKey(attributeName = "UserID")
    public String getEnglish() {
        return UserID;
    }
    public void setEnglish(String s){
        this.UserID = s;
    }

    @DynamoDBAttribute(attributeName = "Language")
    public String getFarsi(){
        return Language;
    }
    public void setFarsi(String s){
        this.Language = s;
    }
}
