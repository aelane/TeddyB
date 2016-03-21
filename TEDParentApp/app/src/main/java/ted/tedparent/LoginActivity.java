package ted.tedparent;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.TED.Account.model.AccountResponse;
import com.TED.Account.model.UserData;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

import AWS_Classes.Cognito.*;

public class LoginActivity extends AppCompatActivity implements RegTaskResponse, LoginTaskResponse {

    private EditText usernameTxt, passTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usernameTxt = (EditText) findViewById(R.id.usernameText);
        passTxt = (EditText) findViewById(R.id.passwordText);
    }


    public void registerClick(View v){
        if (isNetworkAvailable()) {

            RegTask register = new RegTask();
            register.delegate = this;

            UserData data = new UserData();
            data.setUsername(usernameTxt.getText().toString());
            data.setPassword(passTxt.getText().toString());

            register.execute(data);
        } else {
            //transBox.setText("N/A");
        }
    }
    public void loginClick(View v){

        UserData data = new UserData();
        data.setUsername(usernameTxt.getText().toString());
        data.setPassword(passTxt.getText().toString());

        LoginTask login = new LoginTask();
        login.delegate = this;

        login.execute(data);


    }

    public void gotoMain(View v){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

    public void regFinish(AccountResponse output){

        if(output.getSuccess()) {
            Toast.makeText(LoginActivity.this, "It worked", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(LoginActivity.this, "It sort of worked", Toast.LENGTH_LONG).show();
        }

        // Toast.makeText(LoginActivity.this, "Woe is I", Toast.LENGTH_LONG).show();
    }

    public void loginFinish(DeveloperAuthenticationProvider output){

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider( getApplicationContext(),
                output,
                Regions.US_EAST_1);



    }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
