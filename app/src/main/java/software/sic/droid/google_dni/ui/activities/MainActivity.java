/* www.sic.software
 *
 * @file StatusActivity.java
 * @date 2017-01-25
 * @brief show status of current app
 */
package software.sic.droid.google_dni.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.R;
import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.RssFeedReader;
import software.sic.droid.google_dni.data.data.LogEntry;
import software.sic.droid.google_dni.data.data.SensorStatus;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.notification.NotificationHandler;
import software.sic.droid.google_dni.ui.fragments.AboutFragment;
import software.sic.droid.google_dni.ui.fragments.DebugFragment;
import software.sic.droid.google_dni.ui.fragments.MainFragment;
import software.sic.droid.google_dni.ui.fragments.RegistrationFragment;
import software.sic.droid.google_dni.ui.listeners.SmartNewsFragmentListener;
import software.sic.droid.google_dni.util.PermissionRequests;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

public class MainActivity extends AppCompatActivity implements SmartNewsFragmentListener {
    static final String TAG = "MainActivity";
    static final String ACTION_ABOUT = "software.sic.droid.google_dni.action.About";
    // Instance of the application
    private final MyApplication mMyApplication = MyApplication.instance();
    // Instance of the smart news shared preferences
    private final SmartNewsSharedPreferences mSmartNewsSharedPreferences = SmartNewsSharedPreferences.instance();
    // Boolean flag to register when the activity is resumed
    private boolean isResumed;
    // Runnable to update the ui of the status content fragment
    private final Runnable mUiUpdater = new Runnable() {
        @Override
        public void run() {
            if (isResumed) {
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof DebugFragment) {
                        ((DebugFragment) fragment).doUpdateStatusText();
                        continue;
                    }
                    if (fragment instanceof MainFragment) {
                        ((MainFragment) fragment).doUpdateNews();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(DEBUG){Log.i(TAG,"onCreate");}
        super.onCreate(savedInstanceState);
        // Set the content view
        setContentView(R.layout.activity_main);
        // Get the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(ACTION_ABOUT.equals(getIntent().getAction())){
            this.showFragment(new AboutFragment(), false);
            return;
        }

        // Check which fragment has to be shown, either registration or the main fragment
        final boolean needRegistration = !this.mSmartNewsSharedPreferences.isUserInformationStoredAlready();
        final Fragment fragment = needRegistration  ? new RegistrationFragment() : new AboutFragment();

        // Check if all required permissions are granted and request them if not
        PermissionRequests.checkAndRequestPermissions(this);
        if(DEBUG){Log.i(TAG,"needRegistration="+needRegistration);}

        if(!needRegistration){
            //see #22113: - show always web-view
            NewsEvent latestNewsEvent;
            MyApplication app = MyApplication.instance();
            Database database = app.acquireDatabase();
            latestNewsEvent = database.getLatestNewsEvent(null);
            app.releaseDatabase();
            if(null != latestNewsEvent) {
                Intent newsIntent = new Intent(this, WebViewActivity.class);
                // Set the news id to the intent
                newsIntent.putExtra(NotificationHandler.NEWS_EVENT, latestNewsEvent);
                newsIntent.putExtra(WebViewActivity.EXTRA_KEY_LOG_SHOWN_LAST, true);
                // Start web view activity with the intent
                this.startActivity(newsIntent);
                finish();
                return;
            }
        }
        // Show the fragment
        this.showFragment(fragment, false);
    }//onCreate

    @Override
    protected void onResume() {
        super.onResume();
        // Activity resumed set the flag
        isResumed = true;
        // Register the ui updater
        mMyApplication.registerUiUpdater(mUiUpdater);
    }//onResume

    @Override
    protected void onPause() {
        // Activity paused set the flag
        isResumed = false;
        // Unregister the ui updater
        mMyApplication.unRegisterUiUpdater(mUiUpdater);
        super.onPause();
    }//onPause

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show the options menu if the user is a debug user
        if(mSmartNewsSharedPreferences.isDebugUser()) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_debug, menu);
            return true;
        }
        return false;
    }//onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mMyApplication.postUpdateUi();
        // Switch over all entries of the options menu
        switch (item.getItemId()) {
            case R.id.id_debug_screen:
                boolean isDebugFragmentActive = false;
                // Check if the debugging fragment is already active before showing it
                for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof DebugFragment) {
                        isDebugFragmentActive = true;
                    }
                }
                if (!isDebugFragmentActive) {
                    this.showFragment(new DebugFragment(), true);
                }
                return true;
            case R.id.id_start_stop_news_simulation:
                SmartNewsSharedPreferences.instance().toggleNewsSimulationIsActivated();
                return true;
            case R.id.id_start_stop_user_simulation:
                SmartNewsSharedPreferences.instance().toggleUserSimulationIsActivated();
                return true;
            case R.id.id_start_stop_turbo_polling:
                Toast.makeText(this
                        ,"TurboPolling="
                            +(this.mMyApplication.mStatus.mTurboPollingIsActivated = !this.mMyApplication.mStatus.mTurboPollingIsActivated)
                            +" "+(RssFeedReader.TURBO_POLLING_INTERVAL_MILLIS/1000)
                        ,Toast.LENGTH_SHORT
                ).show();
                RssFeedReader.queueTimer(this);
                return true;
            case R.id.id_change_algorithm:
                LogEntry.Algorithm.sDebugOffset++;
                Toast.makeText(this
                        ,"Algorithm="+ LogEntry.Algorithm.fromUserId(mMyApplication.mStatus.mUserId)
                        ,Toast.LENGTH_SHORT
                ).show();
                return true;
            case R.id.id_menu_entry_registration:
                showFragment(new RegistrationFragment(), false);
                return true;
            case R.id.debug_text_news:
                postDebugNews(NewsEvent.Type.TEXT);
                return true;
            case R.id.debug_picture_news:
                postDebugNews(NewsEvent.Type.PICTURE);
                return true;
            case R.id.debug_video_news:
                postDebugNews(NewsEvent.Type.VIDEO);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }//onOptionsItemSelected

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        // Start the permission request
        PermissionRequests.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }//onRequestPermissionsResult

    @Override
    public void showFragment(Fragment fragment, boolean addToBackStack) {
        // Get the fragment manager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Begin a fragment transaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace the current fragment
        fragmentTransaction.replace(R.id.id_fragment_container, fragment, fragment.getTag());
        // If back navigation to the last fragment should be available add it to the back stack
        if (addToBackStack) fragmentTransaction.addToBackStack(fragment.getTag());
        // Commit the transaction
        fragmentTransaction.commit();
    }//showFragment

    private void postDebugNews(NewsEvent.Type type) {

        NewsEvent newsEvent = getDebugNewsEvent(type);
        mMyApplication.mEngine.onNewsEvent(newsEvent);

        SensorStatus sensorStatus = mMyApplication.mEngine.getSensorStatus();
        mMyApplication.showNotification(new MyApplication.NewsAndSensor(newsEvent, sensorStatus));
        NotificationHandler.instance().showNotification(this, newsEvent);

    }//postDebugNews

    /**
     * Creates a {@link NewsEvent} of the specified {@link software.sic.droid.google_dni.data.event.NewsEvent.Type}
     * for Debug purposes
     *
     * @param type The {@link software.sic.droid.google_dni.data.event.NewsEvent.Type} of the {@link NewsEvent} to be
     *             created
     * @return The requested {@link NewsEvent}
     */
    private NewsEvent getDebugNewsEvent(NewsEvent.Type type) {
        return NewsEvent.createDummyNews(type, this);
    }

}//class MainActivity
//#############################################################################
//eof MainActivity.java
