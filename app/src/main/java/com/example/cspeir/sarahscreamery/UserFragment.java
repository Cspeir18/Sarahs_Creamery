package com.example.cspeir.sarahscreamery;

import android.app.ProgressDialog;
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

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

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
    Button updateButton;

    public UserFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup v, Bundle bundle) {
        super.onCreateView(inflater, v, bundle);
        View rootView = inflater.inflate(R.layout.fragment_user_info, v, false);
        final BackendlessUser bUser = Backendless.UserService.CurrentUser();
        mUser = new User();
        mUser.setFirstName(bUser.getProperty("firstName").toString());
        mUser.setLastName(bUser.getProperty("lastName").toString());
        mUser.setBirthday((Date) bUser.getProperty("birthday"));
        mUser.setEmail(bUser.getEmail());
        mUser.setAdmin((Boolean) bUser.getProperty("admin"));
        String birthday = mUser.getBirthday().toString();
        String birthdayFormat;
        birthdayFormat = birthday.substring(4, 10)+ birthday.substring(23, birthday.length());
        firstNameText = (TextView) rootView.findViewById(R.id.first_name_user);
        lastNameText = (TextView) rootView.findViewById(R.id.last_name_user);
        birthdayText = (TextView) rootView.findViewById(R.id.birthday_user);
        emailText = (TextView) rootView.findViewById(R.id.email_text);
        firstNameEdit = (EditText) rootView.findViewById(R.id.update_first_name);
        lastNameEdit = (EditText) rootView.findViewById(R.id.update_last_name);
        updateText = (TextView) rootView.findViewById(R.id.link_edit);
        updateButton = (Button) rootView.findViewById(R.id.btn_update);
        firstNameText.setText(getString(R.string.first_name) + ": " + mUser.getFirstName());
        lastNameText.setText(getString(R.string.last_name) + ": " + mUser.getLastName());
        birthdayText.setText(getString(R.string.birthday) + ": " + birthdayFormat);
        emailText.setText(getString(R.string.email)+ ": " + mUser.getEmail());
        updateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateText.getText() == getString(R.string.update)) {
                    updateButton.setVisibility(View.VISIBLE);
                    firstNameEdit.setVisibility(View.VISIBLE);
                    lastNameEdit.setVisibility(View.VISIBLE);
                    updateText.setText(getString(R.string._cancel_update));
                    firstNameEdit.setEnabled(true);
                    lastNameEdit.setEnabled(true);
                    firstNameEdit.setHint("First Name");
                    lastNameEdit.setHint("Last Name");
                }
                else{
                    firstNameEdit.setVisibility(View.GONE);
                    lastNameEdit.setVisibility(View.GONE);
                    updateButton.setVisibility(View.GONE);
                    updateText.setText(getString(R.string.update));

                }
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstName = firstNameEdit.getText().toString().trim();
                final String lastName = lastNameEdit.getText().toString().trim();
                if (!firstName.isEmpty() &&!lastName.isEmpty()) {
                    bUser.setProperty("firstName", firstName);
                    bUser.setProperty("lastName", lastName);
                    final ProgressDialog pDialog = ProgressDialog.show(v.getContext(),
                            "Please Wait!",
                            "Updating Profile",
                            true);
                    Backendless.UserService.update(bUser, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            Log.i(TAG, "User update succesful");
                            pDialog.dismiss();
                            firstNameEdit.setVisibility(View.GONE);
                            lastNameEdit.setVisibility(View.GONE);
                            updateButton.setVisibility(View.GONE);
                            updateText.setText(getString(R.string.update));
                            firstNameText.setText(getString(R.string.first_name)+": "+ firstName);
                            lastNameText.setText(getString(R.string.last_name)+": "+ lastName);
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.i(TAG, "User update failed: "+ fault.getMessage());
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Error Updating User Profile");
                            builder.setMessage(fault.getMessage());
                            builder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Error Updating User Profile");
                    builder.setMessage("Make sure you have entered a valid first and last name.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });




            return rootView;

    }

}