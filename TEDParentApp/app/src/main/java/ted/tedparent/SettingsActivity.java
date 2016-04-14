package ted.tedparent;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

import AWS_Classes.Dynamo.Settings.AsyncResponse;
import AWS_Classes.Dynamo.Settings.BearData;
import AWS_Classes.Dynamo.Settings.BearStateUpdate;
import AWS_Classes.Dynamo.Settings.SettingsUpdate;
import Helper_Classes.tedSingleton;

public class SettingsActivity extends AppCompatActivity implements AsyncResponse {

    private Spinner LanguageSpinn, TopicSpinn, TeachingModeSpinn;
    private Button btnSubmit;

    private String currLanguage, currTopic, currTeachingMode;

    // Navigation Drawer Variables
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    AsyncResponse myContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LanguageSpinn = (Spinner) findViewById(R.id.LangSpinner);
        TopicSpinn = (Spinner) findViewById(R.id.TopicSpinner);
        TeachingModeSpinn = (Spinner) findViewById(R.id.TeachingModeSpinner);
        btnSubmit = (Button) findViewById(R.id.SettingsButton);

        // Navigation Drawer Setup
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        myContext = this;

        updateSpinners();


        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String languageStr = LanguageSpinn.getSelectedItem().toString();
                String topicStr = TopicSpinn.getSelectedItem().toString();
                String teachingModeStr = TeachingModeSpinn.getSelectedItem().toString();
                //Check if we are connected to wifi
                if (isNetworkAvailable()) {
                    //Get our credentials in order to talk to our AWS database
                    CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                            getApplicationContext(),
                            "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                            Regions.US_EAST_1 // Region
                    );


                    //Credentials for specific accounts
                    //CognitoCachingCredentialsProvider credentialsProvider = mySingleton.getInstance().getCredentials()

                    SettingsUpdate myMapper = new SettingsUpdate(credentialsProvider);
                    myMapper.delegate = myContext;
                    myMapper.execute(languageStr, topicStr, teachingModeStr);
                }
            }
        });

    }

    //public void processFinish(BearData output){}

    public void updateSpinners() {

        // Check if we are connected to wifi
        if (isNetworkAvailable()) {
            //Get our credentials in order to talk to our AWS database
/*            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );*/

            //Credentials for specific accounts
            CognitoCachingCredentialsProvider credentialsProvider = tedSingleton.getInstance().getCredentials();

            BearStateUpdate myMapper = new BearStateUpdate(credentialsProvider);
            myMapper.delegate = this;
            myMapper.execute();

        }
        else {
        }
    }

    public void processFinish(BearData output){

        // Make the default values of each spinner the current values in AWS
        currTopic = output.getTopic();
        currLanguage = output.getLanguage();
        currTeachingMode = output.getTeachingMode();

        ArrayAdapter langAdap = (ArrayAdapter) LanguageSpinn.getAdapter(); //cast to an ArrayAdapter
        int spinnerPosition = langAdap.getPosition(currLanguage);
        //set the default according to value
        LanguageSpinn.setSelection(spinnerPosition);

        ArrayAdapter topicAdap = (ArrayAdapter) TopicSpinn.getAdapter(); //cast to an ArrayAdapter
        int spinnerPosition2 = topicAdap.getPosition(currTopic);
        //set the default according to value
        TopicSpinn.setSelection(spinnerPosition2);

        ArrayAdapter teachingModeAdap = (ArrayAdapter) TeachingModeSpinn.getAdapter(); //cast to an ArrayAdapter
        int spinnerPosition3 = teachingModeAdap.getPosition(currTeachingMode);
        //set the default according to value
        TeachingModeSpinn.setSelection(spinnerPosition3);
    }

    private void addDrawerItems() {
        String[] osArray = { "Home", "Metrics", "Settings", "Log Out" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Change the activity based on the selected item
                changeActivity(position);
            }
        });
    }

    public void changeActivity(int position) {
        switch (position) {
            case 0:
                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                break;
            case 1:
                startActivity(new Intent(SettingsActivity.this, MetricsActivity.class));
                break;
            case 2:
                startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                break;
//            case 4:
//                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            default:
                break;
        }
    }

    private void setupDrawer() {
        // Initialize mDrawerToggle
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }


    // Figure out why these don't work (also in Metrics Activity and Home Activity)

/*    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

/*        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
           }*/

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
