package ted.tedparent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import Helper_Classes.listWords;
import Helper_Classes.tedSingleton;

public class metricsDisplayActivity extends AppCompatActivity {

    TextView known;
    TextView trouble;
    TextView teachingMode;
    private Button back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics_display);

        known = (TextView) findViewById(R.id.known);
        trouble = (TextView) findViewById(R.id.trouble);
        teachingMode = (TextView) findViewById(R.id.teachingMode);
        back = (Button) findViewById(R.id.backButton);

        known.setText("Known Words: \n" + listWords.displayWords(tedSingleton.getInstance().getKnown()));
        trouble.setText("Trouble Words: \n" + listWords.displayWords(tedSingleton.getInstance().getTrouble()));
        teachingMode.setText(tedSingleton.getInstance().getTeachingMode());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(metricsDisplayActivity.this, MetricsActivity.class));
            }
        });
    }
}
