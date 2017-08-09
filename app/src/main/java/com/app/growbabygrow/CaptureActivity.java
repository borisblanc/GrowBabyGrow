package com.app.growbabygrow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.growbabygrow.Classes.Camera2Source;
import com.app.growbabygrow.Classes.CameraSource;
import com.app.growbabygrow.Classes.CameraSourcePreview;
import com.app.growbabygrow.Classes.CustomFaceDetector;
import com.app.growbabygrow.Classes.FaceGraphic;
import com.app.growbabygrow.Classes.GraphicOverlay;
import com.app.growbabygrow.Classes.Helpers;
import com.app.growbabygrow.Classes.Utils;
import com.app.growbabygrow.Classes.VideoUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CaptureActivity extends AppCompatActivity {
    private static final String TAG = "CaptureActivity";

    private Context context;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 201;
    private TextView cameraVersion;
    private ImageView ivAutoFocus;

    // CAMERA VERSION ONE DECLARATIONS
    private CameraSource mCameraSource = null;

    // CAMERA VERSION TWO DECLARATIONS
    private Camera2Source mCamera2Source = null;

    // COMMON TO BOTH CAMERAS
    private CameraSourcePreview mPreview;
    private FaceDetector previewFaceDetector = null;
    private GraphicOverlay mGraphicOverlay;
    private FaceGraphic mFaceGraphic;
    private boolean wasActivityResumed = false;

    // DEFAULT CAMERA BEING OPENED
    private boolean usingFrontCamera = true;

    // MUST BE CAREFUL USING THIS VARIABLE.
    // ANY ATTEMPT TO START CAMERA2 ON API < 21 WILL CRASH.
    private boolean useCamera2 = false;
    private boolean trackRecord = false; //determines if regular preview is opened or we start track record also
    public Button recButton;
    private int _fps = 30;
    private int _vidlengthseconds = 3;
    private SharedPreferences sharedpreferences;
    private FaceSession fs_current;
    private Face lastsessionface;
    private boolean isnewsession;
    public Button toggleoverlayButton;
    private Boolean isoverlyshown = false;
    private String OverlayBitmapFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); lock this in manifest instead
            setContentView(R.layout.captureactivity_main);
            context = getApplicationContext();
            recButton = (Button) findViewById(R.id.btn_record);
            Button switchButton = (Button) findViewById(R.id.btn_switch);
            mPreview = (CameraSourcePreview) findViewById(R.id.preview);
            mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
            ivAutoFocus = (ImageView) findViewById(R.id.ivAutoFocus);
            toggleoverlayButton = (Button) findViewById(R.id.btn_toggle_overlay);

            sharedpreferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);

            fs_current = new FaceSession();
            fs_current.MainMergedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_main_mp4pathname), null);
            fs_current.OriginalVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_orig_mp4pathname), null);

            FaceSessionProperty fsp1 = fs_current.GetNewProp();
            fsp1._trimmedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_trim1_mp4pathname), null);
            fsp1._lastfacekey = getString(R.string.p_file1_saved_trim1_last_face);
            fsp1._lastfacetskey = getString(R.string.p_file1_saved_trim1_last_face_ts);

            FaceSessionProperty fsp2 = fs_current.GetNewProp();
            fsp2._trimmedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_trim2_mp4pathname), null);
            fsp2._lastfacekey = getString(R.string.p_file1_saved_trim2_last_face);
            fsp2._lastfacetskey = getString(R.string.p_file1_saved_trim2_last_face_ts);

            FaceSessionProperty fsp3 = fs_current.GetNewProp();
            fsp3._trimmedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_trim3_mp4pathname), null);
            fsp3._lastfacekey = getString(R.string.p_file1_saved_trim3_last_face);
            fsp3._lastfacetskey = getString(R.string.p_file1_saved_trim3_last_face_ts);

            isnewsession = sharedpreferences.getBoolean(getString(R.string.p_file1_is_new), false);
            OverlayBitmapFilePath = sharedpreferences.getString(getString(R.string.p_file1_saved_selected_last_week_face_bitmap_path), null);

            if (!isnewsession) {//if not new session try to track face from last session
                Gson gson = new Gson();
                String json = sharedpreferences.getString(getString(R.string.p_file1_saved_selected_last_week_face), "");
                lastsessionface = gson.fromJson(json, Face.class);
            }



            if (checkGooglePlayAvailability()) {

                requestPermissionThenOpenCamera();

                switchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (usingFrontCamera) {
                            stopCameraSource();
                            createCameraSource(Camera2Source.CAMERA_FACING_BACK);
                            usingFrontCamera = false;
                        }
                        else {
                            stopCameraSource();
                            createCameraSource(Camera2Source.CAMERA_FACING_FRONT);
                            usingFrontCamera = true;
                        }
                    }
                });

                toggleoverlayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isoverlyshown) {

                            isoverlyshown = false;
                            mFaceGraphic.showoverlay = false;
                        }
                        else {
                            isoverlyshown = true;
                            mFaceGraphic.showoverlay = true;
                        }
                    }
                });


                recButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCamera2Source.mTrackRecord) {

                            stopCameraSource(); //call this to release everything or all the shit breaks
                            trackRecord = false;
                            recButton.setText(R.string.record);
                            requestPermissionThenOpenCamera(); //back to preview mode

                            //this will have to stay constant throughout Grow baby session, if back and front don't support same resolutions will have to show user error or lock down camera facing
                            VerifySaveLockedDownVidSize(mCamera2Source.mVideoSize);
                            SaveFacing(usingFrontCamera);
                            try {

                                RunFaceProcessing(fs_current);

                                //trim all preview videos
                                //Toast.makeText(context, "Starting Trim Video", Toast.LENGTH_SHORT).show();
                                for (FaceSessionProperty fsp : fs_current._props) {
                                    CreateTrimmedVideo(fsp._previewbestfacedata, fs_current.OriginalVideoOutputFilepath, fsp._trimmedVideoOutputFilepath);
                                    FindSaveLastFace(fs_current._faces, fsp);
                                }

                                Toast.makeText(context, "Smiles Captured & Created!", Toast.LENGTH_SHORT).show();
                                //move control to edit activity
                                TransfertoVideoEdit();
                            }
                            catch(IllegalArgumentException ex)
                            {
                                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                                fs_current._faces.clear(); //remove all faces and try again
                            }
                        }
                        else {
                            stopCameraSource(); //call this to release everything or all the shit breaks
                            trackRecord = true;
                            recButton.setText(R.string.stop);
                            requestPermissionThenOpenCamera();
                        }
                    }
                });


            }
        } catch (Throwable e) {
            Log.d(TAG,e.getMessage());
        }

    }

    private void SaveFacing(Boolean isfacingfront)
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(getString(R.string.p_file1_saved_current_session_camera_facing), isfacingfront ? "Front" : "Back" ); //will overwrite previous value each time
        editor.apply();
    }

    //function will check to see if new session then save resolution if not new then check to make sure resolution is same else throw exception.
    private void VerifySaveLockedDownVidSize(Size recordedsize)
    {
        boolean isnew = sharedpreferences.getBoolean(getString(R.string.p_file1_is_new), false);

        if (isnew)
        {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(getString(R.string.p_file1_saved_main_mp4_fixed_width), recordedsize.getWidth());
            editor.putInt(getString(R.string.p_file1_saved_main_mp4_fixed_height), recordedsize.getHeight());
            editor.apply();
        }
        else
        {
            int width = sharedpreferences.getInt(getString(R.string.p_file1_saved_main_mp4_fixed_width), 0);
            int height = sharedpreferences.getInt(getString(R.string.p_file1_saved_main_mp4_fixed_height), 0);

            if (width != recordedsize.getWidth() || height != recordedsize.getHeight())
                throw new IllegalStateException("Resolution of video for saved project has changed! This is a problem!");
        }
    }

    private void TransfertoVideoEdit()
    {
        Intent intent = new Intent(CaptureActivity.this, VideoEditActivity.class);
        startActivity(intent);
    }

    private void RunFaceProcessing(FaceSession FS)
    {
        FaceSessionProperty FirstFSP = new FaceSessionProperty();
        ArrayList<Helpers.FaceData> previous_exceptions = new ArrayList<>(); //for holding previous exceptions to choose best face for subsequent previews
        Helpers.FaceData previous_bestface = new Helpers.FaceData(Long.valueOf(0), 0);

        if ((GetCoreFrames() * FS._props.size()) > FS._faces.size()) //if frames captured is less than coreframes * number of previews then don't bother, need more try again!
            throw new IllegalArgumentException("Not enough frames supplied. Try Recording Again!!");

        for(FaceSessionProperty fsp : FS._props)
        {

            if (FS._props.indexOf(fsp) == 0 ) //first facesession process like this and save for others to process off
            {
                Processfaces(FS._faces, fsp); //preview 1 processing only but gets data for subsequent previews
                FirstFSP = fsp;
                previous_bestface = FirstFSP._previewbestface;
            }
            else //preview processing based on previous preview
            {
                fsp._previewfinalscores = ArrayMinusException(FirstFSP._previewfinalscores, previous_bestface); //_previewfinalscores for previews other than FirstFSP are just exceptions, best face must be from previous

                previous_exceptions.addAll(fsp._previewfinalscores); //running total of all exceptions

                fsp._previewbestface = BestFace(FirstFSP._previewfinalscores, previous_exceptions); //needs original list and exceptions to determine best face
                fsp._previewbestfacedata = BestFacedata(FirstFSP._previewfinalscores, fsp._previewbestface);
                previous_bestface = fsp._previewbestface;
            }
        }

    }



    private void CreateTrimmedVideo(Helpers.Tuple<Long,Long> bestfacetimestamps, String OriginalVideoOutputFilepath, String TrimmedVideoOutputFilepath)
    {
        if (bestfacetimestamps == null)
            return;

        VideoUtils.TrimVideo(OriginalVideoOutputFilepath, TrimmedVideoOutputFilepath, bestfacetimestamps.firstTimeStamp, bestfacetimestamps.lastTimeStamp, false, true);
    }

    //process all of first preview (data & results) and subsequent data only for all other previews
    private void Processfaces(ArrayList<Helpers.FrameData> _faces, FaceSessionProperty P)
    {
        if (_faces == null || _faces.size() < GetFrameTotal()) {
            throw new IllegalArgumentException("Not enough frames supplied. Try Recording Again!!");
        }

        int coreframeslength = GetCoreFrames(); //core sample of frames will be two seconds of video might in future vary depending on user settings
        int computelimit = _faces.size() - GetFrameTotal(); //this will keep walking average calcs only happening within range

        for(Helpers.FrameData facedata : _faces )
        {
            Long currenttimestamp = facedata._timeStamp;
            if (_faces.indexOf(facedata) < computelimit) //makes sense to compute because we will can use it
            {
                List<Helpers.FrameData> coreframes = _faces.subList(_faces.indexOf(facedata), _faces.indexOf(facedata) + coreframeslength);
                ArrayList<Helpers.FaceData> corescores = new ArrayList<>();
                for (Helpers.FrameData face : coreframes)
                {
                    corescores.add(new Helpers.FaceData(face._timeStamp, Utils.GetImageUsability(Utils.GetFirstFace(face._faces), face._frameWidth, face._frameHeight))); //todo improve this by moving all the face finding and scoring outside so its done only once per frame
                }

                double avg = Utils.calculateAverage(corescores);
                double stDev = Utils.stDev(corescores);

                P._previewfinalscores.add(new Helpers.FaceData(currenttimestamp, avg < stDev ? 0 :  avg  - stDev)); //avg - std dev should give those with best avg score and lowest deviation /no negatives
            }
            else //can't use computations past this point so just need timestamps
            {
                P._previewfinalscores.add(new Helpers.FaceData(currenttimestamp, 0 ));
            }
        }

        P._previewbestface = Utils.getMaxFace(P._previewfinalscores);

        Helpers.FaceData bestLastface = P._previewfinalscores.get(P._previewfinalscores.indexOf(P._previewbestface) + GetFrameTotal());

        P._previewbestfacedata = new Helpers.Tuple<>(P._previewbestface._timeStamp, bestLastface._timeStamp);
    }

    //return list of exceptions that will be used to make sure these are not chosen in other previews
    private ArrayList<Helpers.FaceData> ArrayMinusException(ArrayList<Helpers.FaceData> finalscores, Helpers.FaceData _faceexception)
    {
        int offset = GetCoreFrames(); //frame offset for each additional previews so we get something different, make it equal to fps * 2 which is two seconds of video
        ArrayList<Helpers.FaceData> final_scores_allexceptions = new ArrayList<> (finalscores);

        int exceptionindex = finalscores.indexOf(_faceexception);

        int margin = (exceptionindex - (offset/2));
        if (margin < 0)
            return new ArrayList<>(final_scores_allexceptions.subList(exceptionindex - ((offset/2) + margin) , exceptionindex + ((offset/2) - margin))); //negative margin add in to shirt over so we don't get out of bounds
        else
            return new ArrayList<>(final_scores_allexceptions.subList(exceptionindex - (offset/2), exceptionindex + (offset/2)));
    }

    private Helpers.FaceData BestFace(ArrayList<Helpers.FaceData> finalscores, ArrayList<Helpers.FaceData> exceptions)
    {
        if (exceptions.size() == 0)
            return Utils.getMaxFace(finalscores);
        else
            return Utils.getMaxFace(finalscores, exceptions);
    }

    private Helpers.Tuple<Long,Long> BestFacedata(ArrayList<Helpers.FaceData> finalscores, Helpers.FaceData bestface)
    {
        Helpers.FaceData bestLastface = finalscores.get(finalscores.indexOf(bestface) + GetFrameTotal());
        return new Helpers.Tuple<>(bestface._timeStamp, bestLastface._timeStamp);
    }

    private void FindSaveLastFace(ArrayList<Helpers.FrameData> _allFaces, FaceSessionProperty fsp)
    {
        Helpers.FrameData FirstFrame = null;
        Helpers.FrameData LastFrame = null;

        for(Helpers.FrameData frame : _allFaces)
        {

            if (frame._timeStamp == fsp._previewbestfacedata.firstTimeStamp)
                FirstFrame = frame;

            if (frame._timeStamp == fsp._previewbestfacedata.lastTimeStamp)
                LastFrame = frame;

        }

        int firstframeindex = _allFaces.indexOf(FirstFrame);
        int lastframeindex = _allFaces.indexOf(LastFrame);


        Face LastFace = null;
        Long LastFacets = null;
        for (int i = lastframeindex; i > firstframeindex; i--) //walk backwards from last timestamp and find last face then break
        {
            LastFace = Utils.GetFirstFace(_allFaces.get(i)._faces);
            LastFacets = _allFaces.get(i)._timeStamp;
            if (LastFace != null)
                break;
        }

        SharedPreferences.Editor editor = sharedpreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(LastFace);
        editor.putString(fsp._lastfacekey, json); //must use these faces for overlay placement because they have correct preview dimensions
        editor.putLong(fsp._lastfacetskey, LastFacets);
        editor.apply();
    }


    private int GetFrameTotal()
    {
        return _fps * _vidlengthseconds; //total frames will always be frames per second * number of seconds
    }

    private int GetCoreFrames() //used for frames processing and exceptions processing
    {
        return _fps * 2;
    }

    private boolean checkGooglePlayAvailability() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if(resultCode == ConnectionResult.SUCCESS) {
            return true;
        } else {
            if(googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(CaptureActivity.this, resultCode, 2404).show();
            }
        }
        return false;
    }

    private void requestPermissionThenOpenCamera() {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                useCamera2 = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
                createCameraSource(usingFrontCamera ? Camera2Source.CAMERA_FACING_FRONT : Camera2Source.CAMERA_FACING_BACK);
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void createCameraSource(int facing)
    {
        if (trackRecord) {
            previewFaceDetector = new FaceDetector.Builder(context)
                    .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .setMode(FaceDetector.ACCURATE_MODE) //need accurate mode for Euler Y
                    .setProminentFaceOnly(true)
                    .setTrackingEnabled(true)
                    .build();

            CustomFaceDetector faceDetector = new CustomFaceDetector(previewFaceDetector, fs_current._faces);

            if (previewFaceDetector.isOperational()) {
                //previewFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
                faceDetector.setProcessor(new LargestFaceFocusingProcessor.Builder(previewFaceDetector, new GraphicFaceTracker(mGraphicOverlay)).build());

            }
            else {
                Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
            }

            if (useCamera2) {
                mCamera2Source = new Camera2Source.Builder(context, faceDetector, fs_current.OriginalVideoOutputFilepath)
                        .setFocusMode(Camera2Source.CAMERA_AF_AUTO)
                        .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                        .setFacing(facing) //Camera2Source.CAMERA_FACING_FRONT = 1 or CAMERA_FACING_BACK = 0
                        .build();

                //IF CAMERA2 HARDWARE LEVEL IS LEGACY, CAMERA2 IS NOT NATIVE.
                //WE WILL USE CAMERA1.
                if (mCamera2Source.isCamera2Native()) {
                    startCameraSource();
                } else {
                    useCamera2 = false;
                    createCameraSource(usingFrontCamera ? Camera2Source.CAMERA_FACING_FRONT : Camera2Source.CAMERA_FACING_BACK);
                }
            } else {
                mCameraSource = new CameraSource.Builder(context, faceDetector)
                        .setFacing(facing)
                        .setRequestedFps(30.0f)
                        .build();

                startCameraSource();
            }
        }
        else //preview only camera 2 only for now
        {
            mCamera2Source = new Camera2Source.Builder(context)
                    .setFacing(facing)
                    .build();
            startCameraSource();
        }
    }



    private void startCameraSource() {
        if(useCamera2) {
            if(mCamera2Source != null) {
                try {mPreview.start(mCamera2Source, mGraphicOverlay, trackRecord);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source 2.", e);
                    mCamera2Source.release();
                    mCamera2Source = null;
                }
            }
        } else {
            if (mCameraSource != null) {
                try {mPreview.start(mCameraSource, mGraphicOverlay);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source.", e);
                    mCameraSource.release();
                    mCameraSource = null;
                }
            }
        }
    }

    private void stopCameraSource() {
        mPreview.stop();
    }

//    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
//        @Override
//        public Tracker<Face> create(Face face) {
//            return new GraphicFaceTracker(mGraphicOverlay);
//        }
//    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, context, lastsessionface, OverlayBitmapFilePath);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mFaceGraphic.goneFace();
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mFaceGraphic.goneFace();
            mOverlay.remove(mFaceGraphic);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera();
            } else {
                Toast.makeText(CaptureActivity.this, "CAMERA PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        if(requestCode == REQUEST_STORAGE_PERMISSION) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera();
            } else {
                Toast.makeText(CaptureActivity.this, "STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(wasActivityResumed)
            //If the CAMERA2 is paused then resumed, it won't start again unless creating the whole camera again.
            if(useCamera2)
            {
                createCameraSource(usingFrontCamera ? Camera2Source.CAMERA_FACING_FRONT : Camera2Source.CAMERA_FACING_BACK);
            } else {
                startCameraSource();
            }
    }

    @Override
    protected void onPause() {
        super.onPause();
        wasActivityResumed = true;
        stopCameraSource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCameraSource();
        if(previewFaceDetector != null) {
            previewFaceDetector.release();
        }
    }

    @Override
    public void onBackPressed(){
        if (trackRecord)
            Toast.makeText(context, "Please press stop on record First!", Toast.LENGTH_SHORT).show();
        else
            super.onBackPressed();

    }


    private class FaceSession
    {
        private ArrayList<Helpers.FrameData> _faces = new ArrayList<>();
        private ArrayList<FaceSessionProperty> _props;
        private String MainMergedVideoOutputFilepath;
        private String OriginalVideoOutputFilepath;

        private FaceSession ()
        {
            _props = new ArrayList<>();
        }

        private FaceSessionProperty GetNewProp() {
            FaceSessionProperty fsp = new FaceSessionProperty();
            _props.add(fsp);
            return fsp;
        }
    }

    private class FaceSessionProperty
    {
        private ArrayList<Helpers.FaceData> _previewfinalscores;
        private Helpers.Tuple<Long,Long> _previewbestfacedata;
        private Helpers.FaceData _previewbestface;
        private String _trimmedVideoOutputFilepath;
        private String _lastfacekey;
        private String _lastfacetskey;

        private FaceSessionProperty()
        {
            _previewfinalscores = new ArrayList<>();
        }
    }

}

