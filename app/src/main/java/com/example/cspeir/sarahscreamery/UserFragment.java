package com.example.cspeir.sarahscreamery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cspeir on 11/11/2017.
 */

public class UserFragment extends Fragment {
    TextView firstNameText;
    private final String TAG =MainActivity.class.getSimpleName();
    TextView lastNameText;
    TextView birthdayText;
    TextView emailText;
    EditText lastNameEdit;
    EditText firstNameEdit;
    TextView updateText;
    User mUser;
    String username;
    Button updateButton;

    public UserFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle bundle) {
        super.onCreateView(inflater, v, bundle);
        final View rootView = inflater.inflate(R.layout.fragment_user_info, v, false);
        final BackendlessUser bUser = Backendless.UserService.CurrentUser();
        Intent intent = getActivity().getIntent();
        username = intent.getStringExtra("name");
        final CognitoUser user = AppHelper.getPool().getUser(username);
        GetDetailsHandler detailsHandler = new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {

                // Store details in the AppHandler
                AppHelper.setUserDetails(cognitoUserDetails);
                mUser= new User();
                String fullName;
                fullName = cognitoUserDetails.getAttributes().getAttributes().get("name");
                SimpleDateFormat bdayFormat = new SimpleDateFormat("MM/dd/yyyy");

                mUser.setFirstName(fullName.substring(0,fullName.indexOf(" ")));
                mUser.setLastName(fullName.substring(fullName.indexOf(" "), fullName.length()));
                try {
                    mUser.setBirthday(bdayFormat.parse(cognitoUserDetails.getAttributes().getAttributes().get("birthdate")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mUser.setEmail(username);
                String birthday = mUser.getBirthday().toString();
                String birthdayFormat;
                birthdayFormat = birthday.substring(4, 10)+ birthday.substring(23, birthday.length());
                firstNameText = (TextView) rootView.findViewById(R.id.first_name_user);
                lastNameText = (TextView) rootView.findViewById(R.id.last_name_user);
                birthdayText = (TextView) rootView.findViewById(R.id.birthday_user);
                emailText = (TextView) rootView.findViewById(R.id.email_text);
                firstNameText.setText(getString(R.string.first_name) + ": " + mUser.getFirstName());
                lastNameText.setText(getString(R.string.last_name) + ": " + mUser.getLastName());
                birthdayText.setText(getString(R.string.birthday) + ": " + birthdayFormat);
                emailText.setText(getString(R.string.email)+ ": " + mUser.getEmail());

            }

            @Override
            public void onFailure(Exception exception) {

                Toast.makeText(getContext(), "Could not fetch user details! "+exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        user.getDetailsInBackground(detailsHandler);
        return rootView;

    }

}