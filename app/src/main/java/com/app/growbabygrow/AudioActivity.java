package com.app.growbabygrow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.growbabygrow.Classes.SyncDialogue;
import com.app.growbabygrow.Classes.VideoUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AudioActivity extends AppCompatActivity {

    private Context context;
    private ConstraintLayout constmain;
    private ArrayList<MusicFile> musicfiles;
    private SharedPreferences sharedpreferences;
    private String MainMergedVideoOutputFilepath;
    private File MainMergedVideoOutputFile()
    {
        return new File(MainMergedVideoOutputFilepath);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        context = getApplicationContext();
        constmain = (ConstraintLayout) findViewById(R.id.constraintaudiomain);
        sharedpreferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);
        MainMergedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_main_mp4pathname), null);

        musicfiles = SetGetMusicSelection();
    }

    private ArrayList<MusicFile> SetGetMusicSelection()
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
            mfile._MPController = new MusicPlayerController(mfile._MusicPlayer, mfile._Name, mfile._Artist, mfile._FileId, mfile._Duration);
        }

        return files;
    }



    private class MusicPlayerController
    {
        private int oneTimeOnly = 0;
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
        private RadioButton rdoSelected;

        private int rawMusicId;
        private Boolean isplaying = false;
        private View parentview;
        private MusicPlayerController parentcontroller;
        private String Songname;

        public MusicPlayerController(View view, String songname, String artist, int rawmusicid, String duration)
        {
            parentview = view;
            bFF = (Button) view.findViewById(R.id.btnFF);
            bPause = (Button) view.findViewById(R.id.btnPause);
            bPlay = (Button) view.findViewById(R.id.btnPlay);
            bRW = (Button) view.findViewById(R.id.btnRw);
            txtDuration = (TextView) view.findViewById(R.id.textDuration);
            seekbar = (SeekBar) view.findViewById(R.id.seekBar);
            txtSongname = (TextView) view.findViewById(R.id.txtname);
            txtArtist = (TextView) view.findViewById(R.id.txtartist);
            rdoSelected = (RadioButton) view.findViewById(R.id.rdoSelect);
            rawMusicId = rawmusicid;

            seekbar.setClickable(false);
            bPause.setEnabled(false);
            mediaPlayer = MediaPlayer.create(context, rawMusicId);
            txtSongname.setText(songname);
            txtDuration.setText(duration);
            txtArtist.setText(String.format("by: %s", artist));
            Songname = songname;

            rdoSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View innerview) {
                    DeselectOthers(rawMusicId);
                    Drawable background = parentview.getBackground();
                    ((GradientDrawable)background).setColor(Color.LTGRAY);

                    Boolean performAudioMerge = SyncDialogue.getYesNoWithExecutionStop("Use Audio Baby Grow?", "Are you sure you want merge this Music into your Baby Grow?", AudioActivity.this);

                    if (performAudioMerge)
                    {
                        MergeAudio(rawMusicId, Songname);
                        Toast.makeText(context, "Finished adding Music to your Baby Grow!", Toast.LENGTH_LONG).show();
                    }

                }
            });


            bPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StopAllOtherPlayers(rawMusicId);

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
                }
            });


        }

        private void MergeAudio(int rawmusicid, String songName)
        {
            String baseVideoDir = MainMergedVideoOutputFile().getParent();
            InputStream in = getResources().openRawResource(rawmusicid);
            File audiofile = new File(baseVideoDir, songName + ".m4a");
            if (!audiofile.exists()) //if not copied to disk yet then do it
                VideoUtils.CopyResourcetoDisk(in, audiofile.getAbsolutePath());

//        Intent intent = new Intent(MainMenuActivity.this, VideoViewActivity.class);
//        intent.putExtra(getString(R.string.player_video_file_path), audiopath);
//        intent.putExtra(getString(R.string.ActivityName), TAG);
//        startActivity(intent);


            long videoDuration = VideoUtils.GetMediaDurationMilli(MainMergedVideoOutputFile().getAbsolutePath());

            //trims mp3
            VideoUtils.TrimMedia(audiofile.getAbsolutePath(), new File(baseVideoDir, songName + "_trim.m4a").getAbsolutePath(), 0L, videoDuration, true, false);

            VideoUtils.MuxAudioVideo(MainMergedVideoOutputFile().getAbsolutePath(), MainMergedVideoOutputFile().getAbsolutePath(), new File(baseVideoDir, songName + "_trim.m4a").getAbsolutePath());

        }

        private void StopAllOtherPlayers(int currentmusicid)
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

        private void DeselectOthers(int currentmusicid)
        {
            for (MusicFile mfile: musicfiles)
            {
                if (mfile._MPController.rawMusicId != currentmusicid)
                {
                    mfile._MPController.rdoSelected.setChecked(false);
                    Drawable background = mfile._MPController.parentview.getBackground();
                    ((GradientDrawable)background).setColor(Color.WHITE);
                }
            }
        }

        Runnable UpdateSongTime = new Runnable() {
            public void run() {

                startTime = mediaPlayer.getCurrentPosition();
                long elapsedminutes = TimeUnit.MILLISECONDS.toMinutes((long) startTime);
                long elapsedseconds = TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(elapsedminutes);
                txtDuration.setText(String.format("%d min, %d sec", elapsedminutes, elapsedseconds));
                seekbar.setProgress((int)startTime);

                if (isplaying)
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
