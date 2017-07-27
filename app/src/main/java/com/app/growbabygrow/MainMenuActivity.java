package com.app.growbabygrow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import static com.app.growbabygrow.R.id.fab;


public class MainMenuActivity extends AppCompatActivity {
    private static final String TAG = "MainMenuActivity";
    private TextView tvnewproject;
    private FloatingActionButton fabnewproj;
    private EditText txtname;
    private Boolean savedexists;
    private Boolean startbabygrow;
    private Spinner timeperiods;

    private Context context;

    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        context = getApplicationContext();
        txtname = (EditText) findViewById(R.id.editTextName);
        timeperiods = (Spinner) findViewById(R.id.spinnerTime);
        fabnewproj = (FloatingActionButton) findViewById(fab);
        tvnewproject = (TextView) findViewById(R.id.TV_noProjects);

        sharedpreferences = getSharedPreferences(getString(R.string.preference_file_key1), Context.MODE_PRIVATE);
        String name = sharedpreferences.getString(getString(R.string.saved_name), null);
        String period = sharedpreferences.getString(getString(R.string.saved_period), null);

        if (name == null) {//no projects exist
            savedexists = false;
            startbabygrow = false;
        }
        else {
            savedexists = true;
            startbabygrow = true;
            SetupBabyGrow(name, period);
        }

        fabnewproj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!startbabygrow) {
                    if (!savedexists) {
                        tvnewproject.setText(R.string.save_project_settings);
                        fabnewproj.setImageResource(android.R.drawable.ic_menu_save);
                        txtname.setVisibility(View.VISIBLE);
                        timeperiods.setVisibility(View.VISIBLE);
                        savedexists = true;
                    }
                    else {
                        if (!txtname.getText().toString().isEmpty() && !timeperiods.getSelectedItem().toString().equals("Select video schedule..")) {
                            try
                            {
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString(getString(R.string.saved_name), txtname.getText().toString());
                                editor.putString(getString(R.string.saved_period), timeperiods.getSelectedItem().toString());
                                editor.apply();

                                startbabygrow = true;
                                SetupBabyGrow(txtname.getText().toString(), timeperiods.getSelectedItem().toString());

                            } catch (Exception ex) {
                                Log.d(TAG, ex.getMessage());
                            }

                        }
                        else
                        {
                            Toast.makeText(context, "Please enter all required fields to proceed!", Toast.LENGTH_SHORT).show();
                        }

                    }
                } else {
                    Intent intent = new Intent(MainMenuActivity.this, CaptureActivity.class);
                    startActivity(intent);
                }
            }
        });


    }

    private void SetupBabyGrow(String name, String period) {
        tvnewproject.setText(R.string.go_capture_start);
        fabnewproj.setImageResource(android.R.drawable.star_big_on);
        txtname.setVisibility(View.VISIBLE);
        txtname.setText(name);
        txtname.setEnabled(false);
        timeperiods.setVisibility(View.VISIBLE);
        timeperiods.setSelection(getSpinnerIndex(timeperiods, period));
        timeperiods.setEnabled(false);
    }

    private int getSpinnerIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void Clearshared() {

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preference_file_key1), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }


}
