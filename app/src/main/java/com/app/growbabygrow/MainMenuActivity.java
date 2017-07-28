package com.app.growbabygrow;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private ImageButton delete;

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
        delete = (ImageButton) findViewById((R.id.deletebtn));

        //Default first project will always use this constant R.string.preference_file_key1
        sharedpreferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);
        String name = sharedpreferences.getString(getString(R.string.p_file1_saved_name), null);
        String period = sharedpreferences.getString(getString(R.string.p_file1_saved_period), null);

        if (name == null) {//no projects exist
            ShowBabyGrowNew();
            savedexists = false;
            startbabygrow = false;
        }
        else {
            savedexists = true;
            startbabygrow = true;
            ShowBabyGrowReady(name, period);
        }

        fabnewproj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!startbabygrow) {
                    if (!savedexists) {
                        ShowBabyGrowInput();
                        savedexists = true;
                    }
                    else {
                        if (!txtname.getText().toString().isEmpty() && !timeperiods.getSelectedItem().toString().equals("Select video schedule..")) {
                            try
                            {
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString(getString(R.string.p_file1_saved_name), txtname.getText().toString());
                                editor.putString(getString(R.string.p_file1_saved_period), timeperiods.getSelectedItem().toString());
                                editor.apply();

                                startbabygrow = true;
                                ShowBabyGrowReady(txtname.getText().toString(), timeperiods.getSelectedItem().toString());

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

        delete.setOnClickListener(new View.OnClickListener() {

              @Override
              public void onClick(View v) {
                  new AlertDialog.Builder(MainMenuActivity.this)
                          .setTitle("!Delete Baby Grow Session!")
                          .setMessage("Are you sure you want to delete all videos and info on your Baby Grow?")
                          .setIcon(android.R.drawable.ic_dialog_alert)
                          .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                              public void onClick(DialogInterface dialog, int whichButton) {
                                  ClearSharedDefault();
                                  //todo add logic to delete actual videos
                                  ShowBabyGrowNew();
                                  savedexists = false;
                                  startbabygrow = false;
                              }})
                          .setNegativeButton(android.R.string.no, null).show();
              }
        });


    }

    private void ShowBabyGrowNew()
    {
        tvnewproject.setText(R.string.add_new_project);
        fabnewproj.setImageResource(android.R.drawable.ic_input_add);
        timeperiods.setVisibility(View.INVISIBLE);
        txtname.setVisibility(View.INVISIBLE);
        delete.setVisibility(View.INVISIBLE);
    }

    private void ShowBabyGrowInput()
    {
        tvnewproject.setText(R.string.save_project_settings);
        fabnewproj.setImageResource(android.R.drawable.ic_menu_save);
        txtname.setVisibility(View.VISIBLE);
        txtname.setEnabled(true);
        timeperiods.setVisibility(View.VISIBLE);
        timeperiods.setEnabled(true);
        delete.setVisibility(View.INVISIBLE);
    }

    private void ShowBabyGrowReady(String name, String period) {
        tvnewproject.setText(R.string.go_capture_start);
        fabnewproj.setImageResource(android.R.drawable.star_big_on);
        txtname.setVisibility(View.VISIBLE);
        txtname.setText(name);
        txtname.setEnabled(false);
        timeperiods.setVisibility(View.VISIBLE);
        timeperiods.setSelection(getSpinnerIndex(timeperiods, period));
        timeperiods.setEnabled(false);
        delete.setVisibility(View.VISIBLE);
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

    private void ClearSharedDefault() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        Toast.makeText(MainMenuActivity.this, "Everything has been deleted", Toast.LENGTH_SHORT).show();
    }


}
