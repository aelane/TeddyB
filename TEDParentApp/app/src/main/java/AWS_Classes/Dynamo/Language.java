package AWS_Classes.Dynamo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;

/**
 * Created by pwdarby on 3/21/16.
 */
public class Language {
    private String English;
    private String Farsi;
    private String French;
    private String Greek;
    private String Spanish;

    @DynamoDBHashKey(attributeName = "English")
    public String getEnglish() {
        return English;
    }
    public void setEnglish(String s){
        this.English = s;
    }

    @DynamoDBAttribute(attributeName = "Farsi")
    public String getFarsi(){
        return Farsi;
    }
    public void setFarsi(String s){
        this.Farsi = s;
    }

    @DynamoDBAttribute(attributeName = "French")
    public String getFrench(){
        return French;
    }
    public void setFrench(String s){
        this.French = s;
    }

    @DynamoDBAttribute(attributeName = "Greek")
    public String getGreek(){
        return Greek;
    }
    public void setGreek(String s){
        this.Greek = s;
    }

    @DynamoDBAttribute(attributeName = "Spanish")
    public String getSpanish(){
        return Spanish;
    }
    public void setSpanish(String s){
        this.Spanish = s;
    }

}
