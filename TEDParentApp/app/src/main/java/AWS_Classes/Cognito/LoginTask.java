package AWS_Classes.Cognito;

import android.os.AsyncTask;

import com.TED.Account.model.UserData;
import com.amazonaws.regions.Regions;

import java.util.HashMap;

import AWS_Classes.Cognito.DeveloperAuthenticationProvider;

/**
 * Created by Niko on 1/12/2016.
 */
public class LoginTask extends AsyncTask<UserData, Void, DeveloperAuthenticationProvider> {

    public LoginTaskResponse delegate = null;

    public LoginTask(){}

    @Override
    protected DeveloperAuthenticationProvider doInBackground(UserData... args) {

        DeveloperAuthenticationProvider developerProvider = new DeveloperAuthenticationProvider( null,
                "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014",
                Regions.US_EAST_1,
                args[0]);
        if(developerProvider.refresh() != null) {

            HashMap<String, String> loginsMap = new HashMap<String, String>();
            loginsMap.put(developerProvider.getProviderName(), args[0].getUsername());

            developerProvider.setLogins(loginsMap);

            return developerProvider;
        }
        else{
            return null;
        }
    }


    @Override
    protected void onPostExecute(DeveloperAuthenticationProvider result) {
        if(result == null){
            delegate.loginFinish(null);
        }
        else {
            delegate.loginFinish(result);
        }
    }

}
