package AWS_Classes.Dynamo.Account;

import com.TED.Account.model.AccountResponse;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

/**
 * Created by Niko on 4/21/2016.
 */
public interface LambdaInterface {

    @LambdaFunction(functionName = "App_regBear")
    AccountResponse regBear(BearRegData bearRegData);
}
