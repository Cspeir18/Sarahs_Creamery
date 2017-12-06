package com.example.cspeir.sarahscreamery;

import android.os.Bundle;
import android.os.Looper;
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

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cspeir on 11/11/2017.
 */

public class FlavorsListFragment extends ListFragment {
    private static final String TAG = "FlavorListFragment";
    private ArrayList<Flavor> mFlavors;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //get the value of mPublicView from the intent. By default, it will be set to false (the second parameter in the call below)


        //Create the list of trips
        mFlavors = new ArrayList<Flavor>();
        refreshFlavorList();

        //Create the Adapter that will control the ListView for the fragment
        //The adapter is responsible for feeding the data to the list view
        FlavorAdapter adapter = new FlavorAdapter(mFlavors);
        setListAdapter(adapter);

    }
    public FlavorsListFragment(){

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup v, final Bundle bundle) {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        super.onCreateView(inflater, v, bundle);
        View rootView = inflater.inflate(R.layout.fragment_flavors_list, v, false);


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
                View mView = getLayoutInflater(bundle).inflate(R.layout.new_flavor, null);
                final EditText flavorEt = (EditText) mView.findViewById(R.id.new_flavor_et);
                Button flavorButton = (Button) mView.findViewById(R.id.new_flavor_btn);
                builder.setView(mView);
                final AlertDialog dialog = builder.create();
                dialog.show();
                final Flavor mFlavor = new Flavor();
                flavorButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!flavorEt.getText().toString().isEmpty()){
                            mFlavor.setFlavorName(flavorEt.getText().toString().trim());
                            mFlavor.setShared(true);
                            Backendless.Persistence.of(Flavor.class).save(mFlavor, new AsyncCallback<Flavor>() {
                                @Override
                                public void handleResponse(Flavor response) {
                                    Toast.makeText(getContext(), "New flavor added", Toast.LENGTH_SHORT).show();
                                    refreshFlavorList();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(getContext(), "Failed to add flavor: "+ fault.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
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
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        if (currentUser.getProperty("admin").equals(true)) {
            getActivity().getMenuInflater().inflate(R.menu.menu_flavor_list_item_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        FlavorAdapter adapter = (FlavorAdapter)getListAdapter();
        Flavor flavor = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.menu_item_delete_trip:
                //delete the trip from the baas
                deleteFlavor(flavor);
                return true;
        }
        return super.onContextItemSelected(item);
    }
    private void deleteFlavor(Flavor flavor) {
            final Flavor deleteFlavor = flavor;
            Thread deleteTread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Backendless.Data.of(Flavor.class).remove(deleteFlavor);
                    Looper.prepare();
                    Toast.makeText(getContext(), "Flavor removed", Toast.LENGTH_SHORT).show();
                    refreshFlavorList();
                }
            });
            deleteTread.start();

    }
    private void refreshFlavorList() {

        // todo: Activity 3.1.4
        BackendlessUser user = Backendless.UserService.CurrentUser();
        DataQueryBuilder dq = DataQueryBuilder.create();
        dq.setPageSize(40);
        dq.setWhereClause("shared = true");
        dq.setSortBy("created");
        Backendless.Persistence.of(Flavor.class).find(dq, new AsyncCallback<List<Flavor>>() {
            @Override
            public void handleResponse(List<Flavor> response) {
                Log.i(TAG, "refresh success");
                mFlavors.clear();
                for(int i=0; i<response.size();i++){
                    mFlavors.add(response.get(i));
                }
                ((FlavorAdapter)getListAdapter()).notifyDataSetChanged();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.i(TAG, "refresh failed"+fault.getMessage());
            }
        });
    }
    private class FlavorAdapter extends ArrayAdapter<Flavor> {

        public FlavorAdapter(ArrayList<Flavor> flavors) {
            super(getActivity(), 0, flavors);
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
            Flavor flavor = getItem(position);
            TextView nameTextView = (TextView)convertView.findViewById(R.id.flavor_list_item_textName);
            nameTextView.setText(flavor.getFlavorName());

            return convertView;

        }
    }
}
