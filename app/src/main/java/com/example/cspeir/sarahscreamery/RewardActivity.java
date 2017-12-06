package com.example.cspeir.sarahscreamery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by cspeir on 12/6/2017.
 */

public class RewardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the view for the activity to be the xml layout screen that has the FrameLayout that will contain the trip fragment (which in turn uses fragment_trip.xml)
        setContentView(R.layout.activity_reward);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = null;

        if (fragment == null) {
            fragment = new RewardFragment();
            manager.beginTransaction()
                    .add(R.id.tripFragmentContainer, fragment)
                    .commit();
        }
    }

}
