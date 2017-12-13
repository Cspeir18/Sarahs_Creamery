package com.example.cspeir.sarahscreamery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

/**
 * Created by cspeir on 11/16/2017.
 */

public class HomeFragment extends Fragment {
    TextView helloText;
    TextView saraText;

    public HomeFragment(){

    }
    public View onCreateView(LayoutInflater inflater , ViewGroup v, Bundle bundle) {
        super.onCreateView(inflater, v, bundle);
        View rootView = inflater.inflate(R.layout.fragment_home, v, false);
        helloText= (TextView) rootView.findViewById(R.id.hello);
        helloText.setText("Hello "+getUserName()+ "!");


        return rootView;


    }
    public String getUserName(){
        BackendlessUser bUser = Backendless.UserService.CurrentUser();
        return bUser.getProperty("firstName").toString();

    }

}
