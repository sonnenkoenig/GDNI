/*
 * sic.software
 * google-dni_smart_news
 *
 * @date Feb 2017
 */
package software.sic.droid.google_dni.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.UUID;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.data.DebugAppStatus;
import software.sic.droid.google_dni.simulation.NewsSimulator;

/**
 * Shared preferences for the smart news app
 */
public class SmartNewsSharedPreferences {
    // Name of the shared preferences
    private static final String SMART_NEWS_SHARED_PREFERENCES = "smart_news_shared_preferences";
    // Fields to identify entries in the shared preferences
    private static final String SMART_NEWS_SHARED_PREFERENCES_USER_UUID = "user_uuid";
    private static final String SMART_NEWS_SHARED_PREFERENCES_EMAIL_ADDRESS = "email_address";
    private static final String SMART_NEWS_SHARED_PREFERENCES_AGE = "age";
    private static final String SMART_NEWS_SHARED_PREFERENCES_JOB = "job";
    private static final String KEY_UPLOAD_REGISTRATION_DATA = "registrationDataUploaded";
    private static final String SMART_NEWS_SHARED_PREFERENCES_SIMULATE_USER = "simulate_user";
    private static final String SMART_NEWS_SHARED_PREFERENCES_SIMULATE_NEWS = "simulate_news";
    // Debug user strings
    private static final  @NonNull String EMPTY_STRING = "";
    private static final String DEBUG_USER_EMAIL = "debug.user@0112358";
    private static final String SMART_NEWS_SHARED_PREFERENCES_LAST_ETAG = "last_etag";
    // Instance of itself
    private static SmartNewsSharedPreferences sInstance;
    // Shared preferences
    private final SharedPreferences mSharedPreferences;
    // UUID of the user
    private final String mUuid;

    private SmartNewsSharedPreferences(@NonNull Context context) {
        // Get the shared preferences
        this.mSharedPreferences = context.getSharedPreferences(SMART_NEWS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        // Initialize the shared preferences
        mUuid = initializeSharedPreferences();
        final DebugAppStatus s = MyApplication.instance().mStatus;
        s.mUserId = mUuid;
        s.mUserSimulationIsActivated = mSharedPreferences.getBoolean(SMART_NEWS_SHARED_PREFERENCES_SIMULATE_USER,false);
        s.mNewsSimulationIsActivated = mSharedPreferences.getBoolean(SMART_NEWS_SHARED_PREFERENCES_SIMULATE_NEWS,false);
    }//c'tor

    @NonNull
    public static SmartNewsSharedPreferences instance() {
        if (sInstance == null) {
            sInstance = new SmartNewsSharedPreferences(MyApplication.instance());
        }
        return sInstance;
    }//instance

    /**
     * Checks if there is a uuid stored already, if not a new one is created here
     */
    @NonNull
    private String initializeSharedPreferences() {
        String uuid = this.mSharedPreferences.getString(SMART_NEWS_SHARED_PREFERENCES_USER_UUID, null);

        if (uuid == null) {
            // Create a new user id
            uuid = UUID.randomUUID().toString();

            // Clear the shared preferences before creating anything new
            final SharedPreferences.Editor editor = this.mSharedPreferences.edit().clear();
            editor.putString(SMART_NEWS_SHARED_PREFERENCES_USER_UUID, uuid);
            // Apply the changes
            editor.apply();
        }
        // Return the uuid
        return uuid;
    }//initializeSharedPreferences

    /**
     * Gets the user uuid stored in the shared preferences
     *
     * @return The uuid for the user
     */
    @NonNull
    public String getUserId() {
        // Return the uuid
        return this.mUuid;
    }//getUserId

    /**
     * Stores the user information into the shared preferences
     *
     * @param emailAddress  Email address of the user
     * @param age
     * @param job
     */
    public void saveUserInformation(@NonNull String emailAddress, @Nullable String age, @Nullable String job) {
        // Get the editor for the shared preferences
        final SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        // Put the values in the shared preferences
        editor.putString(SMART_NEWS_SHARED_PREFERENCES_EMAIL_ADDRESS, emailAddress);
        editor.putString(SMART_NEWS_SHARED_PREFERENCES_AGE, age);
        editor.putString(SMART_NEWS_SHARED_PREFERENCES_JOB, job);
        editor.remove(KEY_UPLOAD_REGISTRATION_DATA);
        // Apply the changes;
        editor.apply();
    }//saveUserInformation

    /**
     * Checks if the required user information is already stored
     *
     * @return Flag if the required user information is already stored or not
     */
    public boolean isUserInformationStoredAlready() {
//        if(true){return false;}//FIXME
        return  null != this.mSharedPreferences.getString(SMART_NEWS_SHARED_PREFERENCES_EMAIL_ADDRESS, null);
    }//isUserInformationStoredAlready

    public boolean isUserInformationUploadedAlready() {
        return this.mSharedPreferences.getBoolean(KEY_UPLOAD_REGISTRATION_DATA, false);
    }//isUserInformationUploadedAlready


    public void setUserInformationUploadedAlready() {
        final SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putBoolean(KEY_UPLOAD_REGISTRATION_DATA, true);
        editor.apply();
    }//setUserInformationUploadedAlready

    public @NonNull String getJob(){
        return  this.mSharedPreferences.getString(SMART_NEWS_SHARED_PREFERENCES_JOB, EMPTY_STRING);
    }//getJob
    public @NonNull String getAge(){
        return  this.mSharedPreferences.getString(SMART_NEWS_SHARED_PREFERENCES_AGE, EMPTY_STRING);
    }//getAge
    public @NonNull String getEMail(){
        return  this.mSharedPreferences.getString(SMART_NEWS_SHARED_PREFERENCES_EMAIL_ADDRESS, EMPTY_STRING);
    }//getEMail

    /**
     * Checks whether the user entered the debug parameters during the registration or not
     *
     * @return If the user is a debug user or not
     */
    public boolean isDebugUser() {
        return this.mSharedPreferences.getString(SMART_NEWS_SHARED_PREFERENCES_EMAIL_ADDRESS, EMPTY_STRING).contains(DEBUG_USER_EMAIL);
    }//isDebugUser

    /**
     * Stores the ETag of a received HTTP response
     *
     * @param etag ETag as String
     */
    public void saveLastETag(String etag) {
        // Get the editor for the shared preferences
        final SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        // Put the values in the shared preferences
        editor.putString(SMART_NEWS_SHARED_PREFERENCES_LAST_ETAG, etag);
        // Apply the changes;
        editor.apply();
    }//saveLastEtag

    /**
     * Returns the Value stored in the ETag preference
     * Returns null if no Preference exists yet
     *
     * @return ETag as String
     */
    @SuppressWarnings("unused")
    public String getCurrentStoredEtag() {
        return mSharedPreferences.getString(SMART_NEWS_SHARED_PREFERENCES_LAST_ETAG, null);
    }//getCurrentStoredEtag

    public void toggleUserSimulationIsActivated(){
        final MyApplication myApp = MyApplication.instance();
        final boolean set = myApp.mStatus.mUserSimulationIsActivated = !myApp.mStatus.mUserSimulationIsActivated;
        final SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putBoolean(SMART_NEWS_SHARED_PREFERENCES_SIMULATE_USER, set);
        editor.apply();
        Toast.makeText(myApp ,"UserSimulation="+set,Toast.LENGTH_SHORT).show();
    }//setUserSimulationIsActivated

    public void toggleNewsSimulationIsActivated(){
        final MyApplication myApp = MyApplication.instance();
        final boolean set = myApp.mStatus.mNewsSimulationIsActivated = !myApp.mStatus.mNewsSimulationIsActivated;
        final SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putBoolean(SMART_NEWS_SHARED_PREFERENCES_SIMULATE_NEWS, set);
        editor.apply();
        Toast.makeText(myApp ,"NewsSimulation="+set,Toast.LENGTH_SHORT).show();
        NewsSimulator.queueTimer(myApp);
    }//setNewsSimulationIsActivated

}//SmartNewsSharedPreferences
