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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;

import java.util.ArrayList;
import java.util.List;

import AWS_Classes.Dynamo.Metrics.Metrics;
import AWS_Classes.Dynamo.Metrics.MetricsResponse;
import AWS_Classes.Dynamo.Metrics.MetricsSearch;

public class MetricsActivity extends AppCompatActivity implements MetricsResponse{

    // Navigation Drawer Variables
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    TextView repeatBox;
    TextView englishToBox;
    TextView foreignToBox;

    public int count = 0;
    public int repeatAfterMeVal = 0;
    public int foreignToEnglishVal = 0;
    public int englishToForeignVal = 0;

    public List<String> correctRepeat = new ArrayList<String>();
    public List<String> correctForeignTo = new ArrayList<String>();
    public List<String> correctEnglishTo = new ArrayList<String>();
    public List<String> incorrectRepeat = new ArrayList<String>();
    public List<String> incorrectForeignTo = new ArrayList<String>();
    public List<String> incorrectEnglishTo = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics);

        // Navigation Drawer Setup
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        //Metrics Setup
        repeatBox = (TextView)findViewById(R.id.repeatAfterMe);
        foreignToBox = (TextView)findViewById(R.id.foreignToEnglish);
        englishToBox = (TextView)findViewById(R.id.englishToForeign);

        //Metrics Calculations
        calculateProgress();

        //Post Calculations
/*        repeatBox.append(String.valueOf(repeatAfterMeVal));
        foreignToBox.append(String.valueOf(foreignToEnglishVal));
        englishToBox.append(String.valueOf(englishToForeignVal));*/

    }

    private void addDrawerItems() {
        String[] osArray = { "Home", "Metrics", "Accounts", "Settings", "Sign Out" };
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
                startActivity(new Intent(MetricsActivity.this, HomeActivity.class));
                break;
            case 1:
                startActivity(new Intent(MetricsActivity.this, MetricsActivity.class));
                break;
            case 3:
                startActivity(new Intent(MetricsActivity.this, SettingsActivity.class));
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

    public void metricsFinish(PaginatedQueryList<Metrics> result){
        count +=1;
       for(Metrics item : result){
            Toast toast = Toast.makeText(getApplicationContext(), item.getDate(), Toast.LENGTH_LONG);
            toast.show();
            //Do ctrl+f on logcot and search for MetricsResults to see what you got for testing, easier to see than toasts
            Log.d("MetricsResult", "ID: " + item.getBearID() + " Date: "+ item.getDate() + " Word: " + item.getCorrectWord() + " Mode: " + item.getTeachingMode() +
                    " Language: " + item.getLanguage() + " Attempt: " + item.getAttempt() + " Length: " + result.size());
        }

        int listSize = result.size();

        if (listSize > 1) {
            Metrics lastAttempt = result.get(listSize - 1);
            Metrics prevAttempt = result.get(listSize - 2);

            Log.d("here: ", "I AM IN THE FIRST IF STATEMENT!");
            Log.d("Last Attempt: ", "Teaching Mode: " + lastAttempt.getTeachingMode() + " is correct: " + lastAttempt.getCorrect() + " Attempt: " + String.valueOf(lastAttempt.getAttempt()));
            Log.d("2nd to Last Attempt: ", "Teaching Mode: " + prevAttempt.getTeachingMode() + " is correct: " + prevAttempt.getCorrect() + " Attempt: " + String.valueOf(prevAttempt.getAttempt()));
            Log.d("Word: ", "correct word: " + prevAttempt.getCorrectWord());

            // Save known words
            if (lastAttempt.getCorrect() == true &&
                    prevAttempt.getCorrect() == true &&
                    lastAttempt.getAttempt() == 1 &&
                    prevAttempt.getAttempt() == 1) {
                Toast toast = Toast.makeText(getApplicationContext(), "here", Toast.LENGTH_LONG);
                Log.d("here: ", "I AM THE SECOND IF STATMENT!!!");
                toast.show();
                switch (lastAttempt.getTeachingMode()) {
                    case "Repeat After Me":
                        repeatAfterMeVal += 1;
                        correctRepeat.add(lastAttempt.getCorrectWord());
                        break;
                    case "Foreign to English":
                        foreignToEnglishVal += 1;
                        correctForeignTo.add(lastAttempt.getCorrectWord());
                        break;
                    case "English to Foreign":
                        englishToForeignVal += 1;
                        correctEnglishTo.add(lastAttempt.getCorrectWord());
                        break;
                }
            }
            // Save problem words
            else {
                Log.d("THE WORD NOT KNOWN ", lastAttempt.getCorrectWord());
                switch (lastAttempt.getTeachingMode()) {
                    case "Repeat After Me":
                        incorrectRepeat.add(lastAttempt.getCorrectWord());
                        Log.d("BETCHES", "here");
                        break;
                    case "Foreign to English":
                        incorrectForeignTo.add(lastAttempt.getCorrectWord());
                        break;
                    case "English to Foreign":
                        incorrectEnglishTo.add(lastAttempt.getCorrectWord());
                        break;
                }
            }
        }
        //Post Calculations after all 50 words have been checked for each teaching teaching mode
        if (count == 150) {
            repeatBox.append(String.valueOf(repeatAfterMeVal));
            foreignToBox.append(String.valueOf(foreignToEnglishVal));
            englishToBox.append(String.valueOf(englishToForeignVal));
            for (String word : incorrectRepeat) {
                Log.d("English to ", word);
            }

        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void calculateProgress() {

        String[] teachingModes = {"Repeat After Me", "Foreign to English", "English to Foreign"};
        String[] vocab = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
                "ten", "apple", "banana", "bread", "cake", "carrot", "cookie", "grape", "milk",
                "juice", "water", "bear", "bird", "cat", "dog", "elephant", "horse", "giraffe",
                "monkey", "pig", "cow", "head", "arm", "leg", "foot", "hand", "eye", "ear", "nose",
                "mouth", "stomach", "family", "mother", "father", "brother", "sister", "grandma",
                "grandpa", "bed", "chair", "table", "television"};

        if (isNetworkAvailable()) {
            //Get our credentials in order to talk to our AWS database
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );


            //Credentials for specific accounts
            //CognitoCachingCredentialsProvider credentialsProvider = mySingleton.getInstance().getCredentials()

            for (String teachingMode : teachingModes) {
                for (String word : vocab) {
                    Log.d("Searching for", teachingMode + " " + word);
                    MetricsSearch myMapper = new MetricsSearch(credentialsProvider);
                    myMapper.delegate = this;
                    myMapper.execute(teachingMode, "English", word);
                }
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


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

}
