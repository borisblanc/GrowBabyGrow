package com.app.growbabygrow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoViewActivity extends AppCompatActivity {

    VideoView main_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        main_video = (VideoView) findViewById(R.id.videoView_main);

        String path = getIntent().getStringExtra(getString(R.string.player_video_file_path));

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
}
