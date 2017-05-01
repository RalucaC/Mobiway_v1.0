package ro.pub.acs.mobiway.gui.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.acra.ACRA;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.general.Constants;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.lib.Authentication;

public class ResetPasswordFragment extends Fragment {

    private static final String TAG = CreateAccountUserFragment.class.getSimpleName();
    private ResetPasswordButtonListener buttonOnClickListener = new ResetPasswordButtonListener();

    EditText emailEditText;
    EditText passEditText;
    EditText confirmPassEditText;
    TextView confirmPasswordErrorTextView;

    private static String emailAddress = Constants.EMPTY_STRING;
    private static String password = Constants.EMPTY_STRING;

    private SharedPreferencesManagement sharedPreferencesManagement;

    private ProgressDialog pDialog;
    private Activity activity;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    public void dismissDialog(){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                pDialog.dismiss();
            }
        });
    }

    private class ResetPasswordButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Authentication authLib = new Authentication();
            if(!authLib.isEmailValid(emailEditText, getActivity())) {
                return;
            }

            if(!authLib.isPasswordValid(passEditText, getActivity())) {
                return;
            }

            if(!authLib.arePasswordsEqual(passEditText, confirmPassEditText)) {

                // show error message
                confirmPasswordErrorTextView.setVisibility(View.VISIBLE);
                dismissDialog();
            } else {

                // set error message invisible
                confirmPasswordErrorTextView.setVisibility(View.INVISIBLE);

                // call reset password API
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("ResetPasswordFragment.onActivityCreated()", "method has been invoked");

        super.onActivityCreated(savedInstanceState);
        Button nextButton = (Button) getActivity().findViewById(R.id.reset_password_next_button);
        nextButton.setOnClickListener(buttonOnClickListener);

        emailEditText = (EditText) getActivity().findViewById(R.id.username_reset_edit_text);
        passEditText = (EditText) getActivity().findViewById(R.id.password_reset_edit_text);
        confirmPassEditText = (EditText) getActivity().findViewById(R.id.retype_password_reset_edit_text);
        confirmPasswordErrorTextView = (TextView) getActivity().findViewById(R.id.password_match_error_text_view);

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(getActivity().getString(R.string.reset_password));
        actionBar.show();

        sharedPreferencesManagement = SharedPreferencesManagement.getInstance(getActivity().getApplicationContext());

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getResources().getString(R.string.loading));
        activity = getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){

        //ACRA log
        ACRA.getErrorReporter().putCustomData("ResetPasswordFragment.onSaveInstanceState()", "method has been invoked");

        Log.e("ResetPasswordFragment", "onSaveInstanceState" + bundle.toString());

        bundle.putString("emailAddress", emailEditText.getText().toString());
        bundle.putString("password", passEditText.getText().toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
    }
}
