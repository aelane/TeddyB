package AWS_Classes.Dynamo.Metrics;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by Niko on 11/10/2015.
 */
@DynamoDBTable(tableName = "Metrics")
public class Metrics {

    String BearID;
    String Date;
    String CorrectWord;
    String TeachingMode;
    String WordSaid;
    int Attempt;
    Boolean Correct;
    String Topic;

    @DynamoDBHashKey(attributeName = "BearID")
    public String getBearID() {return BearID;}
    public void setBearID(String s){this.BearID = s;}

    @DynamoDBRangeKey(attributeName = "Date")
    public String getDate() {
        return Date;
    }
    public void setDate(String s){
        this.Date = s;
    }

    @DynamoDBAttribute(attributeName = "CorrectWord")
    public String getCorrectWord() {
        return CorrectWord;
    }
    public void setCorrectWord(String s){
        this.CorrectWord = s;
    }

    @DynamoDBAttribute(attributeName = "TeachingMode")
    public String getTeachingMode() {
        return TeachingMode;
    }
    public void setTeachingMode(String s){
        this.TeachingMode = s;
    }

    @DynamoDBAttribute(attributeName = "WordSaid")
    public String getWordSaid() {
        return WordSaid;
    }
    public void setWordSaid(String s){
        this.WordSaid = s;
    }

    @DynamoDBAttribute(attributeName = "Attempt")
    public int getAttempt() {
        return Attempt;
    }
    public void setAttempt(int s){
        this.Attempt = s;
    }

    @DynamoDBAttribute(attributeName = "Correct")
    public Boolean getCorrect(){
        return Correct;
    }
    public void setCorrect(Boolean s){
        this.Correct = s;
    }

    @DynamoDBAttribute(attributeName = "Topic")
    public String getTopic(){
        return Topic;
    }
    public void setTopic(String s){
        this.Topic = s;
    }
}
