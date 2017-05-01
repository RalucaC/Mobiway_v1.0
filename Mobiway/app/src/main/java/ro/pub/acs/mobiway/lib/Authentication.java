package ro.pub.acs.mobiway.lib;


import android.app.Activity;
import android.widget.EditText;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.general.Constants;
import ro.pub.acs.mobiway.general.Util;

public class Authentication {

    public Authentication(){
        // constructor
    }

    public boolean isEmailValid(EditText emailEditText, Activity activity){

        final String emailAddress = emailEditText.getText().toString();

        if (emailAddress.equals(Constants.EMPTY_STRING)) {
            emailEditText.setHintTextColor(activity.getResources().getColor(R.color.custom_hint_text_color));
            emailEditText.setHint(activity.getResources().getString(R.string.emailAddressRequired));
            return false;
        } else {
            if(!Util.emailValidator(emailAddress)){
                emailEditText.setText(Constants.EMPTY_STRING);
                emailEditText.setHintTextColor(activity.getResources().getColor(R.color.custom_hint_text_color));
                emailEditText.setHint(activity.getResources().getString(R.string.emailAddressNotValid));
                return false;
            }
        }

        return true;
    }

    public boolean isPasswordValid(EditText passwordEditText, Activity activity) {
        String password = passwordEditText.getText().toString();

        if (password.equals(Constants.EMPTY_STRING)) {
            passwordEditText.setHintTextColor(activity.getResources().getColor(R.color.custom_hint_text_color));
            passwordEditText.setHint(activity.getResources().getString(R.string.passwordRequired));
            return false;
        }

        return true;
    }

    public boolean arePasswordsEqual(EditText password, EditText confirmPassword) {

        if(!password.getText().toString().equals(confirmPassword.getText().toString())) {
            return false;
        }

        return true;
    }
}
