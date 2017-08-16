package com.app.growbabygrow;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.app.growbabygrow.Classes.Helpers;
import com.app.growbabygrow.Classes.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import static com.app.growbabygrow.R.id.fab;


public class MainMenuActivity extends AppCompatActivity {
    public static final String TAG = "MainMenuActivity";
    private TextView tvnewproject;
    private FloatingActionButton fabnewproj;
    private EditText txtname;
    private Boolean savedexists;
    private Boolean startbabygrow;
    private Spinner timeperiods;
    private ImageButton delete;
    private ImageButton hamburger;
    private Switch camSwitch;

    private Context context;

    private SharedPreferences sharedpreferences;

    private File baseVideoFileDir;

    private String Name;
    private String Period;
    private Boolean MainMergedVideoOutputFilepath_has_Audio;

    private boolean usingFrontCamera = false;

    private Integer GetHash()
    {
        return Math.abs((Name + Period).hashCode());
    }

    private File MainMergedVideoOutputFilePath()
    {
        //simple non unique hash
        return new File(baseVideoFileDir, "main_merge_" + GetHash() + ".mp4");
    }

    private File MainMergedVideoOutputFilePath_With_Audio()
    {
        //simple non unique hash
        return new File(baseVideoFileDir, "main_merge_audio" + GetHash() + ".mp4");
    }

    private File OriginalVideoOutputFilePath()
    {
        return new File(baseVideoFileDir, "orig_" + GetHash() + ".mp4");
    }

    private File TrimmedVideoOutputFilePath(int number)
    {
        String trimVideoFileName = "trim_" + number + "_" + GetHash() + ".mp4";
        return new File(baseVideoFileDir, trimVideoFileName);
    }

    private File IntroVideoOutputFilePath()
    {
        return new File(baseVideoFileDir, "intro_" + GetHash() + ".mp4");
    }

    private File OverlayBitmapFilePath()
    {
        return new File(baseVideoFileDir, "overlay_" + GetHash() + ".bmp");
    }


    private ListView mDrawerList;
    private RelativeLayout mDrawerPane;
    private Helpers.DrawerListAdapter mAdapter;
    private DrawerLayout mDrawerLayout;
    private ArrayList<Helpers.NavItem> mNavItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main_menu);

            if (getIntent().getBooleanExtra("Exit me", false)) {
                finish();
                return; // add this to prevent from doing unnecessary stuffs
            }

            context = getApplicationContext();

            baseVideoFileDir = context.getExternalFilesDir(null);
            txtname = (EditText) findViewById(R.id.editTextName);
            timeperiods = (Spinner) findViewById(R.id.spinnerTime);
            fabnewproj = (FloatingActionButton) findViewById(fab);
            tvnewproject = (TextView) findViewById(R.id.TV_noProjects);
            delete = (ImageButton) findViewById(R.id.deletebtn);
            hamburger = (ImageButton) findViewById(R.id.btn_hamburger);
            camSwitch = (Switch) findViewById(R.id.switchCameraface);


            populateDrawer();

            //Default first project will always use this constant R.string.preference_file_key1
            sharedpreferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);
            Name = sharedpreferences.getString(getString(R.string.p_file1_saved_name), null);
            Period = sharedpreferences.getString(getString(R.string.p_file1_saved_period), null);
            MainMergedVideoOutputFilepath_has_Audio = sharedpreferences.getBoolean(getString(R.string.p_file1_saved_main_has_audio), false);


            if (Name == null) {//no projects exist
                ShowBabyGrowNew();
                savedexists = false;
                startbabygrow = false;
            } else {
                savedexists = true;
                startbabygrow = true;
                ShowBabyGrowReady(Name, Period);
            }

            fabnewproj.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!startbabygrow) {
                        if (!savedexists) {
                            ShowBabyGrowInput();
                            savedexists = true;
                        } else {
                            if (!txtname.getText().toString().isEmpty() && !timeperiods.getSelectedItem().toString().equals("Select video schedule..")) {
                                try {
                                    Name = txtname.getText().toString();
                                    Period = timeperiods.getSelectedItem().toString();

                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    //set isnew flag so we can create intro movie later this will be set to false after intro video is created and merged in videoedit
                                    editor.putBoolean(getString(R.string.p_file1_is_new), true);

                                    editor.putString(getString(R.string.p_file1_saved_name), Name);
                                    editor.putString(getString(R.string.p_file1_saved_period), Period);
                                    editor.putString(getString(R.string.p_file1_saved_selected_last_week_face_bitmap_path), OverlayBitmapFilePath().getAbsolutePath());
                                    editor.apply();

                                    startbabygrow = true;
                                    ShowBabyGrowReady(Name, Period);

                                } catch (Exception ex) {
                                    Log.d(TAG, ex.getMessage());
                                }

                            } else {
                                Toast.makeText(context, "Please enter all required fields to proceed!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    } else {

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        if (sharedpreferences.getString(getString(R.string.p_file1_saved_main_mp4pathname), null) == null) //if main merge video file does not exist create new names and save for all files
                        {
                            editor.putString(getString(R.string.p_file1_saved_main_mp4pathname), MainMergedVideoOutputFilePath().getAbsolutePath());
                            editor.putString(getString(R.string.p_file1_saved_main_mp4pathname_with_audio), MainMergedVideoOutputFilePath_With_Audio().getAbsolutePath());
                            editor.putBoolean(getString(R.string.p_file1_saved_main_has_audio), false); //at first we have no audio
                            editor.putString(getString(R.string.p_file1_saved_orig_mp4pathname), OriginalVideoOutputFilePath().getAbsolutePath());
                            editor.putString(getString(R.string.p_file1_saved_trim1_mp4pathname), TrimmedVideoOutputFilePath(1).getAbsolutePath());
                            editor.putString(getString(R.string.p_file1_saved_trim2_mp4pathname), TrimmedVideoOutputFilePath(2).getAbsolutePath());
                            editor.putString(getString(R.string.p_file1_saved_trim3_mp4pathname), TrimmedVideoOutputFilePath(3).getAbsolutePath());
                            editor.putString(getString(R.string.p_file1_saved_intro_mp4pathname), IntroVideoOutputFilePath().getAbsolutePath());
                            editor.putString(getString(R.string.p_file1_saved_intro_mp4pathname), IntroVideoOutputFilePath().getAbsolutePath());
                        }

                        editor.putBoolean(getString(R.string.p_file1_saved_current_session_camera_facing_is_front), usingFrontCamera); //always send this as it can change
                        editor.apply();

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
                                    ClearMediaFiles();
                                    ShowBabyGrowNew();
                                    savedexists = false;
                                    startbabygrow = false;
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });

            mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectItemFromDrawer(position);
                }
            });

            hamburger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(mDrawerPane);
                }
            });

            camSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    usingFrontCamera = camSwitch.isChecked();
                }
            });


    }

    private void populateDrawer()// Populate the Navigation Drawer with options
    {
        mNavItems.add(new Helpers.NavItem(getString(R.string.StartDrawer),"Start Menu", "Begin New BabyGrow", android.R.drawable.star_big_on));
        mNavItems.add(new Helpers.NavItem(getString(R.string.VideoDrawer), "View Video", "View Saved BabyGrow", R.drawable.video_icon));
        mNavItems.add(new Helpers.NavItem(getString(R.string.MusicDrawer), "Add Music", "Baby Grow Music", R.drawable.music_icon));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        mAdapter = new Helpers.DrawerListAdapter(context, mNavItems, mDrawerList, 0);
        mDrawerList.setAdapter(mAdapter);
    }

    private void selectItemFromDrawer(int position) {
        Helpers.NavItem currentNav = mNavItems.get(position);
        mDrawerList.setItemChecked(position, true);

        for (Map.Entry<Integer, View> e : mAdapter.Views.entrySet()) {
            if (e.getKey() == position)
                e.getValue().setBackgroundColor(Color.GRAY);
            else
                e.getValue().setBackgroundColor(Color.WHITE);

        }
        setTitle(currentNav.mTitle);

        if (currentNav.mName.equals(getString(R.string.StartDrawer))) //do nothing if current activity is selected
        {
            mDrawerLayout.closeDrawer(mDrawerPane);
        }
        else if (currentNav.mName.equals(getString(R.string.VideoDrawer)))
        {
            Intent intent = new Intent(MainMenuActivity.this, VideoViewActivity.class);

            if (!MainMergedVideoOutputFilepath_has_Audio)
                intent.putExtra(getString(R.string.player_video_file_path), MainMergedVideoOutputFilePath().getAbsolutePath());
            else
                intent.putExtra(getString(R.string.player_video_file_path), MainMergedVideoOutputFilePath_With_Audio().getAbsolutePath());

            intent.putExtra(getString(R.string.ActivityName), TAG);
            startActivity(intent);
        }
        else if (currentNav.mName.equals(getString(R.string.MusicDrawer)))
        {
            Intent intent = new Intent(MainMenuActivity.this, AudioActivity.class);
            intent.putExtra(getString(R.string.ActivityName), TAG);
            startActivity(intent);
        }
        else
        {
            mDrawerLayout.closeDrawer(mDrawerPane);
        }

    }


    private void ShowBabyGrowNew()
    {
        tvnewproject.setText(R.string.add_new_project);
        fabnewproj.setImageResource(android.R.drawable.ic_input_add);
        timeperiods.setVisibility(View.INVISIBLE);
        txtname.setVisibility(View.INVISIBLE);
        delete.setVisibility(View.INVISIBLE);
        camSwitch.setVisibility(View.INVISIBLE);
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
        camSwitch.setVisibility(View.VISIBLE);
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

    private void ClearMediaFiles()
    {
        if (IntroVideoOutputFilePath().exists())
            IntroVideoOutputFilePath().delete();

        if (TrimmedVideoOutputFilePath(1).exists())
            TrimmedVideoOutputFilePath(1).delete();

        if (TrimmedVideoOutputFilePath(2).exists())
            TrimmedVideoOutputFilePath(2).delete();

        if (TrimmedVideoOutputFilePath(3).exists())
            TrimmedVideoOutputFilePath(3).delete();

        if (OriginalVideoOutputFilePath().exists())
            OriginalVideoOutputFilePath().delete();

        if (OverlayBitmapFilePath().exists())
            OverlayBitmapFilePath().delete();

//        if (MainMergedVideoOutputFilePath().exists()) //leave for now
//            MainMergedVideoOutputFilePath().delete();

    }

    @Override
    public void onBackPressed(){
        finish();
    }


    Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler(){

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            String tag = "Unhandled Exception";
            Log.d(tag, ex.getMessage(), ex);

            Helpers.Logger.LogExceptionToFile(tag, Helpers.Logger.ErrorLoggerFilePath(getApplicationContext(), "Unhandled"), ex, thread.getName());
        }

    };


}
