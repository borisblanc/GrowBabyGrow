package com.app.growbabygrow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.app.growbabygrow.Classes.Bigflake.ExtractDecodeEditEncodeMuxTest;
import com.app.growbabygrow.Classes.Helpers;
import com.app.growbabygrow.Classes.SyncDialogue;
import com.app.growbabygrow.Classes.Utils;
import com.app.growbabygrow.Classes.VideoUtils;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.Gson;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;



public class VideoEditActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences sharedpreferences;
    private String TAG = "VideoEditActivity";
    private String errorfilename = "VideoEditActivityErrors";

    private String MainMergedVideoOutputFilepath;
    private String MainMergedVideoOutputFilepath_with_Audio;
    private Boolean MainMergedVideoOutputFilepath_has_Audio;
    private int MainMergedVideoOutputFilepath_file_id;

    private String OriginalVideoOutputFilepath;
    private String TrimmedVideoOutputFilepath1;
    private Face TrimmedVideo1face;
    private Long TrimmedVideo1facets;
    private String TrimmedVideoOutputFilepath2;
    private Face TrimmedVideo2face;
    private Long TrimmedVideo2facets;
    private String TrimmedVideoOutputFilepath3;
    private Face TrimmedVideo3face;
    private Long TrimmedVideo3facets;
    private String IntroVideoOutputFilePath;
    private String babyName;
    private String period;
    private boolean isnewsession;
    private boolean ispreviewready = false;
    private int fixed_width;
    private int fixed_height;

    private ImageView main_imageview;
    private ImageView prev1_imageview;
    private ImageView prev2_imageview;
    private ImageView prev3_imageview;
    private TextView main_title;
    private TextView txt_fab;
    private TextView new_title;
    private Button orig_view_btn;
    private Button retry_btn;
    private FloatingActionButton fab;
    private RadioButton Radio_prev1;
    private RadioButton Radio_prev2;
    private RadioButton Radio_prev3;
    private RadioGroup Radio_Group_prev;
    private View progressOverlay;

    private Boolean exitapp = false;

    private File MainMergedVideoOutputFile()
    {
        return new File(MainMergedVideoOutputFilepath);
    }

    private String SelectedTrimmedVideoOutputFilepath;
    private Face SelectedTrimmedVideoface;
    private Long SelectedTrimmedVideofacets;
    private String OverlayBitmapFilePath;
    private boolean usingFrontCamera = false;
    private boolean performMerge;

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
        progressOverlay = findViewById(R.id.progress_overlay);
        retry_btn = (Button) findViewById(R.id.buttonretry);
        fab.setVisibility(View.INVISIBLE);


        sharedpreferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);
        Gson gson = new Gson();

        MainMergedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_main_mp4pathname), null);
        MainMergedVideoOutputFilepath_has_Audio = sharedpreferences.getBoolean(getString(R.string.p_file1_saved_main_has_audio), false);
        MainMergedVideoOutputFilepath_with_Audio = sharedpreferences.getString(getString(R.string.p_file1_saved_main_mp4pathname_with_audio), null);
        MainMergedVideoOutputFilepath_file_id = sharedpreferences.getInt(getString(R.string.p_file1_saved_main_audio_file_id), 0);

        OriginalVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_orig_mp4pathname), null);

        TrimmedVideoOutputFilepath1 = sharedpreferences.getString(getString(R.string.p_file1_saved_trim1_mp4pathname), null);
        String json = sharedpreferences.getString(getString(R.string.p_file1_saved_trim1_last_face), "");
        TrimmedVideo1face = gson.fromJson(json, Face.class);
        TrimmedVideo1facets = sharedpreferences.getLong(getString(R.string.p_file1_saved_trim1_last_face_ts), 0);

        TrimmedVideoOutputFilepath2 = sharedpreferences.getString(getString(R.string.p_file1_saved_trim2_mp4pathname), null);
        json = sharedpreferences.getString(getString(R.string.p_file1_saved_trim2_last_face), "");
        TrimmedVideo2face = gson.fromJson(json, Face.class);
        TrimmedVideo2facets = sharedpreferences.getLong(getString(R.string.p_file1_saved_trim2_last_face_ts), 0);

        TrimmedVideoOutputFilepath3 = sharedpreferences.getString(getString(R.string.p_file1_saved_trim3_mp4pathname), null);
        json = sharedpreferences.getString(getString(R.string.p_file1_saved_trim3_last_face), "");
        TrimmedVideo3face = gson.fromJson(json, Face.class);
        TrimmedVideo3facets = sharedpreferences.getLong(getString(R.string.p_file1_saved_trim3_last_face_ts), 0);

        IntroVideoOutputFilePath = sharedpreferences.getString(getString(R.string.p_file1_saved_intro_mp4pathname), null);
        isnewsession = sharedpreferences.getBoolean(getString(R.string.p_file1_is_new), false);
        fixed_width = sharedpreferences.getInt(getString(R.string.p_file1_saved_main_mp4_fixed_width), 0);
        fixed_height = sharedpreferences.getInt(getString(R.string.p_file1_saved_main_mp4_fixed_height), 0);
        OverlayBitmapFilePath = sharedpreferences.getString(getString(R.string.p_file1_saved_selected_last_week_face_bitmap_path), null);
        usingFrontCamera = sharedpreferences.getBoolean(getString(R.string.p_file1_saved_current_session_camera_facing_is_front), false);

        //if new session lets get a head start and begin async intro video creation while user pics preview
        if (isnewsession) {
            CreatePreview(fixed_width, fixed_height);
        }

        Radio_prev1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setVisibility(View.VISIBLE);
                txt_fab.setVisibility(View.VISIBLE);
                SelectedTrimmedVideoOutputFilepath = TrimmedVideoOutputFilepath1;
                SelectedTrimmedVideoface = TrimmedVideo1face;
                SelectedTrimmedVideofacets = TrimmedVideo1facets;
            }
        });

        Radio_prev2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setVisibility(View.VISIBLE);
                txt_fab.setVisibility(View.VISIBLE);
                SelectedTrimmedVideoOutputFilepath = TrimmedVideoOutputFilepath2;
                SelectedTrimmedVideoface = TrimmedVideo2face;
                SelectedTrimmedVideofacets = TrimmedVideo2facets;
            }
        });

        Radio_prev3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setVisibility(View.VISIBLE);
                txt_fab.setVisibility(View.VISIBLE);
                SelectedTrimmedVideoOutputFilepath = TrimmedVideoOutputFilepath3;
                SelectedTrimmedVideoface = TrimmedVideo3face;
                SelectedTrimmedVideofacets = TrimmedVideo3facets;
            }
        });

        retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VideoEditActivity.this, MainMenuActivity.class);
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

        SetVideoButtons();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exitapp) {
                    ExitApp();
                    return;
                }

                if (!isnewsession) {

                    performMerge = SyncDialogue.getYesNoWithExecutionStop("Merge Baby Grow?", "Are you sure you want merge this Video into your Baby Grow?", VideoEditActivity.this);
//                    new AlertDialog.Builder(VideoEditActivity.this)
//                            .setTitle("Merge Baby Grow?")
//                            .setMessage("Are you sure you want merge this Video into your Baby Grow?")
//                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//
//                                public void onClick(DialogInterface dialog, int whichButton) {
//                                    performMerge = true;
//                                    Utils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
//                                }
//                            })
//                            .setNegativeButton(android.R.string.no, null).show();

                    if (!performMerge)
                        return;

                    Toast.makeText(context, "Merging Baby Grow!", Toast.LENGTH_SHORT).show();
                    Utils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200); //not working here!! why??
                    //face image for next session overlay
                    Thread th = new Thread(new Runnable() {
                        public void run() {
                            ExtractEditSaveBitmap(SelectedTrimmedVideoOutputFilepath, OverlayBitmapFilePath, SelectedTrimmedVideofacets);
                            SetNextSessionface(SelectedTrimmedVideoface); //next session face
                        }
                    });
                    th.start();

                    VideoUtils.MuxMergeVideos(getApplicationContext(), new File(MainMergedVideoOutputFilepath), new File(MainMergedVideoOutputFilepath), new File(SelectedTrimmedVideoOutputFilepath));

                    if (MainMergedVideoOutputFilepath_has_Audio) //if audio version exists need to merge it also
                        MergeAudio();


                    Toast.makeText(context, "Merging into Baby Grow Completed", Toast.LENGTH_SHORT).show();
                    HidePreviewsShowGoodbye();
                    Utils.animateView(progressOverlay, View.GONE, 0, 200);
                }
                else //first time when no movie yet rename selected trim to main and add merge in intro movie
                {

                    performMerge = SyncDialogue.getYesNoWithExecutionStop("Create New Baby Grow?", "Are you sure you want to start your Baby Grow with this video?", VideoEditActivity.this);
                    if (!performMerge)
                        return;

                    //need to block UI thread and show loading overlay if preview isn't ready yet and poll until it is
                    if (!ispreviewready) {
                        Toast.makeText(context, "Creating first Baby Grow, this should only take a few seconds!", Toast.LENGTH_LONG).show();
                        orig_view_btn.setEnabled(false);
                        retry_btn.setEnabled(false);
                        fab.setEnabled(false);
                        Utils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
                    }

                    //face image for previous session overlay
                    Thread thbitmap = new Thread(new Runnable() {
                        public void run() {
                            ExtractEditSaveBitmap(SelectedTrimmedVideoOutputFilepath, OverlayBitmapFilePath, SelectedTrimmedVideofacets);
                            SetNextSessionface(SelectedTrimmedVideoface); //next session face
                        }
                    });
                    thbitmap.start();


                    Thread th = new Thread(new Runnable() {
                        public void run() {
                            //block background worker until intro vid is ready
                            while (!ispreviewready) {
                                try {
                                    Thread.sleep(1000);
                                }
                                catch (InterruptedException ex) {
                                    Log.d(TAG, ex.getMessage(), ex);
                                    Helpers.Logger.LogExceptionToFile("VideoEditActivity.line306", Helpers.Logger.ErrorLoggerFilePath(getApplicationContext(), errorfilename), ex);
                                }
                            }
                            //step 2 & 3 inside, need to extract decode (might need to remove edit part) encode and mux using phones codec and then MuxMerge
                            //looper warning here, setting timeout to infinity for it to work but in future will need to create looper thread for
                            File tempintrofile = new File(new File(MainMergedVideoOutputFile().getParent()), "Temp.mp4");

                            ExtractDecodeEditEncodeMuxTest mux = new ExtractDecodeEditEncodeMuxTest();
                            try {

                                mux.ExtractDecodeEditEncodeMux(IntroVideoOutputFilePath, tempintrofile.getAbsolutePath(), fixed_width, fixed_height, SelectedTrimmedVideoOutputFilepath, MainMergedVideoOutputFilepath);
                                VideoUtils.MuxMergeVideos(getApplicationContext(), new File(MainMergedVideoOutputFilepath), new File(tempintrofile.getAbsolutePath()), new File(SelectedTrimmedVideoOutputFilepath));
                                if (tempintrofile.exists())
                                    tempintrofile.delete();

                                runOnUiThread(new Runnable() { //run this shit on UI thread
                                    @Override
                                    public void run() {
                                        //Don't do anything that blocks here or ExtractDecodeEditEncodeMux will never finish
                                        Utils.animateView(progressOverlay, View.GONE, 0, 200); //stop overlay busy animation
                                        main_imageview.setVisibility(View.VISIBLE);
                                        main_title.setVisibility(View.VISIBLE);
                                        InitVideoButton(IntroVideoOutputFilePath, MainMergedVideoOutputFilepath, main_imageview); //make sure to use existing video for thumbnail not newly created MainMergedVideoOutputFilepath or it will not detect it
                                        Toast.makeText(context, "First Baby Grow Created!", Toast.LENGTH_SHORT).show();
                                        HidePreviewsShowGoodbye();
                                    }
                                });

                            }
                            catch (Throwable throwable) {
                                Log.d(TAG, throwable.getMessage(), throwable);
                                Helpers.Logger.LogExceptionToFile("VideoEditActivity.line329", Helpers.Logger.ErrorLoggerFilePath(getApplicationContext(), errorfilename), throwable);
                            }
                        }
                    });
                    th.start();

                    SetNextSessionold(); //make next session not new anymore

                }
            }
        });
    }

    private void MergeAudio()
    {
        if (MainMergedVideoOutputFilepath_file_id == 0) //no selected music then forget it
            return;

        String baseVideoDir = MainMergedVideoOutputFile().getParent();
        InputStream in = getResources().openRawResource(MainMergedVideoOutputFilepath_file_id);
        File audiofile = new File(baseVideoDir, MainMergedVideoOutputFilepath_file_id + ".m4a");
        if (!audiofile.exists()) //if not copied to disk yet then do it
            VideoUtils.CopyResourcetoDisk(in, audiofile.getAbsolutePath(), getApplicationContext());


        long videoDuration = VideoUtils.GetMediaDurationMilli(MainMergedVideoOutputFile().getAbsolutePath());
        //trims mp3
        VideoUtils.TrimMedia(audiofile.getAbsolutePath(), new File(baseVideoDir, MainMergedVideoOutputFilepath_file_id + "_trim.m4a").getAbsolutePath(), 0L, videoDuration, true, false, getApplicationContext());
        //must merge into new mp4 with audio and leave original alone for later merges
        VideoUtils.MuxAudioVideo(MainMergedVideoOutputFilepath_with_Audio, MainMergedVideoOutputFile().getAbsolutePath(), new File(baseVideoDir, MainMergedVideoOutputFilepath_file_id + "_trim.m4a").getAbsolutePath(), getApplicationContext());
    }

    //do this on separate thread
    private void ExtractEditSaveBitmap(String inputvideopath, String Savepath, Long Timestampmilli)
    {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(inputvideopath);
        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(Timestampmilli * 1000); //convert unit to microsecond
        SaveLastFaceImage(bitmap, Savepath);
    }


    private void SaveLastFaceImage(Bitmap origbitmap, String Savepath)
    {
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();


        Bitmap scaledbmap = Utils.scaleBitmap(origbitmap, context.getResources().getInteger(R.integer.overlay_image_scale)); //face detector too slow without scaling
        Frame newframe = new Frame.Builder().setBitmap(scaledbmap).build();

        SparseArray faces = detector.detect(newframe); //todo sometimes no face is detected when face is off screen need to think of how to handle this

        //Utils.testSavebitmap(scaledbmap, Savepath);
        Face face = Utils.GetFirstFace(faces); //this face is only good for cropping image, can't use this for overlay because its not relative to preview dimensions just bitmap dimensions


        try {
            Bitmap cropped = crop(scaledbmap, face);
            scaledbmap.recycle();
            Bitmap newbitmap = Bitmap.createBitmap(cropped.getWidth(), cropped.getHeight(), Bitmap.Config.ARGB_8888);
            fade(cropped, newbitmap);
            cropped.recycle();

            Bitmap finalbitmap = null;

            if (usingFrontCamera) //mirror for front camera only
            {
                finalbitmap = flip(newbitmap);
                newbitmap.recycle();
            }
            else
                finalbitmap = newbitmap;

            Utils.Savebitmap(finalbitmap, Savepath, getApplicationContext());
        }
        catch(Exception ex)
        {
            Log.e(TAG, ex.getMessage(), ex);
            Helpers.Logger.LogExceptionToFile("VideoEditActivity.SaveLastFaceImage", Helpers.Logger.ErrorLoggerFilePath(getApplicationContext(), errorfilename), ex);
        }

        detector.release();
    }


    private Bitmap crop (Bitmap src, Face face)
    {
        Bitmap cropped = null;
        try {
            int actualCropX = (int) face.getPosition().x < 0f ? (int) 0f : (int) face.getPosition().x;
            int actualCropY = (int) face.getPosition().y < 0f ? (int) 0f : (int) face.getPosition().y;
            int facewidth = (int) face.getWidth();
            int faceheight = (int) face.getHeight();

            //todo will need to experiment with different use cases of when face goes off screen for this.
            if (src.getHeight() <= (actualCropY + faceheight)) //this happens when face is too low/high and cut off on bottom/top
                faceheight = src.getHeight() - actualCropY - 1;

            if (src.getWidth() <= (actualCropX + facewidth)) //this happens when face is too off to the side and cut off on left/right
                facewidth = src.getWidth() - actualCropX -1;

            cropped = Bitmap.createBitmap(src, actualCropX , actualCropY, facewidth, faceheight); //crop
        }
        catch(Exception ex)
        {
            Log.e(TAG, ex.getMessage(), ex);
            Helpers.Logger.LogExceptionToFile("VideoEditActivity.crop", Helpers.Logger.ErrorLoggerFilePath(getApplicationContext(), errorfilename), ex);
        }

        //src.recycle();
        return cropped;
    }


    private Bitmap flip (Bitmap src)
    {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }

    private void fade (Bitmap src, Bitmap dst)
    {
        Canvas canvas = new Canvas(dst);
        Paint alphapaint = new Paint();
        alphapaint.setAlpha(80); //transparency
        canvas.drawBitmap(src, 0, 0, alphapaint);
    }


    private void CreatePreview(final int width, final int height)
    {
        //step 1 create intro video on new thread, uses jcodec
        final File Introvid = new File(IntroVideoOutputFilePath);
        if (!Introvid.exists()) {
            Thread th = new Thread(new Runnable() {
                public void run() {
                    CreateIntro_movie(Introvid, width, height);
                    ispreviewready = true; //check this later to see if ready
                }
            });
            th.start();

        }
    }


    private void HidePreviewsShowGoodbye()
    {
        Radio_Group_prev.setVisibility(View.INVISIBLE);
        orig_view_btn.setVisibility(View.INVISIBLE);
        retry_btn.setVisibility(View.INVISIBLE);
        new_title.setVisibility(View.INVISIBLE);
        prev1_imageview.setVisibility(View.INVISIBLE);
        prev2_imageview.setVisibility(View.INVISIBLE);
        prev3_imageview.setVisibility(View.INVISIBLE);
        fab.setEnabled(true);

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

    //set next weeks session
    private void SetNextSessionold()
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (isnewsession)
            editor.putBoolean(getString(R.string.p_file1_is_new), false); //set isnew to false so next time we don't do intro again

        editor.apply();
    }

    private void SetNextSessionface(Face face)
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(face);
        editor.putString(getString(R.string.p_file1_saved_selected_last_week_face), json);
        editor.apply();
    }


    private void SetVideoButtons()
    {
        if (!isnewsession) //will not exist first time around
        {
            if (!MainMergedVideoOutputFilepath_has_Audio)
                InitVideoButton(MainMergedVideoOutputFilepath, MainMergedVideoOutputFilepath, main_imageview);
            else
                InitVideoButton(MainMergedVideoOutputFilepath_with_Audio, MainMergedVideoOutputFilepath_with_Audio, main_imageview);
        }
        else
        {
            main_imageview.setVisibility(View.INVISIBLE);
            main_title.setVisibility(View.INVISIBLE);
        }

        InitVideoButton(TrimmedVideoOutputFilepath1, TrimmedVideoOutputFilepath1, prev1_imageview);
        InitVideoButton(TrimmedVideoOutputFilepath2, TrimmedVideoOutputFilepath2, prev2_imageview);
        InitVideoButton(TrimmedVideoOutputFilepath3, TrimmedVideoOutputFilepath3, prev3_imageview);

    }

    private void InitVideoButton(String vidfilepath_forimage, final String vidfilepath_forplay, ImageView vidbutton)
    {
        Bitmap mainthumb = ThumbnailUtils.createVideoThumbnail(vidfilepath_forimage, MediaStore.Video.Thumbnails.MINI_KIND);
        vidbutton.setImageBitmap(mainthumb);

        vidbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoEditActivity.this, VideoViewActivity.class);
                intent.putExtra(getString(R.string.player_video_file_path), vidfilepath_forplay);
                startActivity(intent);
            }
        });
    }


    private void CreateIntro_movie(File vidfilepath, int width, int height)
    {
        try {
            ArrayList<Bitmap> b = new ArrayList<>();

            String baby_name = sharedpreferences.getString(getString(R.string.p_file1_saved_name), null);
            String period = sharedpreferences.getString(getString(R.string.p_file1_saved_period), null);
            String intro1 = baby_name + "'s" + " Baby Grow";

            //draw bitmaps from resource
            b.add(VideoUtils.drawTextToBitmap(context, R.drawable.black_canvas, intro1, width, height, 102));
            b.add(VideoUtils.drawTextToBitmap(context, R.drawable.black_canvas, GetIntroPeriod(period), width, height, 102));


            VideoUtils.CreatevideoFromBitmaps(vidfilepath, b, 30, getApplicationContext());
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage(), ex);
            Helpers.Logger.LogExceptionToFile("VideoEditActivity.CreateIntro_movie", Helpers.Logger.ErrorLoggerFilePath(getApplicationContext(), errorfilename), ex);
        }
    }

    private String GetIntroPeriod(String period)
    {
        switch (period)
        {
            case "Twice Weekly":
                return "Every Few Days...";
            case "Weekly":
                return "Week by Week...";
            case "Bi-weekly":
                return "Every Couple Weeks...";
            case "Monthly":
                return "Month by Month...";
            default:
                return "Week by Week...";
        }

    }

    private void ExitApp()
    {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(VideoEditActivity.this, MainMenuActivity.class);
        startActivity(intent);
    }

}
