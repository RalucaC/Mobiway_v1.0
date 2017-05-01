package ro.pub.acs.mobiway.gui.auth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.acra.ACRA;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;

import ro.pub.acs.mobiway.R;
import ro.pub.acs.mobiway.general.SharedPreferencesManagement;
import ro.pub.acs.mobiway.general.Util;
import ro.pub.acs.mobiway.gui.main.MainActivity;
import ro.pub.acs.mobiway.lib.Authentication;
import ro.pub.acs.mobiway.rest.RestClient;
import ro.pub.acs.mobiway.rest.model.User;

public class LoginFragment extends Fragment {

    // Log tag
    private static final String TAG = LoginFragment.class.getSimpleName();
    private Integer tokenExpiresIn;
    private String accessToken;

    private SharedPreferencesManagement sharedPreferencesManagement;
    private LoginButtonOnClickListener buttonOnClickListener = new LoginButtonOnClickListener();
    EditText emailEditText;
    EditText passEditText;
    TextView authErrorTextView;
    LoginButton loginButton;
    CallbackManager callbackManager;
    private ProgressDialog pDialog;
    private Activity activity;

    private static boolean loggedInWithFacebook = false;

    private class LoginButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            //ACRA log
            ACRA.getErrorReporter().putCustomData("LoginButtonOnClickListener.onClick()", "method has been invoked");
            final Authentication authLib = new Authentication(getActivity());

            switch (v.getId()) {
                case R.id.login_button: {
                    authErrorTextView.setVisibility(View.INVISIBLE);

                    final String emailAddress = emailEditText.getText().toString();
                    final String password = passEditText.getText().toString();

                    if(!authLib.isEmailValid(emailEditText)) {

                        break;
                    }

                    if(!authLib.isPasswordValid(passEditText)) {
                        break;
                    }

                    Thread thread = new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {
                                authLib.showDialog(pDialog);

                                RestClient restClient = new RestClient();
                                User user = new User();
                                user.setUsername(emailAddress);
                                user.setPassword(new String(Hex.encodeHex(DigestUtils.sha(password))));

                                User result = restClient.getApiService().loginUserPass(user);
                                if (result != null){
                                    sharedPreferencesManagement.createLoginSession(result.getId(),
                                            result.getLastname(), result.getUsername(), result.getPassword(),
                                            result.getFirstname(), result.getAuth_token(), result.getAuthExpiresIn());

                                    Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                    authLib.dismissDialog(pDialog);
                                } else {
                                    authErrorTextView.post(new Runnable() {
                                        public void run() {
                                            authErrorTextView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    authLib.dismissDialog(pDialog);

                                    // show reset password button
                                    Button resetButton = (Button) getActivity().findViewById(R.id.reset_password_button);
                                    resetButton.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {

                                //ACRA log
                                ACRA.getErrorReporter().putCustomData("LoginButtonOnClickListener.onClick():error", e.toString());

                                e.printStackTrace();
                                authLib.dismissDialog(pDialog);
                            }
                        }
                    });
                    thread.start();
                }
                break;

                case R.id.create_account_button: {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.container, new CreateAccountUserFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
                break;

                case R.id.reset_password_button: {

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.container, new ResetPasswordFragment());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
                break;
            }
        }
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle state) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("LoginFragment.onCreateView()", "method has been invoked");

        Log.d(TAG, "onCreateView");

        callbackManager = CallbackManager.Factory.create();

        if(loggedInWithFacebook){
            LoginManager.getInstance().logOut();
            sharedPreferencesManagement.logoutUser();

            loggedInWithFacebook = false;
        }

        View view = layoutInflater.inflate(R.layout.fragment_login, container, false);

        loginButton = (LoginButton) view.findViewById(R.id.login_facebook_button);
        loginButton.setReadPermissions(Arrays.asList("user_friends, public_profile, basic_info, email, user_birthday"));

        // If using in a fragment
        loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                long msNow = new Date().getTime();
                long msExpiresIn = loginResult.getAccessToken().getExpires().getTime();
                final Authentication authLib = new Authentication(getActivity());

                accessToken = loginResult.getAccessToken().getToken();
                tokenExpiresIn = (int)((msExpiresIn - msNow) / 1000);
                authLib.showDialog(pDialog);

                final GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    final GraphResponse response) {
                                JSONObject responseObj = response.getJSONObject();

                                if (responseObj != null) {
                                    try {

                                        Log.d(TAG, "FBObjectResponse" + responseObj.toString());

                                        final String email = responseObj.getString("email");
                                        final String firstName = responseObj.getString("first_name");
                                        final String lastName = responseObj.getString("last_name");
                                        final String phoneNumber = Util.getPhoneNumber(getActivity());

                                        Thread thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    RestClient restClient = new RestClient();
                                                    User user = new User();
                                                    user.setUsername(email);
                                                    user.setPhone(phoneNumber);
                                                    user.setFirstname(firstName);
                                                    user.setLastname(lastName);
                                                    user.setFacebook_token(accessToken);
                                                    user.setFacebookExpiresIn(tokenExpiresIn);
                                                    user.setPassword("");

                                                    User result = restClient.getApiService().loginFacebook(user);

                                                    if (result != null) {
                                                        sharedPreferencesManagement.createLoginSession(result.getId(),
                                                                result.getLastname(), result.getUsername(), result.getPassword(),
                                                                result.getFirstname(), result.getAuth_token(), result.getAuthExpiresIn());
                                                        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                                                        startActivity(intent);
                                                        getActivity().finish();
                                                        authLib.dismissDialog(pDialog);
                                                    } else {
                                                        LoginManager.getInstance().logOut();
                                                        authLib.dismissDialog(pDialog);
                                                    }
                                                    Log.e(TAG, "authenticatedUser: " + result);
                                                } catch (Exception e) {

                                                    //ACRA log
                                                    ACRA.getErrorReporter().putCustomData("LoginFragment.onCreateView():error1", e.toString());

                                                    e.printStackTrace();
                                                    LoginManager.getInstance().logOut();
                                                    authLib.dismissDialog(pDialog);
                                                }
                                            }
                                        });
                                        thread.start();
                                    } catch (JSONException e) {

                                        //ACRA log
                                        ACRA.getErrorReporter().putCustomData("LoginFragment.onCreateView():error2", e.toString());

                                        e.printStackTrace();
                                        authLib.dismissDialog(pDialog);
                                    }
                                }

                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email, gender, birthday, first_name, last_name");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("FB CANCEL", "onCancel");
            }

            @Override
            public void onError(FacebookException exception) {

                //ACRA log
                ACRA.getErrorReporter().putCustomData("LoginFragment.onError():FBError", exception.toString());

                Log.d("FB ERROR", exception.toString());
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        //ACRA log
        ACRA.getErrorReporter().putCustomData("LoginFragment.onActivityCreated()", "method has been invoked");

        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        Button loginButton = (Button) getActivity().findViewById(R.id.login_button);
        loginButton.setOnClickListener(buttonOnClickListener);

        Button createAccountButton = (Button) getActivity().findViewById(R.id.create_account_button);
        createAccountButton.setOnClickListener(buttonOnClickListener);

        Button resetPasswordButton = (Button) getActivity().findViewById(R.id.reset_password_button);
        resetPasswordButton.setOnClickListener(buttonOnClickListener);

        authErrorTextView = (TextView) getActivity().findViewById(R.id.auth_error_text_view);
        emailEditText = (EditText) getActivity().findViewById(R.id.username_edit_text);
        passEditText = (EditText) getActivity().findViewById(R.id.password_edit_text);

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.hide();

        sharedPreferencesManagement = SharedPreferencesManagement.getInstance(getActivity().getApplicationContext());

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getResources().getString(R.string.loading));
        activity = getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}