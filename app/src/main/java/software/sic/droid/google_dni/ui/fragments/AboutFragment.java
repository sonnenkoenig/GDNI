/*
 * sic.software
 * google-dni_smart_news
 *
 * @date Mär 2017
 */
package software.sic.droid.google_dni.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import software.sic.droid.google_dni.BuildConfig;
import software.sic.droid.google_dni.R;
import software.sic.droid.google_dni.data.data.LogEntry;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

/**
 * About fragment which shows the version number and the privacy policy if open source code is used we also show the
 * licenses in this fragment
 */
public class AboutFragment extends SmartNewsBaseFragment {

    public AboutFragment() {
        // Required empty public constructor
    }//c'tor

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        // Inflate the view
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        // Get the reference to the version number text view
        TextView versionNumberTextView = (TextView) view.findViewById(R.id.id_text_view_version_number);
        // Set the version number to the text view
        String userId = SmartNewsSharedPreferences.instance().getUserId();
        LogEntry.Algorithm algorithm = LogEntry.Algorithm.fromUserId(userId);
        String versionString = BuildConfig.VERSION_NAME + " " + BuildConfig.BUILD_TAG +" "+BuildConfig.VERSION_CODE ;
        if(null!=algorithm){
            versionString += ":"+algorithm.ordinal();
        }
        if(DEBUG){
            versionString +="(dbg)";
        }
        versionNumberTextView.setText(versionString);
        // Return the view
        return view;
    }//onCreateView
}//AboutFragment
