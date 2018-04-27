package com.adibu.receh.fragment;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adibu.receh.R;
import com.adibu.receh.data.RecehContract;
import com.adibu.receh.data.RecehItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment {

    private TextView total;
    int sum = 0;

    //FIREBASE
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    private DatabaseReference mDatabaseItemReference;

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        mDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        //FETCH DATA FROM DATABASE
        try {
            mDatabaseItemReference = mDatabase.getReference().child(RecehContract.RECEH_DATA).child(mUser.getUid()).child(RecehContract.getSelectedRecehData());
            mDatabaseItemReference.addChildEventListener(childEventListener);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        total = (TextView)rootView.findViewById(R.id.stats_fragment_total_number);

        //RESET SUM TO 0 WHEN DATA CHANGE
        sum=0;
        return rootView;
    }

    //CREATE LISTENER TO RETRIEVE DATA
    ChildEventListener childEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            RecehItem newItem = dataSnapshot.getValue(RecehItem.class);
            int values = newItem.getValues();
            if(!newItem.isTransaction()){//OUTCOME
                values = -values;
            }
            sum += values;
            total.setText(String.valueOf(sum));
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            RecehItem deletedItem = dataSnapshot.getValue(RecehItem.class);
            int values = deletedItem.getValues();
            if(!deletedItem.isTransaction()){//OUTCOME
                values = -values;
            }
            sum = sum-values;
            total.setText(String.valueOf(sum));
        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
