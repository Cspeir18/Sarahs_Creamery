package com.example.cspeir.sarahscreamery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cspeir on 11/24/2017.
 */

public class RewardsListFragment extends ListFragment {
    Button newReward, startDate, endDate;
    EditText newName, newdescription, newDirections;
    private static final String TAG = "RewardsListFragment";
    private ArrayList<Reward> mRewards;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //get the value of mPublicView from the intent. By default, it will be set to false (the second parameter in the call below)


        //Create the list of trips
        mRewards = new ArrayList<Reward>();
        refreshRewardList();

        //Create the Adapter that will control the ListView for the fragment
        //The adapter is responsible for feeding the data to the list view
        RewardsListFragment.RewardAdapter adapter = new RewardsListFragment.RewardAdapter(mRewards);
        setListAdapter(adapter);

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
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.floating_action_button);
        if (currentUser.getProperty("admin").equals(false)){
            fab.setVisibility(View.GONE);
        }
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
                startDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                endDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                builder.setView(mView);
                final AlertDialog dialog = builder.create();
                dialog.show();
                final Reward mReward = new Reward();
                newReward.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!newdescription.getText().toString().trim().isEmpty()&& !newName.getText().toString().trim().isEmpty()&& !newDirections.getText().toString().trim().isEmpty()){
                            mReward.setRewardName(newName.getText().toString().trim());
                            mReward.setDescription(newdescription.getText().toString().trim());
                            mReward.setDirection(newDirections.getText().toString().trim());
                            mReward.setShared(true);
                            mReward.setStartDate((Date)startDate.getText());
                            Backendless.Persistence.of(Reward.class).save(mReward, new AsyncCallback<Reward>() {
                                @Override
                                public void handleResponse(Reward response) {
                                    Toast.makeText(getContext(), "New reward added", Toast.LENGTH_SHORT).show();
                                    refreshRewardList();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getContext(), "Failed to add reward: "+ fault.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                            dialog.dismiss();
                        }
                        else{
                            Toast.makeText(getContext(), "Please enter a name for the reward", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        return rootView;
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Reward r= (Reward) ((RewardAdapter)getListAdapter()).getItem(position);
        Log.d(TAG, r.toString() + " was clicked." + RewardsListFragment.class);
        Intent i = new Intent(getActivity(), RewardActivity.class);
        i.putExtra("rewardName", r.getRewardName());
        i.putExtra("directions", r.getDirection());
        i.putExtra("description", r.getDescription());
        i.putExtra("objectId", r.getObjectId());
        startActivity(i);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        if (currentUser.getProperty("admin").equals(true)) {
            getActivity().getMenuInflater().inflate(R.menu.menu_reward_list_item_context, menu);
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        RewardsListFragment.RewardAdapter adapter = (RewardsListFragment.RewardAdapter)getListAdapter();
        Reward reward = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.menu_item_delete_trip:
                //delete the trip from the baas
                deleteReward(reward);
                return true;
        }
        return super.onContextItemSelected(item);
    }
    private void deleteReward(Reward reward) {
        final Reward deleteReward = reward;


        Thread deleteTread = new Thread(new Runnable() {
            @Override
            public void run() {
                Backendless.Data.of(Reward.class).remove(deleteReward);
                Looper.prepare();
                Toast.makeText(getContext(), "Reward removed", Toast.LENGTH_SHORT).show();
                refreshRewardList();
            }
        });
        deleteTread.start();


    }
    private void refreshRewardList() {
        final BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        final User mUser = new User();
        mUser.setRewardsUsed((String)currentUser.getProperty("rewardsUsed"));
        // todo: Activity 3.1.4
        DataQueryBuilder dq = DataQueryBuilder.create();
        dq.setPageSize(40);
        dq.setWhereClause("shared = true");
        dq.setSortBy("created");
        Backendless.Persistence.of(Reward.class).find(dq, new AsyncCallback<List<Reward>>() {
            @Override
            public void handleResponse(List<Reward> response) {
                Log.i(TAG, "refresh success");
                mRewards.clear();
                for (int i = 0; i < response.size(); i++) {
                    if(!mUser.getRewardsUsed().contains(response.get(i).getObjectId())){
                        mRewards.add(response.get(i));
                    }
                }
                ((RewardAdapter) getListAdapter()).notifyDataSetChanged();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.i(TAG, "refresh failed" + fault.getMessage());
            }
        });
    }
    private class RewardAdapter extends ArrayAdapter<Reward> {

        public RewardAdapter(ArrayList<Reward> Rewards) {
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
            Reward reward= getItem(position);
            TextView nameTextView = (TextView)convertView.findViewById(R.id.reward_list_item_textName);
            nameTextView.setText(reward.getRewardName());
            TextView desciptionTextView = (TextView) convertView.findViewById(R.id.reward_list_item_textDescription);
            desciptionTextView.setText(reward.getDescription());


            return convertView;

        }
    }
    @Override
    public void onResume() {
        refreshRewardList();
        super.onResume();
    }
}
