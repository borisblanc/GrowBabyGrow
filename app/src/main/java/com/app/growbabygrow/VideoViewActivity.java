package com.app.growbabygrow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoViewActivity extends AppCompatActivity {

    VideoView main_video;
    String actcomingfrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        main_video = (VideoView) findViewById(R.id.videoView_main);

        String path = getIntent().getStringExtra(getString(R.string.player_video_file_path));

        actcomingfrom = getIntent().getStringExtra(getString(R.string.ActivityName));

        if (path != null && !path.isEmpty())
        {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(main_video);
            main_video.setMediaController(mediaController);
            main_video.setKeepScreenOn(true);
            main_video.setVideoPath(path);
            main_video.start();
            main_video.requestFocus();
        }
    }


    @Override
    public void onBackPressed(){

        //go back to main if coming from main
        if(actcomingfrom != null && actcomingfrom.equals(MainMenuActivity.TAG)) {
            Intent intent = new Intent(VideoViewActivity.this, MainMenuActivity.class);
            startActivity(intent);
        }
        else {
            super.onBackPressed();
        }

    }
}
