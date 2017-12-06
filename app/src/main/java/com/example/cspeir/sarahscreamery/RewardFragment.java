package com.example.cspeir.sarahscreamery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by cspeir on 12/6/2017.
 */

public class RewardFragment extends Fragment {
    private Reward mReward;
    public RewardFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setHasOptionsMenu(true);
        String rewardName, description, directions, objectId;
        Intent intent = getActivity().getIntent();
        objectId = intent.getStringExtra("objectId");
        rewardName = intent.getStringExtra("rewardName");
        description = intent.getStringExtra("description");
        directions = intent.getStringExtra("directions");
        mReward = new Reward();
        mReward.setDirection(directions);
        mReward.setDescription(description);
        mReward.setRewardName(rewardName);
        mReward.setObjectId(objectId);
        mReward.setShared(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_reward, parent, false);
        TextView rewardNameText, rewarddescriptionText;
        rewardNameText = (TextView) v.findViewById(R.id.qr_reward_name);
        rewarddescriptionText = (TextView) v.findViewById(R.id.qr_reward_description);

        rewardNameText.setText(mReward.getRewardName());
        rewarddescriptionText.setText(mReward.getDescription());

        return v;
    }
}
