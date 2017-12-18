package com.example.cspeir.sarahscreamery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;

import static android.app.Activity.RESULT_OK;

/**
 * Created by cspeir on 11/16/2017.
 */

public class HomeFragment extends Fragment {
    TextView helloText;
    private final String TAG = "HomeFragment";
    private String username;
    private ProgressDialog waitDialog;
    private CognitoUser user;
    private CognitoUserSession session;

    private CognitoUserDetails details;
    private AlertDialog userDialog;
    private AWSCredentials credentials;
    private AWSCredentialsProvider mCredentialsProvider;
    public HomeFragment(){

    }
    public View onCreateView(LayoutInflater inflater , ViewGroup v, Bundle bundle) {
        super.onCreateView(inflater, v, bundle);
        View rootView = inflater.inflate(R.layout.fragment_home, v, false);
        credentials = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return getResources().getString(R.string.access_key_ID);
            }

            @Override
            public String getAWSSecretKey() {
                return getResources().getString(R.string.secret_access_key);
            }
        };
        mCredentialsProvider = new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return credentials;
            }

            @Override
            public void refresh() {

            }
        };
        ClientConfiguration clientConfiguration = new ClientConfiguration();




        Intent intent = getActivity().getIntent();
        username=intent.getStringExtra("name");
        user = AppHelper.getPool().getUser(username);
        helloText= (TextView) rootView.findViewById(R.id.hello);
        GetDetailsHandler detailsHandler = new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {

                // Store details in the AppHandler
                AppHelper.setUserDetails(cognitoUserDetails);
                helloText.setText("Hello "+AppHelper.getUserDetails().getAttributes().getAttributes().get("name")+"!");
                // Trusted devices?

                handleTrustedDevice();
            }

            @Override
            public void onFailure(Exception exception) {

                Toast.makeText(getContext(), "Could not fetch user details! "+exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        user.getDetailsInBackground(detailsHandler);

        return rootView;


    }

    private void handleTrustedDevice() {
        CognitoDevice newDevice = AppHelper.getNewDevice();
        if (newDevice != null) {
            AppHelper.newDevice(null);
            trustedDeviceDialog(newDevice);
        }
    }
    private void trustedDeviceDialog(final CognitoDevice newDevice) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Remember this device?");
        //final EditText input = new EditText(UserActivity.this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        //input.setLayoutParams(lp);
        //input.requestFocus();
        //builder.setView(input);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //String newValue = input.getText().toString();
                    showWaitDialog("Remembering this device...");
                    updateDeviceStatus(newDevice);
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void updateDeviceStatus(CognitoDevice device) {
        device.rememberThisDeviceInBackground(trustedDeviceHandler);
    }
    GenericHandler trustedDeviceHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            // Close wait dialog
            closeWaitDialog();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Failed to update device status", AppHelper.formatException(exception), true);
        }
    };
    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(getContext());
        waitDialog.setTitle(message);
        waitDialog.show();
    }
    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }
    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();

                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG," -- Dialog dismiss failed");
                    if(exit) {

                    }
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

}
