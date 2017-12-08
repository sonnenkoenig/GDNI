/*
 * sic.software
 * google-dni_smart_news
 *
 * @date Apr 2017
 */
package software.sic.droid.google_dni.ui.listeners;

import android.support.v4.app.Fragment;

/**
 * Listener interface to connect the fragments with the underlying activity
 */
public interface SmartNewsFragmentListener {

    void showFragment(Fragment fragment, boolean addToBackStack);
//    void showDefaultScreen();
}//interface SmartNewsFragmentListener
