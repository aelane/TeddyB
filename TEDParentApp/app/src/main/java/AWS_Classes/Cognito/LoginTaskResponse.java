package AWS_Classes.Cognito;

import AWS_Classes.Cognito.DeveloperAuthenticationProvider;

/**
 * Created by Niko on 3/6/2016.
 */
public interface LoginTaskResponse {
    void loginFinish(DeveloperAuthenticationProvider output);
}
