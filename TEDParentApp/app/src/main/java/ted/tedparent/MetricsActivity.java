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
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;

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


        if (isNetworkAvailable()) {
            //Get our credentials in order to talk to our AWS database
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );


            //Credentials for specific accounts
            //CognitoCachingCredentialsProvider credentialsProvider = mySingleton.getInstance().getCredentials()

            MetricsSearch myMapper = new MetricsSearch(credentialsProvider);
            myMapper.delegate = this;
            myMapper.execute();
        }
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
//            case 4:
//                startActivity(new Intent(MetricsActivity.this, LoginActivity.class));
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
        for(Metrics item : result){
            Toast toast = Toast.makeText(getApplicationContext(), item.getDate(), Toast.LENGTH_LONG);
            toast.show();
            //Do ctrl+f on logcot and search for MetricsResults to see what you got for testing, easier to see than toasts
            Log.d("MetricsResult", "ID: " + item.getBearID() + " Date: "+ item.getDate() + " Mode: " + item.getTeachingMode() +
                    " Language: " + item.getLanguage());
        }

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
