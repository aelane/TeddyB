package ted.tedparent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import Helper_Classes.listWords;
import Helper_Classes.tedSingleton;

public class metricsDisplayActivity extends AppCompatActivity {

    TextView known;
    TextView trouble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics_display);

        known = (TextView) findViewById(R.id.known);
        trouble = (TextView) findViewById(R.id.trouble);

        known.setText("Known Words: \n" + listWords.displayWords(tedSingleton.getInstance().getKnown()));
        trouble.setText("Trouble Words: \n" + listWords.displayWords(tedSingleton.getInstance().getTrouble()));
    }
}
