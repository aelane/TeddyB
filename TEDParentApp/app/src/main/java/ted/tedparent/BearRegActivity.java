package ted.tedparent;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.TED.Account.model.AccountResponse;

import AWS_Classes.Dynamo.Account.RegBearResponse;
import AWS_Classes.Dynamo.Account.RegBearTask;

public class BearRegActivity extends AppCompatActivity implements RegBearResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bear_reg);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void regBearClick(View v){

        RegBearTask register = new RegBearTask(this.getApplicationContext());
        register.delegate = this;
        EditText bear = (EditText) findViewById(R.id.bearIDtxtBox);
        register.execute(bear.getText().toString());

    }

    public void regBearFinish(AccountResponse output){
        if(output.getSuccess()){
            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(i);
        }
        else{
            Toast.makeText(BearRegActivity.this, "Invalid Bear ID", Toast.LENGTH_LONG).show();
        }
    }
}
