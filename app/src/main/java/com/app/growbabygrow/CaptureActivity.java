package com.app.growbabygrow;




import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraDevice;
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
import com.app.growbabygrow.Classes.FrameData;
import com.app.growbabygrow.Classes.GraphicOverlay;
import com.app.growbabygrow.Classes.VideoUtils;
import com.app.growbabygrow.Classes.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    private FaceSession FS;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); lock this in manifest instead
            setContentView(R.layout.captureactivity_main);
            context = getApplicationContext();

            sharedpreferences = getSharedPreferences(getString(R.string.p_file1_key), Context.MODE_PRIVATE);

            FS = new FaceSession();
            FS.MainMergedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_main_mp4pathname), null);
            FS.OriginalVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_orig_mp4pathname), null);

            FaceSessionProperty fsp1 = FS.GetNewProp();
            fsp1._trimmedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_trim1_mp4pathname), null);
            FaceSessionProperty fsp2 = FS.GetNewProp();
            fsp2._trimmedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_trim2_mp4pathname), null);
            FaceSessionProperty fsp3 = FS.GetNewProp();
            fsp3._trimmedVideoOutputFilepath = sharedpreferences.getString(getString(R.string.p_file1_saved_trim3_mp4pathname), null);



            recButton = (Button) findViewById(R.id.btn_record);
            Button switchButton = (Button) findViewById(R.id.btn_switch);
            mPreview = (CameraSourcePreview) findViewById(R.id.preview);
            mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
            cameraVersion = (TextView) findViewById(R.id.cameraVersion);
            ivAutoFocus = (ImageView) findViewById(R.id.ivAutoFocus);

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


                recButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCamera2Source.mTrackRecord) {
                            stopCameraSource(); //call this to release everything or all the shit breaks
                            trackRecord = false;
                            recButton.setText(R.string.record);
                            requestPermissionThenOpenCamera();

                            //run all faceprocessing
                            Toast.makeText(context, "Starting Face Processing", Toast.LENGTH_SHORT).show();
                            FaceSession FS = RunFaceProcessing();

                            Toast.makeText(context, "Starting Trim Video", Toast.LENGTH_SHORT).show();
                            //trim all preview videos
                            for(FaceSessionProperty fsp : FS._props)
                            {
                                CreateTrimmedVideo(fsp._previewbestfacedata, FS.OriginalVideoOutputFilepath, fsp._trimmedVideoOutputFilepath);
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


                //mPreview.setOnTouchListener(CameraPreviewTouchListener);
            }
        } catch (Throwable e) {
            Log.d(TAG,e.getMessage());
        }

    }


    private FaceSession RunFaceProcessing()
    {
        FaceSessionProperty previousfsp = new FaceSessionProperty();
        for(FaceSessionProperty fsp : FS._props)
        {
            if (fsp == null) { //can't do anything without first prop session
                Log.d("RunFaceProcessing","Noting to process!!!");
                return null;
            }

            if (FS._props.indexOf(fsp) == 0 ) //first facesession process like this
            {
                Processfaces(FS._faces, fsp); //preview 1 processing only but gets data for subsequent previews
                previousfsp = fsp;
            }
            else //preview processing based on previous preview
            {
                fsp._previewfinalscores = ArrayMinusException(previousfsp._previewfinalscores, previousfsp._previewbestface);
                fsp._previewbestfacedata = BestFacedata(previousfsp._previewfinalscores, fsp._previewbestface); //this will also set current best face for next preview
            }
        }

        return FS;
    }



    private void CreateTrimmedVideo(FrameData.Tuple<Long,Long> bestfacetimestamps, String OriginalVideoOutputFilepath, String TrimmedVideoOutputFilepath)
    {
        if (bestfacetimestamps == null)
            return;

        VideoUtils.TrimVideo(OriginalVideoOutputFilepath, TrimmedVideoOutputFilepath, bestfacetimestamps.x, bestfacetimestamps.y, false, true);


//        Toast.makeText(this, "Starting Merge Video", Toast.LENGTH_SHORT).show();
//        VideoUtils.mergeVideos(MainMergedVideoOutputFilepath, OriginalVideoOutputFilepath, TrimmedVideoOutputFilepath);
//
//        Toast.makeText(this, "All Done!!", Toast.LENGTH_SHORT).show();
    }

    //process all of first preview (data & results) and subsequent data only for all other previews
    private void Processfaces(ArrayList<FrameData> _faces, FaceSessionProperty P)
    {
        if (_faces == null || _faces.size() < GetFrameTotal()) {
            Toast.makeText(context, "Not enough frames captured to process video for trim", Toast.LENGTH_SHORT).show();
            return;
        }

        int coreframeslength = _fps * 2; //core sample of frames will be two seconds of video might in future vary depending on user settings
        int computelimit = _faces.size() - GetFrameTotal(); //this will keep walking average calcs only happening within range

        for(FrameData facedata : _faces )
        {
            Long currenttimestamp = facedata._timeStamp;
            if (_faces.indexOf(facedata) < computelimit) //makes sense to compute because we will can use it
            {
                List<FrameData> coreframes = _faces.subList(_faces.indexOf(facedata), _faces.indexOf(facedata) + coreframeslength);
                ArrayList<FrameData.FaceData> corescores = new ArrayList<>();
                for (FrameData face : coreframes)
                {
                    corescores.add(new FrameData.FaceData(face._timeStamp, Utils.GetImageUsability(Utils.GetFirstFace(face._faces)))); //todo improve this by moving all the face finding and scoring outside so its done only once per frame
                }

                double avg = Utils.calculateAverage(corescores);
                double stDev = Utils.stDev(corescores);

                P._previewfinalscores.add(new FrameData.FaceData(currenttimestamp, avg < stDev ? 0 :  avg  - stDev)); //avg - std dev should give those with best avg score and lowest deviation /no negatives
            }
            else //can't use computations past this point so just need timestamps
            {
                P._previewfinalscores.add(new FrameData.FaceData(currenttimestamp, 0 ));
            }
        }

        P._previewbestface = Collections.max(P._previewfinalscores, new FrameData.compScore());

        FrameData.FaceData bestLastface = P._previewfinalscores.get(P._previewfinalscores.indexOf(P._previewbestface) + GetFrameTotal());

        P._previewbestfacedata = new FrameData.Tuple<>(P._previewbestface._timeStamp, bestLastface._timeStamp);
    }

    private ArrayList<FrameData.FaceData> ArrayMinusException(ArrayList<FrameData.FaceData> finalscores, FrameData.FaceData _faceexception)
    {
        int offset = _fps; //frame offset for each additional previews so we get something different, make it equal to fps which is one second of video
        ArrayList<FrameData.FaceData> final_scores_noexceptions = new ArrayList<> (finalscores);
        int exception1index = finalscores.indexOf(_faceexception);
        final_scores_noexceptions.subList(exception1index, offset).clear();
        return final_scores_noexceptions;
    }

    private FrameData.Tuple<Long,Long> BestFacedata(ArrayList<FrameData.FaceData> finalscores, FrameData.FaceData bestface)
    {
        bestface = Collections.max(finalscores, new FrameData.compScore());

        FrameData.FaceData bestLastface = finalscores.get(finalscores.indexOf(bestface) + GetFrameTotal());

        return new FrameData.Tuple<>(bestface._timeStamp, bestLastface._timeStamp);
    }


    private int GetFrameTotal()
    {
        return _fps * _vidlengthseconds; //total frames will always be frames per second * number of seconds
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
                    .setMode(FaceDetector.FAST_MODE)
                    .setProminentFaceOnly(true)
                    .setTrackingEnabled(true)
                    .build();

            CustomFaceDetector faceDetector = new CustomFaceDetector(previewFaceDetector, FS._faces);

            if (previewFaceDetector.isOperational()) {
                //previewFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
                faceDetector.setProcessor(new LargestFaceFocusingProcessor.Builder(previewFaceDetector, new GraphicFaceTracker(mGraphicOverlay)).build());

            }
            else {
                Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
            }

            if (useCamera2) {
                mCamera2Source = new Camera2Source.Builder(context, faceDetector, FS.OriginalVideoOutputFilepath)
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
                cameraVersion.setText("Camera 2");
                try {mPreview.start(mCamera2Source, mGraphicOverlay, trackRecord);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to start camera source 2.", e);
                    mCamera2Source.release();
                    mCamera2Source = null;
                }
            }
        } else {
            if (mCameraSource != null) {
                cameraVersion.setText("Camera 1");
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
            mFaceGraphic = new FaceGraphic(overlay, context);
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


    private class FaceSession
    {
        public ArrayList<FrameData> _faces = new ArrayList<>();
        public ArrayList<FaceSessionProperty> _props;
        public String MainMergedVideoOutputFilepath;
        public String OriginalVideoOutputFilepath;

        public FaceSession ()
        {
            _props = new ArrayList<>();
        }

        public FaceSessionProperty GetNewProp() {
            FaceSessionProperty fsp = new FaceSessionProperty();
            _props.add(new FaceSessionProperty());
            return fsp;
        }
    }

    private class FaceSessionProperty
    {
        public ArrayList<FrameData.FaceData> _previewfinalscores;
        public FrameData.Tuple<Long,Long> _previewbestfacedata;
        public FrameData.FaceData _previewbestface;
        public String _trimmedVideoOutputFilepath;

        public FaceSessionProperty()
        {
            _previewfinalscores = new ArrayList<>();
        }
    }

}

