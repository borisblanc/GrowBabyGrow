package com.app.growbabygrow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


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
    private Button orig_view_btn;

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

        sharedpreferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);

        MainMergedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_main_mp4pathname), null);
        OriginalVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_orig_mp4pathname), null);
        TrimmedVideoOutputFilepath1 = sharedpreferences.getString(getString(R.string.p_file1_saved_trim1_mp4pathname), null);
        TrimmedVideoOutputFilepath2 = sharedpreferences.getString(getString(R.string.p_file1_saved_trim2_mp4pathname), null);
        TrimmedVideoOutputFilepath3 = sharedpreferences.getString(getString(R.string.p_file1_saved_trim3_mp4pathname), null);

        SetVideoButtons();


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });




    }

    private void SetVideoButtons()
    {
        File mainfile = new File(MainMergedVideoOutputFilepath);
        if (mainfile.exists()) //will not exist first time around
        {
            Bitmap mainthumb = ThumbnailUtils.createVideoThumbnail(MainMergedVideoOutputFilepath, MediaStore.Video.Thumbnails.MINI_KIND);
            main_imageview.setImageBitmap(mainthumb);
            main_imageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(VideoEditActivity.this, VideoViewActivity.class);
                    intent.putExtra(getString(R.string.player_video_file_path), MainMergedVideoOutputFilepath);
                    startActivity(intent);
                }
            });
        }
        else
        {
            main_imageview.setVisibility(View.INVISIBLE);
            main_title.setVisibility(View.INVISIBLE);
        }

        Bitmap prev1thumb = ThumbnailUtils.createVideoThumbnail(TrimmedVideoOutputFilepath1, MediaStore.Video.Thumbnails.MINI_KIND);
        prev1_imageview.setImageBitmap(prev1thumb);
        prev1_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoEditActivity.this, VideoViewActivity.class);
                intent.putExtra(getString(R.string.player_video_file_path), TrimmedVideoOutputFilepath1);
                startActivity(intent);
            }
        });

        Bitmap prev2thumb = ThumbnailUtils.createVideoThumbnail(TrimmedVideoOutputFilepath2, MediaStore.Video.Thumbnails.MINI_KIND);
        prev2_imageview.setImageBitmap(prev2thumb);
        prev2_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoEditActivity.this, VideoViewActivity.class);
                intent.putExtra(getString(R.string.player_video_file_path), TrimmedVideoOutputFilepath2);
                startActivity(intent);
            }
        });

        Bitmap prev3thumb = ThumbnailUtils.createVideoThumbnail(TrimmedVideoOutputFilepath3, MediaStore.Video.Thumbnails.MINI_KIND);
        prev3_imageview.setImageBitmap(prev3thumb);
        prev3_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoEditActivity.this, VideoViewActivity.class);
                intent.putExtra(getString(R.string.player_video_file_path), TrimmedVideoOutputFilepath3);
                startActivity(intent);
            }
        });

        orig_view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoEditActivity.this, VideoViewActivity.class);
                intent.putExtra(getString(R.string.player_video_file_path), OriginalVideoOutputFilepath);
                startActivity(intent);
            }
        });

    }

}
