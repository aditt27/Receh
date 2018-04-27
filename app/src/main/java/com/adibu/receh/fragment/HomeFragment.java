package com.adibu.receh.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adibu.receh.DetailActivity;
import com.adibu.receh.MainActivity;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    HomeAdapter mHomeAdapter;
    ListView itemListView;

    //FIREBASE
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    private DatabaseReference mDatabaseItemReference;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        //FETCH DATA FROM DATABASE
        try {
            mDatabaseItemReference = mDatabase.getReference().child(RecehContract.RECEH_DATA).child(mUser.getUid()).child(RecehContract.getSelectedRecehData());
            mDatabaseItemReference.addChildEventListener(childEventListener);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //INITIATE LISTVEW AND ITS ADAPTER
        itemListView = (ListView)rootView.findViewById(R.id.list_view);
        mHomeAdapter = new HomeAdapter(getContext(), 0);
        itemListView.setAdapter(mHomeAdapter);

        //SET EMPTY VIEW
        View emptyView = rootView.findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        //SET ITEM CLICK LISTENER
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("TITLE", mHomeAdapter.getItem(position).getTitle());
                intent.putExtra("VALUES", mHomeAdapter.getItem(position).getValues());
                intent.putExtra("TRANSACTION", mHomeAdapter.getItem(position).isTransaction());
                intent.putExtra("DATE", mHomeAdapter.getItem(position).getDate());
                intent.putExtra("TIME", mHomeAdapter.getItem(position).getTime());
                intent.putExtra("DESCRIPTION", mHomeAdapter.getItem(position).getDescription());
                intent.putExtra("KEY", mHomeAdapter.getItem(position).getKey());
                startActivity(intent);
            }
        });

        return rootView;
    }

    //CREATE LISTENER TO RETRIEVE DATA
    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            RecehItem newItem = dataSnapshot.getValue(RecehItem.class);
            newItem.setKey(dataSnapshot.getKey());
            mHomeAdapter.add(newItem);
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            //REFRESH ALL ITEM
            mHomeAdapter.clear();
            mDatabaseItemReference.removeEventListener(childEventListener);
            mDatabaseItemReference.addChildEventListener(childEventListener);
        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            RecehItem deletedItem = dataSnapshot.getValue(RecehItem.class);
            //REMOVE MENGGUNAKAN EQUALS DALAM MENCARI DATA
            mHomeAdapter.remove(deletedItem);
        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            databaseError.toException();
        }
    };

    //ADAPTER TO DISPLAY DATA
    class HomeAdapter extends ArrayAdapter<RecehItem> {

        public HomeAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItemView = convertView;
            if(listItemView == null) {
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.list_home_item, parent, false);
            }

            RecehItem currentItem = getItem(position);

            TextView title = (TextView)listItemView.findViewById(R.id.home_item_title);
            title.setText(currentItem.getTitle());

            TextView values = (TextView)listItemView.findViewById(R.id.home_item_value);
            values.setText(currentItem.getValues().toString());

            ImageView transaction = (ImageView)listItemView.findViewById(R.id.home_item_transaction);
            if(currentItem.isTransaction()){
                transaction.setImageResource(R.drawable.ic_income);
            }
            else {
                transaction.setImageResource(R.drawable.ic_outcome);
            }

            return listItemView;
        }
    }
}
