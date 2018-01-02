package com.example.cspeir.sarahscreamery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChooseMfaContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Regions;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by cspeir on 11/9/2017.
 */

public class LoginActivity extends AppCompatActivity {
    private final String TAG =LoginActivity.class.getSimpleName();
    TextView birthday_text, forgot_password_text;
    EditText input_email;
    EditText input_password;
    TextView link_signup, verify_account_text;
    AppCompatButton login_button;
    private String username;
    EditText input_first_name;
    EditText input_last_name;
    private String password;
    DatePicker input_date;
    AppCompatDialog progressDialog;
    private String userPasswd;
    private AlertDialog userDialog;
    private String usernameInput;
    private AnimationDrawable animationDrawable;
    CognitoUser cognitoUser;
    private ForgotPasswordContinuation forgotPasswordContinuation;


    private ImageView mProgressBar;
    public static final String EMAIL_PREF = "EMAIL_PREF";

        @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

            AppHelper.init(getApplicationContext());
            verify_account_text = (TextView) findViewById(R.id.verify_account_text);
            forgot_password_text = (TextView) findViewById(R.id.forgot_password_text);
            birthday_text = (TextView)findViewById(R.id.birthday_text);
            input_date = (DatePicker)findViewById(R.id.birthday);
            input_email=(EditText)findViewById(R.id.input_email);
            input_first_name = (EditText)findViewById(R.id.input_first_name);
            input_last_name = (EditText)findViewById(R.id.input_last_name);
            input_password = (EditText)findViewById(R.id.input_password);
            link_signup = (TextView)findViewById(R.id.link_signup);
            mProgressBar = (ImageView) findViewById(R.id.iv);
            input_email.setHint("Email");
            input_password.setHint("Password");
            verify_account_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmUser();
                }
            });
        forgot_password_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotpasswordUser();
            }
        });
        login_button = (AppCompatButton)findViewById(R.id.btn_login);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (login_button.getText()==getString(R.string.login)){
                    //login();
                    username =input_email.getText().toString().trim();
                    password = input_password.getText().toString().trim();

                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    View mView = getLayoutInflater().inflate(R.layout.animation_loading, null);
                    builder.setView(mView);
                    ImageView animation = (ImageView) mView.findViewById(R.id.animation);
                    final AnimationDrawable animationDrawable1 = (AnimationDrawable)animation.getBackground();
                    animationDrawable1.start();
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getWindow().setLayout(390, 390);

                    AuthenticationHandler authenticationHandler = new AuthenticationHandler() { // login handler


                        @Override
                        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                            Log.d(TAG, " -- Auth Success");
                            AppHelper.setCurrSession(userSession);
                            AppHelper.newDevice(newDevice);
                            dialog.dismiss();
                            launchUser();
                        }

                        @Override
                        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                            // The API needs user sign-in credentials to continue
                            AuthenticationDetails authenticationDetails = new AuthenticationDetails(input_email.getText().toString().trim(), input_password.getText().toString().trim(),null);

                            // Pass the user sign-in credentials to the continuation
                            authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                            // Allow the sign-in to continue
                            authenticationContinuation.continueTask(); // handles the authentication with a email code
                        }

                        @Override
                        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
                            // Multi-factor authentication is required; get the verification code from user

                        }

                        @Override
                        public void authenticationChallenge(ChallengeContinuation continuation) {

                        }

                        @Override
                        public void onFailure(Exception exception) {
                            dialog.dismiss();
                            Toast.makeText(getBaseContext(), "Sign in failed "+ exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    };

                    CognitoUser user =AppHelper.getPool().getUser();
                    user.getSessionInBackground(authenticationHandler);
                }
                else{
                    final Date date1=  new Date(input_date.getYear()-1900, input_date.getMonth(), input_date.getDayOfMonth());

                    if (!input_email.getText().toString().trim().isEmpty()&&!input_password.getText().toString().trim().isEmpty()&&!input_first_name.getText().toString().trim().isEmpty()&&!input_last_name.getText().toString().trim().isEmpty()){
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        View mView = getLayoutInflater().inflate(R.layout.animation_loading, null);
                        builder.setView(mView);
                        ImageView animation = (ImageView) mView.findViewById(R.id.animation);
                        final AnimationDrawable animationDrawable1 = (AnimationDrawable)animation.getBackground();
                        animationDrawable1.start();
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.getWindow().setLayout(390, 390);
                        usernameInput = input_email.getText().toString().trim();
                        String userpasswordInput = input_password.getText().toString().trim();
                        userPasswd = userpasswordInput;
                        String firstName = input_first_name.getText().toString().trim();
                        String lastName = input_last_name.getText().toString().trim();
                        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
                        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                        userAttributes.addAttribute("email", usernameInput);
                        userAttributes.addAttribute("name", firstName+" "+lastName);
                        userAttributes.addAttribute("birthdate",format.format(date1));
                        userAttributes.addAttribute("profile", "false"); // adds attributes to the new user for registration
                        userAttributes.addAttribute("custom:rewardsUsed", " ");
                        userAttributes.addAttribute("custom:birthdayYearUsed", " ");


                        SignUpHandler signUpHandler = new SignUpHandler() {
                            @Override
                            public void onSuccess(CognitoUser user, boolean signUpConfirmationState,
                                                  CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                                // Check signUpConfirmationState to see if the user is already confirmed
                                dialog.dismiss();
                                Boolean regState = signUpConfirmationState;
                                if (signUpConfirmationState) {
                                    // User is already confirmed
                                    Toast.makeText(getBaseContext(), "Sign up successful! "+usernameInput+" has been Confirmed", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    // User is not confirmed
                                    confirmSignUp(cognitoUserCodeDeliveryDetails);
                                }
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                dialog.dismiss();
                                showDialogMessage("Sign up failed",AppHelper.formatException(exception),false);
                            }
                        };
                        AppHelper.getPool().signUpInBackground(usernameInput, userpasswordInput, userAttributes, null, signUpHandler);
                    }


                }
            }
        });
        link_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (link_signup.getText()!="Already have an account? Sign In") {
                    login_button.setText(R.string.sign_up);
                    input_first_name.setEnabled(true);
                    input_last_name.setEnabled(true);
                    input_date.setEnabled(true);
                    input_first_name.setVisibility(View.VISIBLE);
                    input_last_name.setVisibility(View.VISIBLE);
                    input_date.setVisibility(View.VISIBLE);
                    birthday_text.setVisibility(View.VISIBLE);
                    input_first_name.setHint("First Name");
                    input_last_name.setHint("Last Name");
                    input_email.setHint("Email");
                    input_password.setHint("Password");
                    link_signup.setText("Already have an account? Sign In");
                }
                else{
                    login_button.setText(R.string.login);
                    input_first_name.setEnabled(false);
                    input_last_name.setEnabled(false);
                    input_date.setEnabled(false);
                    input_first_name.setVisibility(View.GONE);
                    input_last_name.setVisibility(View.GONE);
                    input_date.setVisibility(View.GONE);
                    birthday_text.setVisibility(View.GONE);
                    input_email.setHint("Email");
                    input_password.setHint("Password");
                    input_first_name.setHint("");
                    input_last_name.setHint("");
                    link_signup.setText("No account yet? Create one");
                }
            }
        });
    }


    public void warnUser(String error, int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(error);
        if(id==2){
            builder.setTitle(R.string.authentication_error_title);
        }
        else if (id == 3){
            builder.setTitle("Invalid Sign Up Credentials");
        }
        else if(id ==1){
            builder.setTitle("Invalid Login Credentials");
        }

        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if(exit) {
                        exit(usernameInput);
                    }
                } catch (Exception e) {
                    if(exit) {
                        exit(usernameInput);
                    }
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }
    private void exit(String uname) {
        exit(uname, null);
    }
    private void exit(String uname, String password) {
        Intent intent = new Intent();
        if (uname == null) {
            uname = "";
        }
        if (password == null) {
            password = "";
        }
        intent.putExtra("name", uname);
        intent.putExtra("password", password);
        setResult(RESULT_OK, intent);
        finish();
    }
    private void confirmSignUp(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
        Intent intent = new Intent(this, SignUpConfirm.class);
        intent.putExtra("source","signup");
        intent.putExtra("name", usernameInput);
        intent.putExtra("destination", cognitoUserCodeDeliveryDetails.getDestination());
        intent.putExtra("deliveryMed", cognitoUserCodeDeliveryDetails.getDeliveryMedium());
        intent.putExtra("attribute", cognitoUserCodeDeliveryDetails.getAttributeName());
        startActivityForResult(intent, 10);
    }
    private void confirmUser() {
        Intent confirmActivity = new Intent(this, SignUpConfirm.class);
        confirmActivity.putExtra("source","main");
        startActivityForResult(confirmActivity, 2); //starts the activity that confirms the user

    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

                if (resultCode == RESULT_OK) {
                    String newPass = data.getStringExtra("newPass");
                    String code = data.getStringExtra("code");
                    if (newPass != null && code != null) {
                        if (!newPass.isEmpty() && !code.isEmpty()) {
                            forgotPasswordContinuation.setPassword(newPass);
                            forgotPasswordContinuation.setVerificationCode(code);
                            forgotPasswordContinuation.continueTask();
                        }
                    }
                }



    }
    private void launchUser() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name", input_email.getText().toString().trim());
        startActivity(intent); // launches the activity where the user can navigate and view things
    }
    private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
        if(username != null) {
            this.username = input_email.getText().toString().trim();
            AppHelper.setUser(username);
        }
        if(this.password == null) {
            input_email.setText(username);
            password = input_password.getText().toString().trim();
            if(password == null) {

                input_password.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }

            if(password.length() < 1) {
                input_password.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }
        }

        AuthenticationDetails authenticationDetails = new AuthenticationDetails(this.username, password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }
    private void forgotpasswordUser() {
        username = input_email.getText().toString();
        if(username == null) {
            Toast.makeText(getBaseContext(), "username cannor be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(username.length() < 1) {
            Toast.makeText(getBaseContext(), "username cannor be empty", Toast.LENGTH_SHORT).show();
            return;
        }


        AppHelper.getPool().getUser(username).forgotPasswordInBackground(forgotPasswordHandler);
    }
    private void getForgotPasswordCode(ForgotPasswordContinuation forgotPasswordContinuation) {
        this.forgotPasswordContinuation = forgotPasswordContinuation;
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        intent.putExtra("destination",forgotPasswordContinuation.getParameters().getDestination());
        intent.putExtra("deliveryMed", forgotPasswordContinuation.getParameters().getDeliveryMedium());
        startActivityForResult(intent, 3);
    }
    ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() {
        @Override
        public void onSuccess() {

            Toast.makeText(getBaseContext(), "Password successfully changed", Toast.LENGTH_SHORT).show();
            input_password.setText("");
            input_password.requestFocus();
        }

        @Override
        public void getResetCode(ForgotPasswordContinuation forgotPasswordContinuation) {

            getForgotPasswordCode(forgotPasswordContinuation);
        }

        @Override
        public void onFailure(Exception e) {

            Toast.makeText(getBaseContext(), "failed to reset password", Toast.LENGTH_SHORT).show();
        }
    };


}


