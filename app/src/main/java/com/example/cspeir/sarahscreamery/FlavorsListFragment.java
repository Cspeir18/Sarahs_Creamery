package com.example.cspeir.sarahscreamery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by cspeir on 11/11/2017.
 */

public class FlavorsListFragment extends ListFragment {
    private static final String TAG = "FlavorListFragment";
    private AWSCredentials credentials;
    private String username;
    private String profile;
    private CognitoUser user;
    private AWSCredentialsProvider mCredentialsProvider;
    private ArrayList<Flavors> mFlavors;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
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


        //get the value of mPublicView from the intent. By default, it will be set to false (the second parameter in the call below)


        //Create the list of trips
        mFlavors = new ArrayList<Flavors>();


        //Create the Adapter that will control the ListView for the fragment
        //The adapter is responsible for feeding the data to the list view
        FlavorsListFragment.FlavorAdapter adapter = new FlavorsListFragment.FlavorAdapter(mFlavors);
        setListAdapter(adapter);
        refreshFlavorList();


    }
    public FlavorsListFragment(){

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup v, final Bundle bundle) {
        super.onCreateView(inflater, v, bundle);
        View rootView = inflater.inflate(R.layout.fragment_flavors_list, v, false);


        //register the context menu
        ListView listView = (ListView)rootView.findViewById(android.R.id.list);
        registerForContextMenu(listView);

        Intent intent = getActivity().getIntent();
        username=intent.getStringExtra("name");
        user = AppHelper.getPool().getUser(username);
        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.floating_action_button);
        GetDetailsHandler detailsHandler = new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {

                // Store details in the AppHandler
                AppHelper.setUserDetails(cognitoUserDetails);
                profile = cognitoUserDetails.getAttributes().getAttributes().get("profile");

                if (profile.contains("false")){
                    fab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception exception) {

                Toast.makeText(getContext(), "Could not fetch user details! "+exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        user.getDetailsInBackground(detailsHandler);

        registerForContextMenu(listView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View mView = getLayoutInflater(bundle).inflate(R.layout.new_flavor, null);
                final EditText flavorEt = (EditText) mView.findViewById(R.id.new_flavor_et);
                Button flavorButton = (Button) mView.findViewById(R.id.new_flavor_btn);
                builder.setView(mView);
                final AlertDialog dialog = builder.create();
                dialog.show();

                flavorButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!flavorEt.getText().toString().isEmpty()){
                            Thread thread =new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(mCredentialsProvider);
                                    DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                                    Flavors flavor = new Flavors();
                                    flavor.setName(flavorEt.getText().toString().trim());
                                    mapper.save(flavor);
                                }
                            });
                            thread.start();
                            dialog.dismiss();

                        }
                        else{
                            Toast.makeText(getContext(), "Please enter a name for the flavor", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        return rootView;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (profile.contains("true")) {
            getActivity().getMenuInflater().inflate(R.menu.menu_flavor_list_item_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        FlavorsListFragment.FlavorAdapter adapter = (FlavorsListFragment.FlavorAdapter)getListAdapter();
        Flavors flavors = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.menu_item_delete_trip:
                //delete the trip from the baas
                deleteFlavor(flavors);
                return true;
        }
        return super.onContextItemSelected(item);
    }
    private void deleteFlavor(Flavors flavors) {
            final Flavors deleteFlavor = flavors;
            Thread deleteTread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(mCredentialsProvider);
                        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                        mapper.delete(deleteFlavor);
                        refreshFlavorList();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });
            deleteTread.start();

    }
    private void refreshFlavorList() {
        Thread refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(mCredentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                Toast.makeText(getContext(), "refreshed",Toast.LENGTH_SHORT ).show();
                PaginatedList<Flavors> result = mapper.scan(Flavors.class, scanExpression);
                mFlavors.clear();
                for (int i=0;i<result.size();i++){

                    mFlavors.add(result.get(i));
                }

                ((FlavorAdapter)getListAdapter()).notifyDataSetChanged();

            }
        });
        refreshThread.run();


        // todo: Activity 3.1.4

    }
    private class FlavorAdapter extends ArrayAdapter<Flavors> {

        public FlavorAdapter(ArrayList<Flavors> Flavors) {
            super(getActivity(), 0, Flavors);
        }
        @Override
        public int getCount(){
            return mFlavors.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView==null){
                convertView= getActivity().getLayoutInflater().inflate(R.layout.fragment_flavor_list_item,  null);
            }


                Flavors flavor = getItem(position);
                TextView nameTextView = (TextView) convertView.findViewById(R.id.flavor_list_item_textName);
                nameTextView.setText(flavor.getName());

            return convertView;

        }
    }
}
