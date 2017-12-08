/*
 * sic.software
 * google-dni_smart_news
 *
 * @author dg
 * @date Mär 2017
 */
package software.sic.droid.google_dni.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.R;
import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.notification.NotificationHandler;
import software.sic.droid.google_dni.ui.activities.WebViewActivity;


/**
 * Main fragment shown if the registration is completed
 */
public class MainFragment extends SmartNewsBaseFragment {
    // Reference fields for the ui elements
    private TextView mLastNewsLinkTextView;
    private Button mWebsiteButton;
    private ImageButton mAboutImageButton;

    public MainFragment() {
        // Required empty public constructor
    }//c'tor

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        // Get the references to the ui elements
        this.mLastNewsLinkTextView = (TextView) view.findViewById(R.id.id_text_view_last_news_link);
        this.mWebsiteButton = (Button) view.findViewById(R.id.id_button_website);
        this.mAboutImageButton = (ImageButton) view.findViewById(R.id.id_image_button_about);
        // Initialize the views eg. register onclick listener, set texts
        this.initializeViews();

        doUpdateNews();

        // Return the view
        return view;
    }//onCreateView

    /**
     * Initializes all the view of the fragment like registering on click listeners and stuff
     */
    private void initializeViews() {
        // Register the on click listener to the text view
        this.mLastNewsLinkTextView.setOnClickListener(new ButtonOnClickListener());
        // Register the on click listener to the buttons
        this.mWebsiteButton.setOnClickListener(new ButtonOnClickListener());
        this.mAboutImageButton.setOnClickListener(new ButtonOnClickListener());
    }//initializeViews

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;
    }//onAttach

    @Override
    public void onDetach() {
        super.onDetach();
        mContext=null;
    }//onDetach

    public void doUpdateNews() {
        //evtl. besser // if(!isResumed()){return;}
        if(mContext == null || mLastNewsLinkTextView == null){
            //avoid NullPointerException
            return;
        }
        MyApplication app = MyApplication.instance();
        Database database = app.acquireDatabase();

        NewsEvent latestNewsEvent = database.getLatestNewsEvent(null);
        if (latestNewsEvent != null) {
            this.mLastNewsLinkTextView.setText(latestNewsEvent.title);
        }
        app.releaseDatabase();
    }//doUpdateNews

    private class ButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.id_text_view_last_news_link:
                    NewsEvent latestNewsEvent;
                    MyApplication app = MyApplication.instance();
                    Database database = app.acquireDatabase();
                    latestNewsEvent = database.getLatestNewsEvent(null);
                    app.releaseDatabase();
                    if(null != latestNewsEvent) {
                        Intent newsIntent = new Intent(getActivity(), WebViewActivity.class);
                        // Set the news id to the intent
                        newsIntent.putExtra(NotificationHandler.NEWS_EVENT, latestNewsEvent);
                        newsIntent.putExtra(WebViewActivity.EXTRA_KEY_LOG_SHOWN_LAST, true);
                        // Start web view activity with the intent
                        getActivity().startActivity(newsIntent);
                    }
                    break;
                case R.id.id_button_website:
                    String url = "http://www.stimme.de";
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
                    websiteIntent.setData(Uri.parse(url));
                    startActivity(websiteIntent);
                    break;
                case R.id.id_image_button_about:
                    // Show the about fragment
                    showFragment(new AboutFragment(), true);
                    break;
                default:
                    break;
            }
        }//onClick
    }//ButtonOnClickListener
}//MainFragment
