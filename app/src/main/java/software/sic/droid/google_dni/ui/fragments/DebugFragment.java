/*
 * www.sic.software
 *
 * 17.03.17
 */
package software.sic.droid.google_dni.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import software.sic.droid.google_dni.BuildConfig;
import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.R;
import software.sic.droid.google_dni.data.RssFeedReader;
import software.sic.droid.google_dni.services.SmartNewsService;
import software.sic.droid.google_dni.simulation.NewsSimulator;

public class DebugFragment extends SmartNewsBaseFragment {
    // Instance of the application
    final MyApplication mMyApplication = MyApplication.instance();
    // The context
    private Context mContext;
    // Reference to the text view to display the debug information in
    private TextView mStatusTextView;
    // Field to hold the old text while evaluating the new text
    private String mOldText;

    public DebugFragment() {
        // Required empty public constructor
    }//DebugFragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the view from the layout file
        View view = inflater.inflate(R.layout.fragment_debug, container, false);
        // Get the reference of the text view
        this.mStatusTextView = (TextView) view.findViewById(R.id.id_text_view_debug);
        // Get the reference to the floating action button
        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id
                .id_floating_action_button);
        // Register the on click listener to the floating action button
        floatingActionButton.setOnClickListener(new OnClickListener());
        // Update the status text
        doUpdateStatusText();
        // Return the view
        return view;
    }//onCreateView

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }//onAttach

    @Override
    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }//onDetach

    public void doUpdateStatusText() {
        final Context context = this.mContext;
        String newText;
        //evtl. besser // if(!isResumed()){return;}

        if (context != null) {
            try {
                String appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                newText = "appVersion=" + appVersion + "\n"
                        + "build(" + BuildConfig.BUILD_TYPE + ") " + BuildConfig.BUILD_BY + " @ " + BuildConfig
                        .BUILD_TIME + "\n"
                        + "tag: " + BuildConfig.BUILD_TAG + "\n"
                        + mMyApplication.mEngine.getLastSensorStatusAsText() + "\n"
                        + mMyApplication.mStatus + "\n"
                        + mMyApplication.mStatistic + "\n"
                ;
            } catch (Exception e) {
                newText = e.toString();
            }

            if (!newText.equals(mOldText)) {
                mStatusTextView.setText(mOldText = newText);
            }
        }
    }//doUpdateStatusText

    /**
     * OnClickListener for all on click events. Switches over the id of the clicked view.
     */
    private class OnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.id_floating_action_button:
                    //for debugging
                    NewsSimulator.startService(v.getContext());
                    SmartNewsService.startService(v.getContext());
                    RssFeedReader.startService(v.getContext());
                    break;
                default:
                    break;
            }
        }//onClick
    }//OnClickListener
}//DebugFragment
