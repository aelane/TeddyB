package AWS_Classes.Cognito;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;

/**
 * Created by Niko on 1/19/2016.
 */
public interface RefreshResponse {

    void RefreshFinish(CognitoCachingCredentialsProvider output);

}
