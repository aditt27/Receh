package com.adibu.receh;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adibu.receh.data.RecehContract;
import com.adibu.receh.data.RecehItem;
import com.adibu.receh.fragment.HomeFragment;
import com.adibu.receh.fragment.StatsFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private Drawer mDrawer;
    PrimaryDrawerItem addData;
    AccountHeader mainAccount;
    ProfileDrawerItem profile;

    //FIREBASE
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseDataReference;
    private DatabaseReference mDatabaseDataItemReference;
    private ChildEventListener mChildEventListener;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ADD TOOLBAR FOR DRAWER TO OVERLAY
        Toolbar toolbar = (Toolbar)findViewById(R.id.main_action_bar);
        setSupportActionBar(toolbar);

        //RESTORE TITLE KALO DI ROTATE
        if(savedInstanceState!=null) {
            setTitle(savedInstanceState.getString("TITLE"));
        }

        //INITIATE DATABASE
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        //CREATE DUMMY PROFILE TO SHOW FIRST BEFORE SIGN IN
        profile = new ProfileDrawerItem()
                .withName("Dummy")
                .withEmail("Dummy@dummy.com")
                .withIdentifier(101);

        //FIREBASE SIGNOUT DRAWER ITEM WITH LISTENER
        final PrimaryDrawerItem signOut = new PrimaryDrawerItem()
                .withName("Sign Out")
                .withSelectable(false);
        signOut.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                AuthUI.getInstance().signOut(MainActivity.this);
                mDrawer.closeDrawer();
                mViewPager.setAdapter(null);
                setTitle(getString(R.string.app_name));
                return true;
            }
        });

        //ADD DATA DRAWER ITEM WITH LISTENER
        addData = new PrimaryDrawerItem()
                .withName(R.string.main_navigation_add_data)
                .withIcon(R.drawable.add_circle)
                .withSelectable(false);

        //CREATE NAV_DRAWER
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withToolbar(toolbar)
                .addStickyDrawerItems(signOut)
                .addDrawerItems(new DividerDrawerItem(), addData)
                .build();

        //CREATE HEADER ACCOUNT FOR NAV_DRAWER
        mainAccount = new AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(profile)
                .withSelectionListEnabled(false)
                .withTextColorRes(R.color.headerColorText)
                .withHeaderBackground(R.drawable.header_drawer)
                .withDrawer(mDrawer)
                .build();

        //CREATE ITEM LISTENER FOR EACH ITEM
        mDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                try{
                    if (position != mDrawer.getDrawerItems().size()) {//BUKAN ADD DATA
                        CustomPrimaryItemDrawer selectedItem = (CustomPrimaryItemDrawer) drawerItem;
                        String data = selectedItem.getName().toString();
                        RecehContract.setSelectedRecehData(data);
                        setTitle(data);
                        //REFRESH VIEWPAGER
                        int currentfragment = mViewPager.getCurrentItem();
                        mViewPager.setAdapter(null);
                        mViewPager.setAdapter(mViewPagerAdapter);
                        mViewPager.setCurrentItem(currentfragment);
                    } else {
                        addNewData(true);
                    }
                } catch (ClassCastException e) {
                    //NOTHING
                }
                return false;
            }
        });
        mDrawer.setOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position, IDrawerItem drawerItem) {
                if (position != mDrawer.getDrawerItems().size()) {//BUKAN ADD DATA
                    CustomPrimaryItemDrawer selectedItem = (CustomPrimaryItemDrawer) drawerItem;
                    final String data = selectedItem.getName().toString();
                    final String key = selectedItem.getKey();
                    final int drawerPosition = position;

                    //final String rename = getString(R.string.rename);
                    final String delete = getString(R.string.delete);
                    CharSequence[] items = new CharSequence[]{/*rename,*/ delete};

                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(data);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            switch (position) {
                                case 0: //DELETE
                                    deleteDataWithConfirmationDialog(data, key, drawerPosition);
                                    break;
                            }
                        }
                    });
                    builder.show();
                }
                return false;
            }
        });

        //FIREBASE AUTHENTICATION LISTENER
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
                if(mUser!=null) {//USER SIGNED IN
                    //GET REFERENCE TO THE DATA
                    mDatabaseDataReference = mDatabase.getReference().child(RecehContract.USERS_DATA).child(mUser.getUid()).child(RecehContract.RECEH_DATA);
                    mDatabaseDataItemReference = mDatabase.getReference().child(RecehContract.RECEH_DATA).child(mUser.getUid());
                    //CREATE NEW PROFILE ITEM BASED ON SIGNED IN ACCOUNT
                    profile = new ProfileDrawerItem()
                            .withName(mUser.getDisplayName())
                            .withEmail(mUser.getEmail())
                            .withIcon(mUser.getPhotoUrl())
                            .withIdentifier(101);
                    //UPDATE THE PROFILE TO THE ACCOUNT HEADER TO THE DRAWER
                    mainAccount.updateProfile(profile);
                    attachDatabaseDataReadListener();
                }
                else {//USER SIGNED OUT
                    detachDatabaseDataReadListener();
                    mDrawer.removeAllItems();;
                    mDrawer.addItems(new DividerDrawerItem(), addData);
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        //CREATE FAB
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(RecehContract.getSelectedRecehData()==null) {
                    Toast.makeText(MainActivity.this, "Please select the data first", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(MainActivity.this, EditActivity.class));
                }

            }
        });

        //FRAGMENT
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        // Create an adapter that knows which fragment should be shown on each page
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        // Set the adapter onto the view pager
        mViewPager.setAdapter(mViewPagerAdapter);

        //TAB LAYOUT FOR FRAGMENT
        final TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

    }

    private void addNewData(boolean cancellable) {
        //bikin alert dialog buat ngasih nama datanya
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(R.layout.main_navigation_edit_data_dialog);
        builder.setTitle(getString(R.string.main_navigation_add_data_dialog_title));
        builder.setPositiveButton(getString(R.string.main_navigation_add_data_dialog_add),null);
        if(!cancellable) {
            builder.setCancelable(false);
        }
        else {
            builder.setNegativeButton(getString(R.string.main_navigation_add_data_dialog_cancel), null);
        }

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final DialogInterface d = dialogInterface;

                //clicklistener add button
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog f = (Dialog) d;
                        EditText addDataDialogEditText = (EditText)f.findViewById(R.id.main_navigation_add_data_title_edit_text);
                        String title = addDataDialogEditText.getText().toString().trim();

                        if(title.isEmpty()) {
                            addDataDialogEditText.setError(getString(R.string.edit_activity_empty_title));
                        }
                        else {
                            mDatabaseDataReference.push().setValue(title);
                            d.dismiss();
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void attachDatabaseDataReadListener() {
        if (mChildEventListener == null) {
            //Event listener untuk ngecek jika ada perubahan data di database
            mChildEventListener = new ChildEventListener() {
                int position = 0;
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    //GET DATA
                    final String data = dataSnapshot.getValue(String.class);
                    final String key = dataSnapshot.getKey();

                    //ASSIGN DATA TO THE DRAWER ITEM
                    CustomPrimaryItemDrawer newItem = (CustomPrimaryItemDrawer) new CustomPrimaryItemDrawer().withName(data);
                    newItem.setKey(key);

                    //ADD ITEM TO THE DRAWER
                    mDrawer.addItemAtPosition(newItem, ++position);

                    //SELECT THE FIRST ITEM WITH ONCLICK TRIGGERED
                    //mDrawer.getDrawerItems().size()==3 karena 2(Divider+addData) + (Data)
                    if(mDrawer.getDrawerItems().size()==3) {
                        mDrawer.setSelectionAtPosition(1, true);
                    }
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if(mDrawer.getDrawerItems().size()>2) {
                        mDrawer.setSelectionAtPosition(1, true);
                    } else if(mDrawer.getDrawerItems().size()==2) {
                        position = 0;
                        addNewData(false);
                    }
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    databaseError.toException();
                }
            };
            //INPUT LISTENER TO THE DATABASE REFERENCE
            mDatabaseDataReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseDataReadListener() {
        if (mChildEventListener != null) {
            mDatabaseDataReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void renameDataDialog(final String title, final String key) {
        //bikin alert dialog buat ngasih nama datanya
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(R.layout.main_navigation_edit_data_dialog);
        builder.setTitle(getString(R.string.rename) + " \"" + title + "\"");
        builder.setNegativeButton(getString(R.string.main_navigation_add_data_dialog_cancel), null);
        builder.setPositiveButton(getString(R.string.main_navigation_add_data_dialog_add),null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final DialogInterface d = dialogInterface;

                //clicklistener add button
                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog f = (Dialog) d;
                        EditText addDataDialogEditText = (EditText)f.findViewById(R.id.main_navigation_add_data_title_edit_text);
                        final String titleRename = addDataDialogEditText.getText().toString().trim();

                        if(titleRename.isEmpty()) {
                            addDataDialogEditText.setError(getString(R.string.edit_activity_empty_title));
                        } else {
                            CharSequence actionBarTitle = getTitle();
                            //TODO: RENAME DATA
                            //ubah actionBarTitle sesuai dengan data yang direname jika data yang di rename adalah data yang sedang dipilih
                            if(actionBarTitle.equals(title)) {
                                setTitle(titleRename);
                            }
                            d.dismiss();
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void deleteDataWithConfirmationDialog(final String title, final String key, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.main_naviation_item_delete_confirmation_title) + " \"" + title + "\" ?");
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDatabaseDataReference.child(key).removeValue();
                mDatabaseDataItemReference.child(title).removeValue();
                RecehContract.setSelectedRecehData(null);
                mDrawer.removeItemByPosition(position);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    //SAVE TITLE IF ROTATED
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("TITLE", getTitle().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private String[] tabTitles = {getString(R.string.main_activity_tab_home),getString(R.string.main_activity_tab_stats)};
        final int pageCount = tabTitles.length;

        private ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                default:
                    return new StatsFragment();
            }
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    private class CustomPrimaryItemDrawer extends PrimaryDrawerItem {
        String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
