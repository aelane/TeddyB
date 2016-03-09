package AWS_Classes.Cognito;

import android.os.AsyncTask;

import com.TED.Account.TEDAccountAPIClient;
import com.TED.Account.model.AccountResponse;
import com.TED.Account.model.UserData;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;


/**
 * Created by Niko on 1/10/2016.
 */
public class RegTask extends AsyncTask<UserData, Void, AccountResponse> {

    public RegTaskResponse delegate = null;

    public RegTask() {}

    @Override
    protected AccountResponse doInBackground(UserData... args) {

        ApiClientFactory factory = new ApiClientFactory().endpoint("https://hbq5qawvri.execute-api.us-east-1.amazonaws.com/Account");
        // Create a client.
        final TEDAccountAPIClient client = factory.build(TEDAccountAPIClient.class);

        // Invoke your parentPath1Get method.

        return client.regresourcePost(args[0]);


    }


    @Override
    protected void onPostExecute(AccountResponse result) {
        if(result == null){
            return;
        }
        delegate.regFinish(result);
    }
}
