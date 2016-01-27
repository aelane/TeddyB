package group15.deliverable1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;


import java.util.HashMap;

import AWS_Classes.Dynamo.AsyncResponse;
import AWS_Classes.Lambda.LambdaInterface;
import AWS_Classes.Lambda.LambdaResponse;
import AWS_Classes.LoginResponse;
import AWS_Classes.Lambda.LambdaAsyncReg;
import AWS_Classes.Lambda.LambdaAsyncLogin;
import AWS_Classes.UserData;
import AWS_Classes.Cognito.DeveloperAuthenticationProvider;
import AWS_Classes.Dynamo.Vocab;
import AWS_Classes.Dynamo.runMapper;

public class LoginActivity extends AppCompatActivity implements LambdaResponse, AsyncResponse  {

    private EditText usernameTxt, passTxt;
    EditText transBox;

    LambdaResponse myContext;
    AsyncResponse otherContext;
    String Operation = null;

    DeveloperAuthenticationProvider developerProvider;
    CognitoCachingCredentialsProvider credentialsProviderAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        usernameTxt = (EditText) findViewById(R.id.usernameText);
        passTxt = (EditText) findViewById(R.id.passwordText);
        transBox = (EditText) findViewById(R.id.translated);

        myContext = this;
        otherContext = this;


    }

    public void registerClick(View v){
        Operation = "Register";
        if (isNetworkAvailable()) {
            //Get our credentials in order to talk to our AWS database
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );
            //Create the lambda factory
            LambdaInvokerFactory factory = new LambdaInvokerFactory(
                    getApplicationContext(),
                    Regions.US_EAST_1,
                    credentialsProvider);
            //Create a new Async since it can only be used once.

            LambdaAsyncReg myLambda = new LambdaAsyncReg(factory.build(LambdaInterface.class));
            myLambda.delegate = myContext;
            UserData data = new UserData(usernameTxt.getText().toString(), passTxt.getText().toString());
            myLambda.execute(data);
        } else {
            transBox.setText("N/A");
        }
    }
    public void loginClick(View v){
        Operation = "Login";
        //Get our credentials in order to talk to our AWS database
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        //Create the lambda factory
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                getApplicationContext(),
                Regions.US_EAST_1,
                credentialsProvider);
        //Create a new Async since it can only be used once.
        developerProvider = new DeveloperAuthenticationProvider(
                usernameTxt.getText().toString(),
                "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014",
                getApplicationContext(),
                Regions.US_EAST_1);

        credentialsProviderAuth = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                developerProvider,
                Regions.US_EAST_1);

        LambdaAsyncLogin myLambda = new LambdaAsyncLogin(factory.build(LambdaInterface.class));
        myLambda.delegate = myContext;
        UserData data = new UserData(usernameTxt.getText().toString(), passTxt.getText().toString());
        myLambda.execute(data);


    }
    public void gotoMain(View v){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }

    public void lambdaFinish(LoginResponse output){
        switch(Operation){
            case "Register":
                if(output.getSuccess()) {
                    Toast.makeText(LoginActivity.this, "It worked", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(LoginActivity.this, "It sort of worked", Toast.LENGTH_LONG).show();
                }
                    break;
            case "Login":
                if(output.getSuccess()) {
                    developerProvider.setAuthData(output);
                    HashMap<String, String> loginsMap = new HashMap<String, String>();
                    loginsMap.put(developerProvider.getProviderName(), output.getToken());
                    Toast.makeText(LoginActivity.this, output.getID() + "ALSO" + output.getToken(), Toast.LENGTH_LONG).show();
                    credentialsProviderAuth.setLogins(loginsMap);
                    //credentialsProviderAuth.refresh();
                    runMapper myMapper = new runMapper(credentialsProviderAuth);
                    myMapper.delegate = otherContext;
                    //myMapper.execute("Apple", "Greek");


                }
                else{
                    Toast.makeText(LoginActivity.this, "Woe is I", Toast.LENGTH_LONG).show();
                }
                break;
            default:

            break;
        }
        //stuff
        Operation = null;
    }

    public void processFinish(Vocab output){
        Toast.makeText(LoginActivity.this, output.getGreek(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
