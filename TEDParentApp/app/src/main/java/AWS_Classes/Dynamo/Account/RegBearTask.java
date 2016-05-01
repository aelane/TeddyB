package AWS_Classes.Dynamo.Account;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.TED.Account.model.AccountResponse;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import Helper_Classes.tedSingleton;
import AWS_Classes.Dynamo.Account.RegBearResponse;


/**
 * Created by Niko on 4/20/2016.
 */
public class RegBearTask extends AsyncTask<String, Void, AccountResponse> {

    public RegBearResponse delegate = null;
    protected LambdaInterface myInterface;

    public RegBearTask(Context s){
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                s,
                Regions.US_EAST_1,
                tedSingleton.getInstance().getCredentials());

        myInterface = factory.build(LambdaInterface.class);
    }

    @Override
    protected AccountResponse doInBackground(String... args) {

        BearRegData data = new BearRegData();
        data.setUsername(tedSingleton.getInstance().getUsername());
        data.setBearID(args[0]);
        try {
            return myInterface.regBear(data);
        }
        catch (LambdaFunctionException lfe) {
            Log.d("TAG", "Failed to invoke reg", lfe);
            return null;
        }
    }


    @Override
    protected void onPostExecute(AccountResponse result) {

            delegate.regBearFinish(result);
    }

}
