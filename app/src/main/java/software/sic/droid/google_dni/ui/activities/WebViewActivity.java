package software.sic.droid.google_dni.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebViewClient;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.R;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.data.event.UiEvent;
import software.sic.droid.google_dni.notification.NotificationHandler;
import software.sic.droid.google_dni.ui.VideoEnabledWebChromeClient;
import software.sic.droid.google_dni.ui.VideoEnabledWebViewWithLog;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

/**
 * WebView activity which displays the news from the intent extra in an web view
 */
public class WebViewActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://gdni.sic.software.w0123d63.kasserver.com/";
    // The news event to be displayed
    private NewsEvent mNewsEvent;
    // WebView to display the news in
    private VideoEnabledWebViewWithLog mWebView;
    private VideoEnabledWebChromeClient mWebChromeClient;

    private static final String TAG = "WebViewActivity";
    public static final String EXTRA_KEY_LOG_SHOWN_LAST = "LOG_SHOWN_LAST";//true if not launched from notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view
        setContentView(R.layout.activity_web_view);
        // Get the news event out of the extras
        final Intent intent = this.getIntent();
        this.mNewsEvent = (NewsEvent) intent.getSerializableExtra(NotificationHandler.NEWS_EVENT);

        if(null==mNewsEvent){
            if(DEBUG){Log.w(TAG,"missing NewsEvent in extras -> finish");}
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mNewsEvent.title);
        }

        this.setupWebView();
        // Display the news in the web view
        mWebView.load(this, mNewsEvent, BASE_URL);
        // Trigger the update of the news event in the database
        this.updateNewsEventInDatabase
                ( intent.getBooleanExtra(EXTRA_KEY_LOG_SHOWN_LAST , false)
                ? UiEvent.Action.SHOWN_LAST : UiEvent.Action.SHOWN
                );
    }//onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }//onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.id_menu_about:
                Intent aboutIntent = new Intent(this,MainActivity.class);
                aboutIntent.setAction(MainActivity.ACTION_ABOUT);
                startActivity(aboutIntent);
                return true;
            case R.id.id_menu_link_hst:
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
                websiteIntent.setData(Uri.parse("http://www.stimme.de"));
                startActivity(websiteIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }//onOptionsItemSelected

    @Override
    public void onBackPressed() {
        if (!mWebChromeClient.onBackPressed()) {

            if (this.mWebView.canGoBack()) {
                // If the user navigated in the web view, go back when the back button is pressed
                this.mWebView.goBack();
            } else {
                // If the user can not navigate back use the system default
                super.onBackPressed();
            }
        }
    }//onBackPressed

    @Override
    protected void onStop() {
        super.onStop();
        this.updateNewsEventInDatabase(UiEvent.Action.CLOSED);
    }//onStop

    private void setupWebView() {
        // Get the reference to the web view
        this.mWebView = (VideoEnabledWebViewWithLog) findViewById(R.id.id_web_view_news);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = findViewById(R.id.id_non_fullscreen_layout);
        ViewGroup videoLayout = (ViewGroup)findViewById(R.id.id_fullscreen_layout);

        mWebChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout);

        mWebChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen)
                {
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.hide();
                    }

                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE| View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
                else
                {
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }

                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

            }
        });
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(new WebViewClient());

    }//setupWebView

    /**
     * Updates the news event in the database. The news event got pressed so set the flag for it
     */
    public void updateNewsEventInDatabase(UiEvent.Action aAction) {
        final MyApplication myApp = MyApplication.instance();
        UiEvent uiEvent = new UiEvent(mNewsEvent, SmartNewsSharedPreferences.instance().getUserId(), aAction);
        myApp.mEngine.onUiEvent(uiEvent);
    }//updateNewsEventInDatabase

}//WebViewActivity
