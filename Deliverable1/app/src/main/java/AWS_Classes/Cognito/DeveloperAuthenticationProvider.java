package AWS_Classes.Cognito;

import android.content.Context;

import com.amazonaws.regions.Regions;
import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;

import AWS_Classes.Lambda.LambdaInterface;
import AWS_Classes.Lambda.LambdaResponse;
import AWS_Classes.LoginResponse;
import AWS_Classes.UserData;

/**
 * Created by Niko on 1/10/2016.
 */
public class DeveloperAuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider implements LambdaResponse {

    private static final String developerProvider = "login.TED.TEDapp";
    private LoginResponse AuthData = null;
    protected LambdaInterface myInterface;
    private UserData Data;

    public DeveloperAuthenticationProvider(String accountId,
                                           String identityPoolId, Context context, Regions region)  {
        super(accountId, identityPoolId, region);
        // Initialize any other objects needed here.
    }

    // Return the developer provider name which you choose while setting up the
    // identity pool in the Amazon Cognito Console

    @Override
    public String getProviderName() {
        return developerProvider;
    }

    // Use the refresh method to communicate with your backend to get an
    // identityId and token.

    @Override
    public String refresh() {

        // Override the existing token
        setToken(null);

        // Get the identityId and token by making a call to your backend
        // (Call to your backend)

            // Call the update method with updated identityId and token to make sure
            // these are ready to be used from Credentials Provider.
            update(AuthData.getID(), AuthData.getToken());
            return AuthData.getToken();



    }

    // If the app has a valid identityId return it, otherwise get a valid
    // identityId from your backend.

    @Override
    public String getIdentityId() {

        // Load the identityId from the cache
        identityId = AuthData.getID();

        if (identityId == null) {
            // Call to your backend
            return identityId;
        } else {
            return identityId;
        }

    }

    public void setUserData(UserData newData){
        Data = newData;
    }
    public void setMyInterface(LambdaInterface newInterface){
        myInterface = newInterface;
    }
    public void setAuthData(LoginResponse data){
        AuthData = data;
    }
    public void lambdaFinish(LoginResponse output){
        //stuff
        AuthData = output;
    }
}
