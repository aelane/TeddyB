package ted.tedparent;

import android.app.Application;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;

/**
 * Created by pwdarby on 3/30/16.
 *
 * Works like a common storage box reachable by any activity for global variables and methods.
 */

public class mySingleton extends Application {
    private volatile static mySingleton mInstance = null;

    private String testString;
    private CognitoCachingCredentialsProvider credentials;

    private mySingleton() {
        testString = "TEST AUTHENTICATION"; //initialize your var here
        credentials = null;
        //Add all the variables you need, here.
    }
    public static mySingleton getInstance(){  //Singleton's core
        if(mInstance == null){
            mInstance = new mySingleton();
        }
        return mInstance;
    }

    //Get and Set methods here
    public String getMystring(){return this.testString;}
    public CognitoCachingCredentialsProvider getCredentials() {return this.credentials;}

    public void setMystring(String s){testString = s;}
    public void setCredentials(CognitoCachingCredentialsProvider c){credentials = c;}


}