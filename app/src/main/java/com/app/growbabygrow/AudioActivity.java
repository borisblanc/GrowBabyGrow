package com.app.growbabygrow;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AudioActivity extends AppCompatActivity {

    private ConstraintLayout constmain;
    private ArrayList<MusicFile> musicfiles;

    public static int oneTimeOnly = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        constmain = (ConstraintLayout) findViewById(R.id.constraintaudiomain);

        musicfiles = SetGetMusicSelection(this);

    }

    private ArrayList<MusicFile> SetGetMusicSelection(Context context)
    {
        ArrayList<MusicFile> files = new ArrayList<>();

        //manually add for now
        files.add(new MusicFile(R.raw.bensound_sunny, "Sunny", "2m20s", "bensound", R.id.audiochild1));
        files.add(new MusicFile(R.raw.bensound_happiness, "Happiness", "4m21s", "bensound", R.id.audiochild2));
        files.add(new MusicFile(R.raw.bensound_sweet, "Sweet", "5m07s", "bensound", R.id.audiochild3));
        files.add(new MusicFile(R.raw.bensound_tenderness, "Tenderness", "2m03s", "bensound", R.id.audiochild4));
        files.add(new MusicFile(R.raw.bensound_cute, "Cute", "3m14s", "bensound", R.id.audiochild5));

        //audiochild view layouts are hardcoded for now because creating them dynamically and placing them in another layout via code is a bitch
        //todo create them dynamically and place them on layout
        for (MusicFile mfile: files)
        {
            mfile._MusicPlayer = findViewById(mfile._PlayerId);
            mfile._MPController = new MusicPlayerController(mfile._MusicPlayer, context, mfile._Name, mfile._Artist, mfile._FileId, mfile._Duration);
        }

        return files;
    }



    private class MusicPlayerController
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
        private TextView txtArtist;

        private int rawMusicId;
        private Boolean isplaying = false;

        public MusicPlayerController(View view, Context context, String songname, String artist, int rawmusicid, String duration)
        {
            //setContentView(R.layout.content_audio_child);

            bFF = (Button) view.findViewById(R.id.btnFF);
            bPause = (Button) view.findViewById(R.id.btnPause);
            bPlay = (Button) view.findViewById(R.id.btnPlay);
            bRW = (Button) view.findViewById(R.id.btnRw);
            txtDuration = (TextView) view.findViewById(R.id.textDuration);
            seekbar = (SeekBar) view.findViewById(R.id.seekBar);
            txtSongname = (TextView) view.findViewById(R.id.txtname);
            txtArtist = (TextView) view.findViewById(R.id.txtartist);
            rawMusicId = rawmusicid;

            seekbar.setClickable(false);
            bPause.setEnabled(false);
            mediaPlayer = MediaPlayer.create(context, rawMusicId);
            txtSongname.setText(songname);
            txtDuration.setText(duration);
            txtArtist.setText(String.format("by: %s", artist));

            bPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAllOtherPlayers(rawMusicId);

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
                    isplaying = true;
                }


            });

            bPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer.pause();
                    bPause.setEnabled(false);
                    bPlay.setEnabled(true);
                    isplaying = false;
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

        private void stopAllOtherPlayers(int currentmusicid)
        {
            for (MusicFile mfile: musicfiles)
            {
                if (mfile._MPController.rawMusicId != currentmusicid && mfile._MPController.isplaying) {
                    mfile._MPController.mediaPlayer.pause();
                    mfile._MPController.bPause.setEnabled(false);
                    mfile._MPController.bPlay.setEnabled(true);
                    mfile._MPController.isplaying = false;
                }
            }
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

    private class MusicFile
    {
        public String _Name;
        public String _Duration;
        public int _FileId;
        public String _Artist;
        public int _PlayerId;

        public View _MusicPlayer;
        public MusicPlayerController _MPController;


        public MusicFile(int fileId, String name, String duration, String artist, int playerid)
        {
            _Name = name;
            _Duration = duration;
            _FileId = fileId;
            _Artist = artist;
            _PlayerId = playerid;
        }
    }


}
