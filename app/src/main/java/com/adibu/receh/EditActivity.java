package com.adibu.receh;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.adibu.receh.data.RecehContract;
import com.adibu.receh.data.RecehItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class EditActivity extends AppCompatActivity {

    private EditText mTitleEditText;
    private EditText mValuesEditText;
    private RadioGroup mTransactionRadioGroup;
    private RadioButton mTransactionIncomeRadio;
    private RadioButton mTransactionOutcomeRadio;
    private EditText mDescriptionEditText;
    private EditText mDateEditText;
    private EditText mTimeEditText;

    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    private DatabaseReference mDatabaseItemReference;

    private Bundle recehItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        recehItem = intent.getExtras();
        mDatabase = FirebaseDatabase.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseItemReference = mDatabase.getReference().child(RecehContract.RECEH_DATA).child(mUser.getUid()).child(RecehContract.getSelectedRecehData());

        mTitleEditText = (EditText)findViewById(R.id.activity_edit_title);
        mValuesEditText = (EditText)findViewById(R.id.activity_edit_values);
        mTransactionRadioGroup = (RadioGroup)findViewById(R.id.activity_edit_transaction);
        mTransactionIncomeRadio = (RadioButton)findViewById(R.id.activity_edit_income);
        mTransactionOutcomeRadio = (RadioButton)findViewById(R.id.activity_edit_outcome);
        mDescriptionEditText = (EditText)findViewById(R.id.activity_edit_description);
        mDateEditText = (EditText)findViewById(R.id.activity_edit_date);
        mTimeEditText = (EditText)findViewById(R.id.activity_edit_time);

        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        String monthString = String.valueOf(month+1);
                        if (monthString.length()==1) {
                            monthString = "0" + monthString;
                        }
                        String dayString = String.valueOf(dayOfMonth);
                        if (dayString.length()==1) {
                            dayString = "0" + dayString;
                        }
                        mDateEditText.setText(year + "-" + monthString + "-" + dayString);
                    }
                };

                DatePickerDialog dateDialog = new DatePickerDialog(EditActivity.this, 0, dateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dateDialog.show();
            }
        });
        mTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        String hourString = String.valueOf(hourOfDay);
                        if (hourString.length()==1) {
                            hourString = "0" + hourString;
                        }
                        String minuteString = String.valueOf(minutes);
                        if (minuteString.length()==1) {
                            minuteString = "0" + minuteString;
                        }
                        mTimeEditText.setText(hourString + ":" + minuteString);
                    }
                };

                TimePickerDialog timeDialog = new TimePickerDialog(EditActivity.this, timeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timeDialog.show();
            }
        });

        //CHECK IF FROM EDIT OR ADDING DATA
        if(recehItem==null) {
            setTitle(R.string.edit_activity_add_data);
        } else {
            setTitle(R.string.edit_activity_edit_data);

            //FILL EDIT TEXT WITH PREVIOUS DATA
            mTitleEditText.setText(recehItem.getString("TITLE"));
            mValuesEditText.setText(String.valueOf(recehItem.getInt("VALUES")));
            mDateEditText.setText(recehItem.getString("DATE"));
            mTimeEditText.setText(recehItem.getString("TIME"));
            mDescriptionEditText.setText(recehItem.getString("DESCRIPTION"));

            if(recehItem.getBoolean("TRANSACTION")) {
                mTransactionIncomeRadio.setChecked(true);
            }
            else if(!recehItem.getBoolean("TRANSACTION"))  {
                mTransactionOutcomeRadio.setChecked(true);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                saveItemData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItemData() {

        String title = mTitleEditText.getText().toString().trim();
        String values = mValuesEditText.getText().toString();
        String description = mDescriptionEditText.getText().toString().trim();
        String date = mDateEditText.getText().toString().trim();
        String time = mTimeEditText.getText().toString().trim();

        boolean transaction = false;
        int radioSelectedId = mTransactionRadioGroup.getCheckedRadioButtonId();
        if (radioSelectedId == R.id.activity_edit_income) {
            transaction = true;
        } else if (radioSelectedId == R.id.activity_edit_outcome){
            transaction = false;
        }

        if(title.isEmpty()) {
            mTitleEditText.setError(getString(R.string.edit_activity_empty_title));
        }
        if (values.isEmpty()) {
            mValuesEditText.setError(getString(R.string.edit_activity_empty_values));
        }
        if (date.isEmpty()) {
            mDateEditText.setError(getString(R.string.edit_activity_empty_date));
        }
        if (time.isEmpty()) {
            mTimeEditText.setError(getString(R.string.edit_activity_empty_time));
        }
        
        if (!title.isEmpty() && !values.isEmpty() && !date.isEmpty() && !time.isEmpty()) {
            Integer intValues = Integer.parseInt(values);
            String datetime = date + " " + time;
            if(recehItem==null) { //ADD ITEM
                RecehItem newItem = new RecehItem(title, intValues, transaction, date, time, description);
                mDatabaseItemReference.push().setValue(newItem);
            } else { //EDIT ITEM
                RecehItem editItem = new RecehItem(title, intValues, transaction, date, time, description);
                mDatabaseItemReference.child(recehItem.getString("KEY")).setValue(editItem);
            }
            finish();
        }
    }
}
