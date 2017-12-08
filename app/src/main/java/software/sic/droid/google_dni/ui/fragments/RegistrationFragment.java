package software.sic.droid.google_dni.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.R;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

public class RegistrationFragment extends SmartNewsBaseFragment {

    private static final String TAG="RegistrationFragment";
    // Input fields to get the user information
    private EditText mEmailAddressEditText;
    private EditText mAgeEditText;
    private Spinner mJobSelector;
    private CheckBox mTermsOfUse;

    // Register button pressed by the user to store the user information
    private Button mRegisterButton;
    // Reference to the smart news shared preferences
    private SmartNewsSharedPreferences mSmartNewsSharedPreferences;

    //private OnRegistrationCompleteListener mCallback;

    public RegistrationFragment() {
        // Required empty public constructor
    }//c'tor

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the shared preferences
        this.mSmartNewsSharedPreferences = SmartNewsSharedPreferences.instance();
    }//onCreate

//TODO //es fehlt noch der link mit den Nutzungsbedingungen // see http://stackoverflow.com/a/21701364/7773146

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        // Get the references to all edit texts
        this.mEmailAddressEditText = (EditText) view.findViewById(R.id.id_edit_text_email_address);
        this.mAgeEditText = (EditText) view.findViewById(R.id.id_edit_text_age);
        mTermsOfUse = (CheckBox) view.findViewById(R.id.checkBox_termsOfUse);
        mTermsOfUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateButtonState();
            }
        });

        mJobSelector = (Spinner) view.findViewById(R.id.spinner_job);

        // Create a new text watcher to observe edit texts
        EditTextTextWatcher editTextTextWatcher = new EditTextTextWatcher();
        // Set the text watcher as changed listeners to the edit texts
        this.mEmailAddressEditText.addTextChangedListener(editTextTextWatcher);
        // Get the reference to the register button
        this.mRegisterButton = (Button) view.findViewById(R.id.id_button_register);
        // Create a new on click listener for the register button
        ButtonOnClickListener buttonOnClickListener = new ButtonOnClickListener();
        // Register on click listener to the register button
        this.mRegisterButton.setOnClickListener(buttonOnClickListener);

        if(mSmartNewsSharedPreferences.isDebugUser()){
            mEmailAddressEditText.setText( mSmartNewsSharedPreferences.getEMail(), TextView.BufferType.EDITABLE);
            mAgeEditText.setText( mSmartNewsSharedPreferences.getAge(), TextView.BufferType.EDITABLE);
        }

        // Return the view
        return view;
    }//onCreateView

    /**
     * Toggles the buttons enabled state depending on the input in the edit texts
     */
    private void updateButtonState() {
        boolean enabled = false;
        if(mTermsOfUse.isChecked()) {
            Editable t = mEmailAddressEditText.getText();
            //hier ggf. eine regex für emails (aber nicht die kaputte vom flunx)
            if (null != t) {
                String s = t.toString().trim();
                int pos = s.indexOf('@');
                if (pos > 0 && pos < (s.length() - 1)) {
                    enabled = true;
                }
            }
        }
        // Set the button enabled or disabled
        this.mRegisterButton.setEnabled(enabled);
    }//updateButtonState

    /**
     * Reads the input out of the edit texts and hands them over to the shared preferences where they get stored
     */
    private void storeUserInformationInSharedPreferences() {
        // Get the input out of the edit texts
        String emailAddress = this.mEmailAddressEditText.getText().toString();
        String age = mAgeEditText.getText().toString();
        String job=mJobSelector.getSelectedItem().toString();

        // Store the user information in the shared preferences
        this.mSmartNewsSharedPreferences.saveUserInformation(emailAddress, age, job);
        MyApplication.instance().mEngine.notifyCloudBackup();
        if(DEBUG){Log.i(TAG , "store UI e="+emailAddress+" a="+age+ " isDebug="+mSmartNewsSharedPreferences.isDebugUser()+" uploaded="+mSmartNewsSharedPreferences.isUserInformationUploadedAlready());}
    }//storeUserInformationInSharedPreferences

    /**
     * OnClickListener for view, switches or the id of the given view
     */
    private class ButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.id_button_register:
                    //hide the keyboard
                    InputMethodManager imeManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imeManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    // Store the user information in the shared preferences
                    storeUserInformationInSharedPreferences();
                    showFragment(new AboutFragment(), false);
                    break;
                default:
                    break;
            }
        }//onClick
    }//ButtonOnClickListener

    /**
     * TextWatcher to observe text changes in edit texts which register this text watcher
     */
    private class EditTextTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }//beforeTextChanged

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Toggle the buttons enabled state when text changed
            updateButtonState();
        }//onTextChanged

        @Override
        public void afterTextChanged(Editable s) {
        }//afterTextChanged
    }//EditTextTextWatcher
}//RegistrationFragment