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

    private String BearID;
    private String Date;
    private String CorrectWord;
    private String TeachingMode;
    private String WordSaid;
    private Integer Attempt;
    private Boolean Correct;
    private String Topic;
    private String Language;

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
    public Integer getAttempt() {
        return Attempt;
    }
    public void setAttempt(Integer s){
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

    @DynamoDBAttribute(attributeName = "Language")
    public String getLanguage(){
        return Language;
    }
    public void setLanguage(String s){
        this.Language = s;
    }

}
