package com.example.cspeir.sarahscreamery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by cspeir on 12/29/2017.
 */

public class AboutFragment extends Fragment {
    public AboutFragment(){

    }
    public View onCreateView(LayoutInflater inflater , ViewGroup v, Bundle bundle) {
        super.onCreateView(inflater, v, bundle);
        View rootView = inflater.inflate(R.layout.fragment_about, v, false); //has about info in the xml final

        return rootView;
    }
}
