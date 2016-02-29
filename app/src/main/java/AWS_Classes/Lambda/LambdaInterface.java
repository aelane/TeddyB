package AWS_Classes.Lambda;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

import AWS_Classes.LoginResponse;
import AWS_Classes.UserData;

/**
 * Created by Niko on 1/10/2016.
 */
public interface LambdaInterface {

    @LambdaFunction(functionName = "App_regUser")
    LoginResponse regUser(UserData userData);

    @LambdaFunction(functionName = "App_Login")
    LoginResponse callLogin(UserData userData);

}
