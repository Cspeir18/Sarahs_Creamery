package com.example.cspeir.sarahscreamery;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.Date;

/**
 * Created by cspeir on 11/9/2017.
 */

public class LoginActivity extends AppCompatActivity {
    private final String TAG =LoginActivity.class.getSimpleName();
    TextView birthday_text;
    EditText input_email;
    EditText input_password;
    TextView link_signup;
    AppCompatButton login_button;
    EditText input_first_name;
    EditText input_last_name;
    DatePicker input_date;
    AppCompatDialog progressDialog;
    private AnimationDrawable animationDrawable;


    private ImageView mProgressBar;
    public static final String EMAIL_PREF = "EMAIL_PREF";

        @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
        Backendless.initApp(this, getString(R.string.app_ID), getString(R.string.app_key));
            String currentUserObjectId = Backendless.UserService.loggedInUser();
            Backendless.UserService.findById(currentUserObjectId, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser response) {
                    Backendless.UserService.setCurrentUser( response );
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }

                @Override
                public void handleFault(BackendlessFault fault) {

                }
            });
        login_button = (AppCompatButton)findViewById(R.id.btn_login);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (login_button.getText()==getString(R.string.login)){
                    login();
                }
                else{
                    register();
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
    public void login(){
        String userEmail = input_email.getText().toString();
        String password = input_password.getText().toString();


        userEmail = userEmail.trim();
        password = password.trim();

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.animation_loading, null);
        builder.setView(mView);
        ImageView animation = (ImageView) mView.findViewById(R.id.animation);
        final AnimationDrawable animationDrawable1 = (AnimationDrawable)animation.getBackground();
        animationDrawable1.start();
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(390, 390);

        Backendless.UserService.login(userEmail, password, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                Log.i(TAG, "Login successful for "+ response.getEmail());

                dialog.dismiss();
                animationDrawable1.stop();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.i(TAG, "login: " + fault.getMessage());
                warnUser(fault.getMessage(), 1);
                dialog.dismiss();
                animationDrawable1.stop();


            }
        });
    }
    public void register(){
        String email = input_email.getText().toString().trim();
        String password = input_password.getText().toString().trim();
        String firstName = input_first_name.getText().toString().trim();
        String lastName = input_last_name.getText().toString().trim();
        final Date date1=  new Date(input_date.getYear()-1900, input_date.getMonth(), input_date.getDayOfMonth());
        if (!email.isEmpty() &&!password.isEmpty() && !firstName.isEmpty()&&password.length()>6&&!lastName.isEmpty()&&date1!=null) {
            BackendlessUser user = new BackendlessUser();
            user.setEmail(email);
            user.setPassword(password);
            user.setProperty("firstName", firstName);
            user.setProperty("lastName", lastName);
            user.setProperty("birthday", date1);
            user.setProperty("admin", false);
            user.setProperty("rewardsUsed", " ");
            user.setProperty("birhtdayYear", " ");

            Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser response) {
                    Log.i("register", response.toString());
                    login();



                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    Log.i("error", fault.toString());
                    warnUser(fault.getMessage(), 2);
                    ;
                }
            });
        }
        else{
            warnUser(getString(R.string.empty_field_signup_error), 3);
        }
    }
    public void warnUser(String error, int id){
        String title;
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
    }


