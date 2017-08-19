package com.app.growbabygrow.Classes;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;

import com.app.growbabygrow.R;
import com.app.growbabygrow.Classes.GraphicOverlay;
import com.app.growbabygrow.Classes.Utils;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.R.attr.x;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private Bitmap smiley;
    private Bitmap cool;
    private Bitmap myface;

    private ArrayList<Bitmap> animation1;
    public Boolean show_Prev_Session_Overlay = false;
    public Boolean show_Smile_Counter = false;

    private BitmapFactory.Options opt;
    private Resources resources;

    private int faceId;

    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private static int mCurrentColorIndex = 0;

    private volatile Face mFace;

    private float numberofsmiles = 0;
    private float numberofgoodfaces = 0;

    private Face lastsessionface;

    private double animatecounter = 0;

    private int prev_session_overlay_counter = 0;

    private Context mContext;


    public FaceGraphic(GraphicOverlay overlay, Context context, Face Lastsessionface, String OverlayBitmapFilePath) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);


        opt = new BitmapFactory.Options();
        opt.inScaled = false;
        mContext = context;
        resources = mContext.getResources();

        smiley = BitmapFactory.decodeResource(resources, R.drawable.smile2, opt);
        cool = BitmapFactory.decodeResource(resources, R.drawable.cool, opt);
        lastsessionface = Lastsessionface;

        File overlaybitmap = new File(OverlayBitmapFilePath);
        if (overlaybitmap.exists()) {

            int bitmapscale = context.getResources().getInteger(R.integer.overlay_image_scale); //global constant for bitmap scale
            Bitmap smallface = BitmapFactory.decodeFile(overlaybitmap.getAbsolutePath());
            myface = Bitmap.createScaledBitmap(smallface, smallface.getWidth() * bitmapscale, smallface.getHeight() * bitmapscale, false);
        }

//        animation1 = new ArrayList<>();
//
//        AssetManager am = context.getAssets();
//        for (int i = 0; i < 18; i++)
//        {
//            Bitmap bmp = null;
//            try {
//                bmp = BitmapFactory.decodeStream(am.open("Animation1/frame_" + i +".gif"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            animation1.add(bmp);
//
//        }
    }

    public void setId(int id) {
        faceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    public void goneFace() {
        mFace = null;
    }

    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;

        if (face == null) //if null don't bother any face tracking draws
            return;

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        if (show_Smile_Counter) { //for recording only
            if (face.getIsSmilingProbability() > .7)
                numberofsmiles += .2;

            if (Utils.GetImageUsability(face, mOverlay.mPreviewWidth, mOverlay.mPreviewHeight) > .7)
                numberofgoodfaces += .2;

            canvas.drawBitmap(cool, 10, canvas.getHeight() - 70, null); //bottom left
            canvas.drawText("Good Looks: " + Math.round(numberofgoodfaces), 100, canvas.getHeight() - 20, mIdPaint);

            canvas.drawBitmap(smiley, 10, canvas.getHeight() - 150, null); //bottom right on top of above
            canvas.drawText("Smiles: " + Math.round(numberofsmiles), 100, canvas.getHeight() - 100, mIdPaint);
        }

        // Draws a bounding box around the face, for both preview and recording
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        //for preview only show prev session overlay to show user last week position
        if (show_Prev_Session_Overlay && lastsessionface != null && myface != null) //&& prev_session_overlay_counter < 50) //prev_session_overlay_counter < 50 says it will show for 50 frames, not sure what fps is here
        {
            float oldx = translateX(lastsessionface.getPosition().x + lastsessionface.getWidth() / 2);
            float oldy = translateY(lastsessionface.getPosition().y + lastsessionface.getHeight() / 2);
            canvas.drawCircle(oldx, oldy, FACE_POSITION_RADIUS, mFacePositionPaint); //this will show center of face from last week
            canvas.drawBitmap(myface, oldx - (myface.getWidth() / 2), oldy - (myface.getHeight() / 2), null); //puts image in center of where it was last week

            //Utils.SetViewTooltip(mContext, myface, "Save Baby Grow Project", true);
            //prev_session_overlay_counter++; //determines how long prev session overlay will display for on screen
        }
    }


//        if (Math.round(numberofgoodfaces) >= 10)
//        {
//            if (animatecounter <= 17) {
//                canvas.drawBitmap(animation1.get((int)Math.round(animatecounter)), x + ID_X_OFFSET *2 , y + ID_Y_OFFSET * 2, null);
//                animatecounter += .3; //rate of frames
//            }
//            else
//                animatecounter = 0;
//        }


        //        if (myface != null) //test only
//            canvas.drawBitmap(marker, x - (myface.getWidth()/ 2), y - (myface.getHeight()/2), null); //puts image in center of face
        //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        //canvas.drawText("id: " + faceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);

//        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
//        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

//        canvas.drawText("Number of smiles: " + numberofsmiles, x - ID_X_OFFSET * 3, y - ID_Y_OFFSET *3 , mIdPaint);
//        canvas.drawText("Number of good looks: " + numberofgoodfaces, x + ID_X_OFFSET * 3, y + ID_Y_OFFSET *3 , mIdPaint);

//        canvas.drawBitmap(smiley, canvas.getWidth() /2, 0, null);
//        canvas.drawText("Smiles: " + numberofsmiles, (canvas.getWidth() /2) + 100, 50 , mIdPaint);

    //}


}