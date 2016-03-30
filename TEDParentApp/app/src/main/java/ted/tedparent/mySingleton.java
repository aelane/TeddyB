package ted.tedparent;

import android.app.Application;

/**
 * Created by pwdarby on 3/30/16.
 *
 * Works like a common storage box reachable by any activity for global variables and methods.
 */

public class mySingleton extends Application {
    private volatile static mySingleton mInstance = null;
    private String authString;

    private mySingleton() {
        authString = "TEST AUTHENTICATION"; //initialize your var here
        //Add all the variables you need, here.
    }
    public static mySingleton getInstance(){  //Singleton's core
        if(mInstance == null){
            mInstance = new mySingleton();
        }
        return mInstance;
    }

    //Place Set and Get methods here
    public String getMystring(){return this.authString;}
    public void setMystring(String s){authString = s;}
//Add get/setmethods for your other variables here
} //Thats it