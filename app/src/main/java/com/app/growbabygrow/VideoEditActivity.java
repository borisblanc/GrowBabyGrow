package com.app.growbabygrow;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.growbabygrow.Classes.Utils;
import com.app.growbabygrow.Classes.VideoUtils;

import java.io.File;
import java.io.IOException;

import static android.R.attr.width;
import static com.app.growbabygrow.R.attr.height;


public class VideoEditActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences sharedpreferences;

    private String MainMergedVideoOutputFilepath;
    private String OriginalVideoOutputFilepath;
    private String TrimmedVideoOutputFilepath1;
    private String TrimmedVideoOutputFilepath2;
    private String TrimmedVideoOutputFilepath3;

    private ImageView main_imageview;
    private ImageView prev1_imageview;
    private ImageView prev2_imageview;
    private ImageView prev3_imageview;
    private TextView main_title;
    private TextView txt_fab;
    private TextView new_title;
    private Button orig_view_btn;
    private FloatingActionButton fab;
    private RadioButton Radio_prev1;
    private RadioButton Radio_prev2;
    private RadioButton Radio_prev3;
    private RadioGroup Radio_Group_prev;

    private Boolean exitapp = false;

    private File MainMergedVideoOutputFile()
    {
        //simple non unique hash
        return new File(MainMergedVideoOutputFilepath);
    }

    private String SelectedTrimmedVideoOutputFilepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_edit);
        context = getApplicationContext();

        main_imageview = (ImageView) findViewById(R.id.imageViewBGMain);
        prev1_imageview = (ImageView) findViewById(R.id.imageViewPrev1);
        prev2_imageview = (ImageView) findViewById(R.id.imageViewPrev2);
        prev3_imageview = (ImageView) findViewById(R.id.imageViewPrev3);
        main_title = (TextView) findViewById(R.id.textViewBGTitle);
        orig_view_btn = (Button) findViewById(R.id.buttonSeeOrig);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        txt_fab = (TextView) findViewById(R.id.textViewFab);
        new_title = (TextView) findViewById(R.id.textViewNewTitle);
        Radio_prev1 = (RadioButton) findViewById(R.id.radioButtonprev1);
        Radio_prev2 = (RadioButton) findViewById(R.id.radioButtonprev2);
        Radio_prev3 = (RadioButton) findViewById(R.id.radioButtonprev3);
        Radio_Group_prev = (RadioGroup) findViewById(R.id.radioGroupPrev);

        fab.setVisibility(View.INVISIBLE);
        txt_fab.setVisibility(View.INVISIBLE);

        sharedpreferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);

        MainMergedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_main_mp4pathname), null);
        OriginalVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_orig_mp4pathname), null);
        TrimmedVideoOutputFilepath1 = sharedpreferences.getString(getString(R.string.p_file1_saved_trim1_mp4pathname), null);
        TrimmedVideoOutputFilepath2 = sharedpreferences.getString(getString(R.string.p_file1_saved_trim2_mp4pathname), null);
        TrimmedVideoOutputFilepath3 = sharedpreferences.getString(getString(R.string.p_file1_saved_trim3_mp4pathname), null);

        Radio_prev1.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               fab.setVisibility(View.VISIBLE);
               txt_fab.setVisibility(View.VISIBLE);
               SelectedTrimmedVideoOutputFilepath = TrimmedVideoOutputFilepath1;
           }
       });

        Radio_prev2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setVisibility(View.VISIBLE);
                txt_fab.setVisibility(View.VISIBLE);
                SelectedTrimmedVideoOutputFilepath = TrimmedVideoOutputFilepath2;
            }
        });

        Radio_prev3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setVisibility(View.VISIBLE);
                txt_fab.setVisibility(View.VISIBLE);
                SelectedTrimmedVideoOutputFilepath = TrimmedVideoOutputFilepath3;
            }
        });

        SetVideoButtons();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exitapp)
                    ExitApp();

                new AlertDialog.Builder(VideoEditActivity.this)
                        .setTitle("Merge Baby Grow?")
                        .setMessage("Are you sure you want use this Video in your Baby Grow?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                if (MainMergedVideoOutputFile().exists()) {

                                    VideoUtils.mergeVideos(MainMergedVideoOutputFilepath, MainMergedVideoOutputFilepath, SelectedTrimmedVideoOutputFilepath);
                                    Toast.makeText(context, "Merging into Baby Grow Completed", Toast.LENGTH_SHORT).show();
                                    HidePreviewsShowGoodbye();
                                }
                                else //first time when no mainfile exists
                                {
                                    main_imageview.setVisibility(View.VISIBLE);
                                    main_title.setVisibility(View.VISIBLE);

                                    File savedir = new File(MainMergedVideoOutputFile().getParent());
                                    String toname = MainMergedVideoOutputFile().getName();
                                    int i = SelectedTrimmedVideoOutputFilepath.lastIndexOf('/');
                                    String fromname =  SelectedTrimmedVideoOutputFilepath.substring(i+ 1, SelectedTrimmedVideoOutputFilepath.length());
                                    Utils.RenameFile(savedir, fromname, toname);
                                    InitVideoButton(MainMergedVideoOutputFilepath, main_imageview);
                                    HidePreviewsShowGoodbye();
                                }

                            }})
                        .setNegativeButton(android.R.string.no, null).show();

            }
        });

    }


    private void HidePreviewsShowGoodbye()
    {
        Radio_Group_prev.setVisibility(View.INVISIBLE);
        orig_view_btn.setVisibility(View.INVISIBLE);
        new_title.setVisibility(View.INVISIBLE);
        prev1_imageview.setVisibility(View.INVISIBLE);
        prev2_imageview.setVisibility(View.INVISIBLE);
        prev3_imageview.setVisibility(View.INVISIBLE);

        String period = sharedpreferences.getString(getString(R.string.p_file1_saved_period), null);
        switch (period) {
            case "Twice Weekly":
                txt_fab.setText("See you in a couple days!");
                break;
            case "Weekly":
                txt_fab.setText("See you next week!");
                break;
            case "Bi-weekly":
                txt_fab.setText("See you in a couple weeks!");
                break;
            case "Monthly":
                txt_fab.setText("See you next month!");
                break;
            default: txt_fab.setText("See you next week!");
                break;

        }

        fab.setImageResource(android.R.drawable.ic_menu_revert);
        exitapp = true;
    }

//    private void ShiftButton(TextView txtfab)
//    {
//        txt_fab.setWidth(200);
//        FrameLayout fl = new FrameLayout(context);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
//        params.setMargins(0,0,32,16);
//        fl.addView(txtfab,params);
//    }

    private void SetVideoButtons()
    {
        if (MainMergedVideoOutputFile().exists()) //will not exist first time around
        {
            InitVideoButton(MainMergedVideoOutputFilepath, main_imageview);
        }
        else
        {
            main_imageview.setVisibility(View.INVISIBLE);
            main_title.setVisibility(View.INVISIBLE);
        }

        InitVideoButton(TrimmedVideoOutputFilepath1, prev1_imageview);
        InitVideoButton(TrimmedVideoOutputFilepath2, prev2_imageview);
        InitVideoButton(TrimmedVideoOutputFilepath3, prev3_imageview);


        orig_view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoEditActivity.this, VideoViewActivity.class);
                intent.putExtra(getString(R.string.player_video_file_path), OriginalVideoOutputFilepath);
                startActivity(intent);
            }
        });

    }

    private void InitVideoButton(final String vidfilepath, ImageView vidbutton )
    {
        Bitmap mainthumb = ThumbnailUtils.createVideoThumbnail(vidfilepath, MediaStore.Video.Thumbnails.MINI_KIND);
        vidbutton.setImageBitmap(mainthumb);
        vidbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoEditActivity.this, VideoViewActivity.class);
                intent.putExtra(getString(R.string.player_video_file_path), vidfilepath);
                startActivity(intent);
            }
        });
    }

    private void ExitApp()
    {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

}
