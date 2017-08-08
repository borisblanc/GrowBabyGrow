package com.app.growbabygrow;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
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

import android.support.v7.app.AlertDialog;
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
import com.app.growbabygrow.Classes.Utils;
import com.app.growbabygrow.Classes.VideoUtils;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.Gson;

import java.io.File;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static android.R.attr.bitmap;
import static android.R.attr.width;
import static android.R.transition.fade;
import static com.app.growbabygrow.Classes.Utils.GetFirstFace;
import static com.app.growbabygrow.Classes.Utils.scaleBitmap;


public class VideoEditActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences sharedpreferences;
    private String TAG = "VideoEditActivity";

    private String MainMergedVideoOutputFilepath;
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
        progressOverlay  = findViewById(R.id.progress_overlay);

        fab.setVisibility(View.INVISIBLE);
        txt_fab.setVisibility(View.INVISIBLE);

        sharedpreferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);
        Gson gson = new Gson();

        MainMergedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_main_mp4pathname), null);
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

        SetVideoButtons();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exitapp)
                    ExitApp();

                if (!isnewsession) {
                    new AlertDialog.Builder(VideoEditActivity.this)
                            .setTitle("Merge Baby Grow?")
                            .setMessage("Are you sure you want merge this Video into your Baby Grow?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //this merge works fine for 'similar' vids
                                    //VideoUtils.Mp4ParserMergeVideos(MainMergedVideoOutputFilepath, MainMergedVideoOutputFilepath, SelectedTrimmedVideoOutputFilepath);

                                    //face image for previous session overlay
                                    Thread th = new Thread(new Runnable() {
                                        public void run() {
                                            ExtractEditSaveBitmap(SelectedTrimmedVideoOutputFilepath, OverlayBitmapFilePath, SelectedTrimmedVideofacets);
                                            SetNextSessionface(SelectedTrimmedVideoface); //next session face
                                        }
                                    });
                                    th.start();

                                    VideoUtils.MuxMergeVideos(new File(MainMergedVideoOutputFilepath), new File(MainMergedVideoOutputFilepath), new File(SelectedTrimmedVideoOutputFilepath));

                                    Toast.makeText(context, "Merging into Baby Grow Completed", Toast.LENGTH_SHORT).show();
                                    HidePreviewsShowGoodbye();

                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();



                }
                else //first time when no movie yet rename selected trim to main and add merge in intro movie
                {
                    //need to block UI thread and show loading overlay if preview isn't ready yet and poll until it is
                    if (!ispreviewready) {
                        Toast.makeText(context, "Creating first Baby Grow, this should only take a few seconds!", Toast.LENGTH_SHORT).show();
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
                                catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            //step 2 & 3 inside, need to extract decode (might need to remove edit part) encode and mux using phones codec and then MuxMerge
                            //looper warning here, setting timeout to infinity for it to work but in future will need to create looper thread for
                            //todo clean this file up after its used
                            File tempintrofile = new File(new File(MainMergedVideoOutputFile().getParent()), "Temp.mp4");

                            ExtractDecodeEditEncodeMuxTest test = new ExtractDecodeEditEncodeMuxTest();
                            try {
                                test.ExtractDecodeEditEncodeMux(IntroVideoOutputFilePath, tempintrofile.getAbsolutePath(), fixed_width, fixed_height, SelectedTrimmedVideoOutputFilepath, MainMergedVideoOutputFilepath);

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

                            } catch (Throwable throwable) {
                                Log.d(TAG, throwable.getMessage(), throwable);
                            }
                        }
                    });
                    th.start();

                    SetNextSessionold(); //make next session not new anymore

                }
            }
        });

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
                .setTrackingEnabled(true) //tracking enables false makes it much slow wtf?!?!
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setProminentFaceOnly(true) // no diff
                //.setLandmarkType(FaceDetector.NO_LANDMARKS) this will make it detect nothing
                //.SetMinFaceSize((float)0.1) //small performance gain when removed
                .setMode(FaceDetector.FAST_MODE) // tiny small performance gain
                .build();


        Bitmap scaledbmap = Utils.scaleBitmap(origbitmap, context.getResources().getInteger(R.integer.overlay_image_scale)); //face detector too slow without scaling
        Frame newframe = new Frame.Builder().setBitmap(scaledbmap).build();

        SparseArray faces = detector.detect(newframe); //takes longest

        Face face = Utils.GetFirstFace(faces); //this face is only good for cropping image, can't use this for overlay because its not relative to preview dimensions just bitmap dimensions


        try {
            Bitmap cropped = crop(scaledbmap, face);
            //Bitmap Greyfacebitmap = Utils.toGrayscale(faceBitmap); //grey scale
            Bitmap newbitmap = Bitmap.createBitmap(cropped.getWidth(), cropped.getHeight(), Bitmap.Config.ARGB_8888);
            fade(cropped, newbitmap);
            Bitmap mirrored = flip(newbitmap);
            Utils.testSavebitmap(mirrored, Savepath);
        }
        catch(Exception e) //this is wonky right now
        {
            e.printStackTrace();
        }

        detector.release();
    }




    private Bitmap crop (Bitmap src, Face face)
    {
        Bitmap cropped = null;
        try {
            int actualCropX = (int) face.getPosition().x < 0f ? (int) 0f : (int) face.getPosition().x;
            int actualCropY = (int) face.getPosition().y < 0f ? (int) 0f : (int) face.getPosition().y;

            //todo will need to experiment with different use cases of when face goes off screen for this.
            if (src.getHeight() <= (actualCropY + face.getHeight())) //this happens when face is too low and cut off on bottom
                actualCropY = (int) 0f;

            cropped = Bitmap.createBitmap(src, actualCropX , actualCropY, (int) face.getWidth(), (int) face.getHeight()); //crop


        }
        catch(Exception ex)
        {
            ex.printStackTrace();
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
        //src.recycle();
        return dst;
    }

    private void fade (Bitmap src, Bitmap dst)
    {
        Canvas canvas = new Canvas(dst);
        Paint alphapaint = new Paint();
        alphapaint.setAlpha(80); //transparency
        canvas.drawBitmap(src, 0, 0, alphapaint);
        src.recycle();
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

            //this will stop it from being async as it will block main thread until its done
//            try {
//                th.join(); //wait for it to finish
//            } catch (InterruptedException e) {
//                Log.d(TAG, e.getMessage(), e);
//            }

        }
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
            InitVideoButton(MainMergedVideoOutputFilepath, MainMergedVideoOutputFilepath, main_imageview);
        }
        else
        {
            main_imageview.setVisibility(View.INVISIBLE);
            main_title.setVisibility(View.INVISIBLE);
        }

        InitVideoButton(TrimmedVideoOutputFilepath1, TrimmedVideoOutputFilepath1, prev1_imageview);
        InitVideoButton(TrimmedVideoOutputFilepath2, TrimmedVideoOutputFilepath2, prev2_imageview);
        InitVideoButton(TrimmedVideoOutputFilepath3, TrimmedVideoOutputFilepath3, prev3_imageview);


        orig_view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VideoEditActivity.this, VideoViewActivity.class);
                intent.putExtra(getString(R.string.player_video_file_path), OriginalVideoOutputFilepath);
                startActivity(intent);
            }
        });

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
//            Utils.testSavebitmap(b.get(0), new File(MainMergedVideoOutputFile().getParent(),"ass.bmp").getAbsolutePath());
//            Utils.testSavebitmap(b.get(1), new File(MainMergedVideoOutputFile().getParent(),"ass2.bmp").getAbsolutePath());

            VideoUtils.CreatevideoFromBitmaps(vidfilepath, b, 30);
        }
        catch (Exception e)
        {
            Log.d(TAG,e.getMessage(),e);
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
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

}
