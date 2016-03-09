package AWS_Classes.Cognito;

import com.TED.Account.TEDAccountAPIClient;
import com.TED.Account.model.LoginResponse;
import com.TED.Account.model.UserData;
import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.regions.Regions;

/**
 * Created by Niko on 1/10/2016.
 */
public class DeveloperAuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider {

    private static final String developerProvider = "login.TED.TEDapp";
    private UserData Data;
    private String cachedID = null;

    public DeveloperAuthenticationProvider(String accountId, String identityPoolId, Regions region, UserData newData) {
        super(accountId, identityPoolId, region);
        Data = newData;
        // Initialize any other objects needed here.

    }

    // Return the developer provider name which you choose while setting up the
    // identity pool in the &COG; Console

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

        ApiClientFactory factory = new ApiClientFactory().endpoint("https://hbq5qawvri.execute-api.us-east-1.amazonaws.com/Account");
        // Create a client.
        final TEDAccountAPIClient client = factory.build(TEDAccountAPIClient.class);

        // Invoke your parentPath1Get method.

        LoginResponse Response = client.loginresourcePost(Data);

        // Call the update method with updated identityId and token to make sure
        // these are ready to be used from Credentials Provider.


        update(Response.getID(), Response.getToken());
        //update(identityId, token);
        cachedID = Response.getID();
        return Response.getToken();

    }

    // If the app has a valid identityId return it, otherwise get a valid
    // identityId from your backend.

    @Override
    public String getIdentityId() {

        // Load the identityId from the cache
        identityId = cachedID;

        if (identityId == null) {
            ApiClientFactory factory = new ApiClientFactory().endpoint("https://hbq5qawvri.execute-api.us-east-1.amazonaws.com/Account");
            // Create a client.
            final TEDAccountAPIClient client = factory.build(TEDAccountAPIClient.class);

            // Invoke your parentPath1Get method.
            LoginResponse Response = client.loginresourcePost(Data);
            return Response.getID();
        } else {
            return identityId;
        }

    }
}