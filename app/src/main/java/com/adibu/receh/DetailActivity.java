package com.adibu.receh;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.adibu.receh.data.RecehContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailActivity extends AppCompatActivity {

    private TextView mTitleTV;
    private TextView mTransactionTV;
    private TextView mValuesTV;
    private TextView mDateTimeTV;
    private TextView mDescriptionTV;
    private Bundle recehItem;

    //FIREBASE
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    private DatabaseReference mDatabaseItemReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        recehItem = intent.getExtras();

        mDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseItemReference = mDatabase.getReference().child(RecehContract.RECEH_DATA).child(mUser.getUid()).child(RecehContract.getSelectedRecehData());


        mTitleTV = (TextView)findViewById(R.id.activity_detail_title);
        mTransactionTV = (TextView)findViewById(R.id.activity_detail_transaction);
        mValuesTV = (TextView)findViewById(R.id.activity_detail_values);
        mDescriptionTV = (TextView)findViewById(R.id.activity_detail_description);
        mDateTimeTV = (TextView)findViewById(R.id.activity_detail_datetime);
        mDescriptionTV.setMovementMethod(new ScrollingMovementMethod());

        mTitleTV.setText(recehItem.getString("TITLE"));
        mValuesTV.setText(String.valueOf(recehItem.getInt("VALUES")));
        mDateTimeTV.setText(recehItem.getString("DATE")+" "+ recehItem.getString("TIME"));
        mDescriptionTV.setText(recehItem.getString("DESCRIPTION"));

        if(recehItem.getBoolean("TRANSACTION")) {
            mTransactionTV.setText(R.string.detail_activity_income);
        }
        else if(!recehItem.getBoolean("TRANSACTION"))  {
            mTransactionTV.setText(R.string.detail_activity_outcome);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_detail_edit:
                Intent intent = new Intent(DetailActivity.this, EditActivity.class);
                intent.putExtras(recehItem);
                startActivity(intent);
                finish();
                return true;
            case R.id.menu_detail_delete:
                deleteItemWithConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteItemWithConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.detail_activity_delete_confirmation_title);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteData();
                finish();
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

    private void deleteData() {
        mDatabaseItemReference.child(recehItem.getString("KEY")).removeValue();
    }
}
