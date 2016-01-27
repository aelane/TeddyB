package AWS_Classes.Lambda;

import AWS_Classes.LoginResponse;

/**
 * Created by Niko on 1/16/2016.
 */
public interface LambdaResponse {
    void lambdaFinish(LoginResponse output);
}
