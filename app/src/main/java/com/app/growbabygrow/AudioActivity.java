package com.app.growbabygrow;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


import java.util.concurrent.TimeUnit;

public class AudioActivity extends AppCompatActivity {

    private ConstraintLayout constmain;
    private View musicplayer1;
    private MusicPlayer musicplayer1class;
    private View musicplayer2;
    private MusicPlayer musicplayer2class;

    public static int oneTimeOnly = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        constmain = (ConstraintLayout) findViewById(R.id.constraintaudiomain);

        musicplayer1 = findViewById(R.id.audiochild1);
        musicplayer1class = new MusicPlayer(musicplayer1, this, "Sunny", "bensound", R.raw.bensound_sunny, "5:00");

        musicplayer2 = findViewById(R.id.audiochild2);
        musicplayer2class = new MusicPlayer(musicplayer2, this, "Fart", "bensound", R.raw.bensound_sunny, "5:00");




    }



    private class MusicPlayer
    {
        private Button bFF, bPause, bPlay, bRW;
        private MediaPlayer mediaPlayer;

        private double startTime = 0;
        private double finalTime = 0;

        private Handler myHandler = new Handler();
        private int forwardTime = 5000;
        private int backwardTime = 5000;
        private SeekBar seekbar;
        private TextView txtDuration;
        private TextView txtSongname;

        public MusicPlayer(View view, Context context, String songname, String artist, int rawmusicid, String duration)
        {
            //setContentView(R.layout.content_audio_child);

            bFF = (Button) view.findViewById(R.id.btnFF);
            bPause = (Button) view.findViewById(R.id.btnPause);
            bPlay = (Button) view.findViewById(R.id.btnPlay);
            bRW = (Button) view.findViewById(R.id.btnRw);
            txtDuration = (TextView) view.findViewById(R.id.textDuration);
            seekbar = (SeekBar) view.findViewById(R.id.seekBar);
            txtSongname = (TextView) view.findViewById(R.id.txtname);

            seekbar.setClickable(false);
            bPause.setEnabled(false);
            mediaPlayer = MediaPlayer.create(context, R.raw.bensound_sunny);
            txtSongname.setText(songname);

            bPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mediaPlayer.start();

                    finalTime = mediaPlayer.getDuration();
                    startTime = mediaPlayer.getCurrentPosition();

                    if (oneTimeOnly == 0) {
                        seekbar.setMax((int) finalTime);
                        oneTimeOnly = 1;
                    }

//                txtDuration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
//                            TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
//                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));

//                txtDuration.setText(String.format("%d min, %d sec",
//                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
//                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
//                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));

                    seekbar.setProgress((int)startTime);
                    myHandler.postDelayed(UpdateSongTime,100);
                    bPause.setEnabled(true);
                    bPlay.setEnabled(false);
                }


            });

            bPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer.pause();
                    bPause.setEnabled(false);
                    bPlay.setEnabled(true);
                }
            });

            bFF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int temp = (int)startTime;

                    if((temp+forwardTime)<=finalTime){
                        startTime = startTime + forwardTime;
                        mediaPlayer.seekTo((int) startTime);
                    }
//                else{
//                    Toast.makeText(getApplicationContext(),"Cannot jump forward 5 seconds",Toast.LENGTH_SHORT).show();
//                }
                }
            });

            bRW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int temp = (int)startTime;

                    if((temp-backwardTime)>0){
                        startTime = startTime - backwardTime;
                        mediaPlayer.seekTo((int) startTime);
                    }
//                else{
//                    Toast.makeText(getApplicationContext(),"Cannot jump backward 5 seconds",Toast.LENGTH_SHORT).show();
//                }
                }
            });


        }

        Runnable UpdateSongTime = new Runnable() {
            public void run() {
                startTime = mediaPlayer.getCurrentPosition();
                txtDuration.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));
                seekbar.setProgress((int)startTime);
                myHandler.postDelayed(this, 100);
            }
        };

    }


}
