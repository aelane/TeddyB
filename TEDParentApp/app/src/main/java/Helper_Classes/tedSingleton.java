package Helper_Classes;

import android.app.Application;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;

/**
 * Created by pwdarby on 3/30/16.
 *
 * Works like a common storage box reachable by any activity for global variables and methods.
 */

public class tedSingleton extends Application {
    private volatile static tedSingleton mInstance = null;

    private String testString;
    private int testInt;
    private CognitoCachingCredentialsProvider credentials;

    private tedSingleton() {
        testString = "TEST AUTHENTICATION"; //initialize your var here
        credentials = null;
        //Add all the variables you need, here.
    }
    public static tedSingleton getInstance(){  //Singleton's core
        if(mInstance == null){
            mInstance = new tedSingleton();
        }
        return mInstance;
    }

    //Get and Set methods here
    public String getMystring(){return this.testString;}
    public CognitoCachingCredentialsProvider getCredentials() {return this.credentials;}

    public int getMyTest(){return this.testInt;}
    public void setMyTest(int s){testInt = s;}

    public void setMystring(String s){testString = s;}
    public void setCredentials(CognitoCachingCredentialsProvider c){credentials = c;}


}