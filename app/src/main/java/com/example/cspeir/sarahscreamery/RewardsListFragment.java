package com.example.cspeir.sarahscreamery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by cspeir on 11/24/2017.
 */

public class RewardsListFragment extends ListFragment {
    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_START_DATE = 1;
    private static final int REQUEST_END_DATE = 2;
    public static final String DATE_FORMAT = "E MM-dd-yyyy";
    Button newReward, startDate, endDate;
    TextView title;
    private AWSCredentials credentials;
    private String username;
    private String birthday;
    private String profile;
    private CognitoUser user;
    private AWSCredentialsProvider mCredentialsProvider;
    EditText newName, newdescription, newDirections;
    private Rewards mReward;
    private static final String TAG = "RewardsListFragment";
    private ArrayList<Rewards> mRewards;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        //get the value of mPublicView from the intent. By default, it will be set to false (the second parameter in the call below)

        mReward = new Rewards();
        //Create the list of trips
        mRewards = new ArrayList<Rewards>();

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

        //Create the Adapter that will control the ListView for the fragment
        //The adapter is responsible for feeding the data to the list view
        RewardsListFragment.RewardAdapter adapter = new RewardsListFragment.RewardAdapter(mRewards);
        setListAdapter(adapter);
        refreshRewardList();
    }
    public RewardsListFragment(){

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup v, final Bundle bundle) {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        super.onCreateView(inflater, v, bundle);
        View rootView = inflater.inflate(R.layout.fragment_rewards_list, v, false);


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

                if (profile.contains("true")){
                    fab.setVisibility(View.VISIBLE);
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
                View mView = getLayoutInflater(bundle).inflate(R.layout.new_reward, null);
                newReward = (Button) mView.findViewById(R.id.new_reward_btn);
                newdescription = (EditText) mView.findViewById(R.id.new_reward_desccription_et);
                newName = (EditText) mView.findViewById(R.id.new_reward_et);
                newDirections = (EditText) mView.findViewById(R.id.new_reward_directions_et);
                startDate = (Button) mView.findViewById(R.id.start_date);
                endDate = (Button) mView.findViewById(R.id.end_date);
                title = (TextView) mView.findViewById(R.id.rewards_title);
                startDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        DatePickerFragment dialog;
                        dialog = DatePickerFragment.newInstance(getDateFromView(startDate), R.string.start_date_hint);
                        dialog.setTargetFragment(RewardsListFragment.this, REQUEST_START_DATE);
                        dialog.show(fm, DIALOG_DATE);
                    }
                });
                endDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        DatePickerFragment dialog;
                        dialog = DatePickerFragment.newInstance(getDateFromView(endDate), R.string.end_date_hint);
                        dialog.setTargetFragment(RewardsListFragment.this, REQUEST_END_DATE);
                        dialog.show(fm, DIALOG_DATE);
                    }
                });
                builder.setView(mView);
                final AlertDialog dialog = builder.create();
                dialog.show();
                newReward.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mReward.getFormattedEndDate()!=null&&mReward.getFormattedStartDate()!=null) {


                            if (mReward.getFormattedStartDate().before(mReward.getFormattedEndDate()) && !newdescription.getText().toString().trim().isEmpty() && !endDate.getText().equals(getText(R.string.end_date)) && !startDate.getText().equals(getString(R.string.start_date)) && !newName.getText().toString().trim().isEmpty() && !newDirections.getText().toString().trim().isEmpty()) {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(mCredentialsProvider);
                                        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                                        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                                        Rewards reward = new Rewards();
                                        reward.setDescription(newdescription.getText().toString().trim());
                                        reward.setDirection(newDirections.getText().toString().trim());
                                        reward.setStartDate(format.format(mReward.getFormattedStartDate()));
                                        reward.setEndDate(format.format(mReward.getFormattedEndDate()));
                                        reward.setRewardName(newName.getText().toString().trim());
                                        reward.setUsedBy(" ");
                                        mapper.save(reward);
                                    }
                                });
                                thread.start();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getContext(), "Please make sure all fields are filled and the start date is before the end date", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(getContext(), "Please enter a start and end date", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        return rootView;
    }
    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        Intent intent = getActivity().getIntent();
        username=intent.getStringExtra("name");
        user = AppHelper.getPool().getUser(username);
        GetDetailsHandler detailsHandler = new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                // Store details in the AppHandler
                AppHelper.setUserDetails(cognitoUserDetails);
                profile = cognitoUserDetails.getAttributes().getAttributes().get("profile");
                birthday = cognitoUserDetails.getAttributes().getAttributes().get("birthdate");
                Rewards r= (Rewards) ((RewardAdapter)getListAdapter()).getItem(position);
                Log.d(TAG, r.toString() + " was clicked." + RewardsListFragment.class);
                Intent i = new Intent(getActivity(), RewardActivity.class);
                i.putExtra("rewardName", r.getRewardName());
                i.putExtra("endDate", r.getFormattedEndDate());
                i.putExtra("startDate", r.getFormattedStartDate());
                i.putExtra("directions", r.getDirection());
                i.putExtra("description", r.getDescription());
                i.putExtra("name", username);
                if (birthday.length()>2){
                    i.putExtra("birthday", birthday);
                    startActivity(i);
                }

            }

            @Override
            public void onFailure(Exception exception) {

                Toast.makeText(getContext(), "Could not fetch user details! "+exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        user.getDetailsInBackground(detailsHandler);

    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (profile.contains("true")) {
            getActivity().getMenuInflater().inflate(R.menu.menu_reward_list_item_context, menu);
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        RewardsListFragment.RewardAdapter adapter = (RewardsListFragment.RewardAdapter)getListAdapter();
        Rewards reward = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.menu_item_delete_trip:
                //delete the trip from the baas
                deleteReward(reward);
                return true;
        }
        return super.onContextItemSelected(item);
    }
    private void deleteReward(Rewards reward) {
        final Rewards deleteReward = reward;


        Thread deleteTread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(mCredentialsProvider);
                    DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                    mapper.delete(deleteReward);
                    refreshRewardList();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        deleteTread.start();


    }
    private void refreshRewardList() {
        final User mUser = new User();
        GetDetailsHandler detailsHandler = new GetDetailsHandler() {
            @Override
            public void onSuccess(final CognitoUserDetails cognitoUserDetails) {
                final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                // Store details in the AppHandler

                AppHelper.setUserDetails(cognitoUserDetails);
                String birthday = cognitoUserDetails.getAttributes().getAttributes().get("birthdate");
                String rewardsUsed = cognitoUserDetails.getAttributes().getAttributes().get("custom:rewardsUsed");
                String birthdayYear = cognitoUserDetails.getAttributes().getAttributes().get("custom:birthdayYearUsed");
                mUser.setRewardsUsed(rewardsUsed);
                try {
                    mUser.setBirthday(format.parse(birthday));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mUser.setBirthdayYear(birthdayYear);
                final Thread refreshThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(mCredentialsProvider);
                        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                        PaginatedList<Rewards> result = mapper.scan(Rewards.class, scanExpression);
                        mRewards.clear();
                        for (int i = 0; i < result.size(); i++) {
                            Rewards newReward = new Rewards();
                            newReward.setUsedBy(result.get(i).getUsedBy());
                            newReward.setRewardName(result.get(i).getRewardName());
                            newReward.setDescription(result.get(i).getDescription());
                            newReward.setDirection(result.get(i).getDirection());
                            try {
                                newReward.setFormattedEndDate(format.parse(result.get(i).getEndDate()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            try {
                                newReward.setFormattedStartDate(format.parse(result.get(i).getStartDate()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
                            final SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
                            final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                            final Date date = new Date();
                            final Date yearDate = new Date();
                            GregorianCalendar cal = new GregorianCalendar();
                            GregorianCalendar calendar = new GregorianCalendar();
                            calendar.setTime(mUser.getBirthday());
                            cal.setTime(mUser.getBirthday());
                            cal.add(Calendar.DATE, 4);
                            Date compareBday = date;
                            compareBday.setYear(mUser.getBirthday().getYear());
                            String year;
                            if (Integer.parseInt(monthFormat.format(date))==1&&Integer.parseInt(monthFormat.format(mUser.getBirthday()))==12){
                                yearDate.setYear(yearDate.getYear()-1);
                                 year= yearFormat.format(yearDate);

                            }
                            else if(Integer.parseInt(monthFormat.format(date))==12&&Integer.parseInt(monthFormat.format(mUser.getBirthday()))==1){
                                yearDate.setYear(yearDate.getYear()+1);
                                year = yearFormat.format(yearDate);
                                compareBday.setYear(mUser.getBirthday().getYear()-1);
                            }
                            else{
                                year = yearFormat.format(yearDate);
                            }
                            Date afterBday = cal.getTime();
                            GregorianCalendar cal2 = new GregorianCalendar();
                            GregorianCalendar calender2 = new GregorianCalendar();
                            calender2.setTime(mUser.getBirthday());
                            cal2.setTime(mUser.getBirthday());
                            cal2.add(Calendar.DATE, -4);

                            Date beforeBday = cal2.getTime();
                            String birthday = dateFormat.format(mUser.getBirthday());
                            String todaysDate = dateFormat.format(date);
                            if (!cognitoUserDetails.getAttributes().getAttributes().get("profile").contains("true")) {

                                if (!newReward.getUsedBy().contains(username) && (newReward.getFormattedStartDate().before(date)||format.format(newReward.getFormattedStartDate()).contains(format.format(date))) && (newReward.getFormattedEndDate().after(date)||format.format(newReward.getFormattedEndDate()).contains(format.format(date)))) {
                                    mRewards.add(newReward);
                                } else if (!newReward.getUsedBy().contains(username + year) && newReward.getRewardName().contains("Birthday") && beforeBday.before(compareBday)&& afterBday.after(compareBday)) {

                                    newReward.setFormattedEndDate(afterBday);
                                    newReward.setFormattedStartDate(beforeBday);
                                    mRewards.add(newReward);
                                }
                            }
                            else{
                                mRewards.add(newReward);
                            }

                        }
                        Log.i(TAG, Integer.toString(mRewards.size()));

                    }
                });

                refreshThread.start();
                try {
                    refreshThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((RewardAdapter) getListAdapter()).notifyDataSetChanged();



            }

            @Override
            public void onFailure(Exception exception) {

                Toast.makeText(getContext(), "Could not fetch user details! "+exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        Intent intent = getActivity().getIntent();
        username=intent.getStringExtra("name");
        user = AppHelper.getPool().getUser(username);
        user.getDetailsInBackground(detailsHandler);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_START_DATE) {
            final Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            updateDateView(startDate, date);
            mReward.setFormattedStartDate(date);
        } else if (requestCode == REQUEST_END_DATE) {
            final Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            updateDateView(endDate, date);
            mReward.setFormattedEndDate(date);

        }

    }
    private void updateDateView(Button dateButton, Date date) {
        dateButton.setText(DateFormat.format(DATE_FORMAT, date));
    }
    private class RewardAdapter extends ArrayAdapter<Rewards> {

        public RewardAdapter(ArrayList<Rewards> Rewards) {
            super(getActivity(), 0, Rewards);
        }
        @Override
        public int getCount(){
            return mRewards.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView==null){
                convertView= getActivity().getLayoutInflater().inflate(R.layout.fragment_reward_list_item,  null);
            }
            Rewards reward= getItem(position);
            Log.i(TAG, "added item");
            TextView nameTextView = (TextView)convertView.findViewById(R.id.reward_list_item_textName);
            nameTextView.setText(reward.getRewardName());
            TextView desciptionTextView = (TextView) convertView.findViewById(R.id.reward_list_item_textDescription);
            desciptionTextView.setText(reward.getDescription());


            return convertView;

        }
    }
    private Date getDateFromView(Button dateButton) {

        String dateString;
        Date date;

        dateString = dateButton.getText().toString();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.US);

        try {
            date = df.parse(dateString);
        }
        catch (Exception e) {
            date = new Date();
            Log.d(TAG, "Exception: " + e);
        }
        return(date);
    }
    @Override
    public void onResume() {
        refreshRewardList();
        super.onResume();
    }
}
