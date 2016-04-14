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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;

import java.util.ArrayList;
import java.util.List;

import AWS_Classes.Dynamo.Metrics.Metrics;
import AWS_Classes.Dynamo.Metrics.MetricsResponse;
import AWS_Classes.Dynamo.Metrics.MetricsSearch;
import Helper_Classes.makePieChart;
import Helper_Classes.tedSingleton;

public class MetricsActivity extends AppCompatActivity implements MetricsResponse {


    // Navigation Drawer Variables
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    private Spinner langSpinner;
    private Button btnSubmit;
    private Button repeatGraph;
    private Button foreignToGraph;
    private Button englishToGraph;
    private ProgressBar totalProgress = null;
    TextView repeatBox;
    TextView englishToBox;
    TextView foreignToBox;
    TextView allBox;

    public int count = 0;

    public List<String> knownRepeat = new ArrayList<String>();
    public List<String> knownForeignTo = new ArrayList<String>();
    public List<String> knownEnglishTo = new ArrayList<String>();
    public List<String> allKnownWords = new ArrayList<String>();
    public List<String> troubleRepeat = new ArrayList<String>();
    public List<String> troubleForeignTo = new ArrayList<String>();
    public List<String> troubleEnglishTo = new ArrayList<String>();
    public List<String> allTroubleWords = new ArrayList<String>();


    String currLang = tedSingleton.getInstance().getLanguage();


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

        //Spinner Setup - Set Default value to English
        langSpinner = (Spinner) findViewById(R.id.LangSpinner);
        btnSubmit = (Button) findViewById(R.id.button);

        ArrayAdapter langAdap = (ArrayAdapter) langSpinner.getAdapter(); //cast to an ArrayAdapter
        int spinnerPosition = langAdap.getPosition(currLang);
        langSpinner.setSelection(spinnerPosition);

        //Metrics Setup
        repeatBox = (TextView)findViewById(R.id.repeatAfterMe);
        foreignToBox = (TextView)findViewById(R.id.foreignToEnglish);
        englishToBox = (TextView)findViewById(R.id.englishToForeign);
        allBox = (TextView) findViewById(R.id.allModes);
        totalProgress = (ProgressBar) findViewById(R.id.totalProgress);

        repeatGraph = (Button)findViewById(R.id.repeatPie);
        foreignToGraph = (Button)findViewById(R.id.foreignToPie);
        englishToGraph = (Button)findViewById(R.id.englishToPie);

        //Calculate Metrics
        Log.d("Current Language ", currLang);
        calculateProgress();

         final Context myContext = this;


        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String languageStr = langSpinner.getSelectedItem().toString();
                tedSingleton.getInstance().setLanguage(languageStr);

                // Update metrics based on language
                startActivity(new Intent(MetricsActivity.this, MetricsActivity.class));

            }
        });

        repeatGraph.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent achartIntent = new makePieChart().execute(myContext, "Repeat After Me", knownRepeat, troubleRepeat);
                startActivity(achartIntent);
            }
        });

        foreignToGraph.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent achartIntent = new makePieChart().execute(myContext, "Foreign to English", knownForeignTo, troubleForeignTo);
                startActivity(achartIntent);
            }
        });

        englishToGraph.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent achartIntent = new makePieChart().execute(myContext, "English to Foreign", knownEnglishTo, troubleEnglishTo);
                startActivity(achartIntent);
            }
        });

    }


    private void addDrawerItems() {
        String[] osArray = { "Home", "Metrics", "Settings", "Sign Out" };
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
            case 2:
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
/*        for(Metrics item : result){
            Toast toast = Toast.makeText(getApplicationContext(), item.getDate(), Toast.LENGTH_LONG);
            toast.show();
            //Do ctrl+f on logcot and search for MetricsResults to see what you got for testing, easier to see than toasts
            Log.d("MetricsResult", "ID: " + item.getBearID() + " Date: "+ item.getDate() + " Word: " + item.getCorrectWord() + " Mode: " + item.getTeachingMode() +
                    " Language: " + item.getLanguage() + " Attempt: " + item.getAttempt() + " Length: " + result.size());
        }*/

        Toast toast = Toast.makeText(getApplicationContext(), String.valueOf(count), Toast.LENGTH_SHORT);
        //toast.show();

        int listSize = result.size();

        if (listSize > 1) {
            Metrics lastAttempt = result.get(listSize - 1);
            Metrics prevAttempt = result.get(listSize - 2);

            // Save known words in ArrayList
            if (lastAttempt.getCorrect() == true &&
                    prevAttempt.getCorrect() == true &&
                    lastAttempt.getAttempt() == 1 &&
                    prevAttempt.getAttempt() == 1) {
                switch (lastAttempt.getTeachingMode()) {
                    case "Repeat After Me":
                        knownRepeat.add(lastAttempt.getCorrectWord());
                        break;
                    case "Foreign to English":
                        knownForeignTo.add(lastAttempt.getCorrectWord());
                        break;
                    case "English to Foreign":
                        knownEnglishTo.add(lastAttempt.getCorrectWord());
                        break;
                }
            }
            // Save problem words in ArrayList
            else {
                switch (lastAttempt.getTeachingMode()) {
                    case "Repeat After Me":
                        troubleRepeat.add(lastAttempt.getCorrectWord());
                        break;
                    case "Foreign to English":
                        troubleForeignTo.add(lastAttempt.getCorrectWord());
                        break;
                    case "English to Foreign":
                        troubleEnglishTo.add(lastAttempt.getCorrectWord());
                        break;
                }
            }
        }
        //Post Calculations after all 51 words have been checked for each teaching teaching mode
        if (count >= 153) {
            //toast.cancel();
            repeatBox.append(String.valueOf(knownRepeat.size()) + "/51 words");
            foreignToBox.append(String.valueOf(knownForeignTo.size()) + "/51 words");
            englishToBox.append(String.valueOf(knownEnglishTo.size()) + "/51 words");

            // Create a list of common known words (known in all three teaching modes)
            List<String> tempKnown = intersection(knownRepeat, knownForeignTo);
            allKnownWords = intersection(tempKnown, knownEnglishTo);
            allBox.append(String.valueOf(allKnownWords.size()/51) + "%");

            totalProgress.setProgress(allKnownWords.size());

            // Create a list of common trouble words (trouble words in all three teaching modes)
            List<String> tempTrouble = intersection(troubleRepeat, troubleForeignTo);
            allTroubleWords = intersection(tempTrouble, troubleEnglishTo);

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
/*
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );
*/


            //Credentials for specific accounts
            CognitoCachingCredentialsProvider credentialsProvider = tedSingleton.getInstance().getCredentials();

            for (String teachingMode : teachingModes) {
                for (String word : vocab) {
                    MetricsSearch myMapper = new MetricsSearch(credentialsProvider);
                    myMapper.delegate = this;
                    myMapper.execute(teachingMode, currLang, word);
                }
            }
        }

    }

    public <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
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
