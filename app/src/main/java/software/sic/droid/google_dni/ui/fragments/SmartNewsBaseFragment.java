/*
 * sic.software
 * google-dni_smart_news
 *
 * @date Apr 2017
 */
package software.sic.droid.google_dni.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import software.sic.droid.google_dni.ui.listeners.SmartNewsFragmentListener;

/**
 * Base fragment which implements the base functionality each fragment needs to have.
 */
public class SmartNewsBaseFragment extends Fragment {
    // The fragment listener which handles base functions
    SmartNewsFragmentListener mSmartNewsFragmentListener;

    public SmartNewsBaseFragment() {
        // Required empty public constructor
    }//c'tor

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the fragment listener
        this.mSmartNewsFragmentListener = (SmartNewsFragmentListener) getActivity();
    }//onCreate

    public void showFragment(Fragment fragment, boolean addToBackStack) {
        this.mSmartNewsFragmentListener.showFragment(fragment, addToBackStack);
    }//showFragment
}//SmartNewsBaseFragment
