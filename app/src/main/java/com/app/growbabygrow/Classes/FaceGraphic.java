package com.app.growbabygrow.Classes;

import android.content.Context;
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

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private Bitmap marker;

    private BitmapFactory.Options opt;
    private Resources resources;

    private int faceId;
    PointF facePosition;
    float faceWidth;
    float faceHeight;
    PointF faceCenter;
    float isSmilingProbability = -1;
    float eyeRightOpenProbability = -1;
    float eyeLeftOpenProbability = -1;
    float eulerZ;
    float eulerY;
    PointF leftEyePos = null;
    PointF rightEyePos = null;
    PointF noseBasePos = null;
    PointF leftMouthCorner = null;
    PointF rightMouthCorner = null;
    PointF mouthBase = null;
    PointF leftEar = null;
    PointF rightEar = null;
    PointF leftEarTip = null;
    PointF rightEarTip = null;
    PointF leftCheek = null;
    PointF rightCheek = null;

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

    public FaceGraphic(GraphicOverlay overlay, Context context) {
        super(overlay);
        opt = new BitmapFactory.Options();
        opt.inScaled = false;
        resources = context.getResources();
        marker = BitmapFactory.decodeResource(resources, R.drawable.marker, opt);


    }

    public FaceGraphic(GraphicOverlay overlay) {
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
    }

    public void setId(int id) {
        faceId = id;
    }

    public float getSmilingProbability() {
        return isSmilingProbability;
    }

    public float getEyeRightOpenProbability() {
        return eyeRightOpenProbability;
    }

    public float getEyeLeftOpenProbability() {
        return eyeLeftOpenProbability;
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

//    @Override
//    public void draw(Canvas canvas) {
//        Face face = mFace;
//        if (face == null) {
//            return;
//        }
//
//        // Draws a circle at the position of the detected face, with the face's track id below.
//        float x = translateX(face.getPosition().x + face.getWidth() / 2);
//        float y = translateY(face.getPosition().y + face.getHeight() / 2);
//        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
//        canvas.drawText("id: " + faceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
//        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);
//
//        // Draws a bounding box around the face.
//        float xOffset = scaleX(face.getWidth() / 2.0f);
//        float yOffset = scaleY(face.getHeight() / 2.0f);
//        float left = x - xOffset;
//        float top = y - yOffset;
//        float right = x + xOffset;
//        float bottom = y + yOffset;
//        canvas.drawRect(left, top, right, bottom, mBoxPaint);
//    }


    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            isSmilingProbability = -1;
            eyeRightOpenProbability = -1;
            eyeLeftOpenProbability = -1;
            return;
        }

        facePosition = new PointF(translateX(face.getPosition().x), translateY(face.getPosition().y));


        faceWidth = face.getWidth() * 4;
        faceHeight = face.getHeight() * 4;
        faceCenter = new PointF(translateX(face.getPosition().x + faceWidth / 8), translateY(face.getPosition().y + faceHeight / 8));
        isSmilingProbability = face.getIsSmilingProbability();
        eyeRightOpenProbability = face.getIsRightEyeOpenProbability();
        eyeLeftOpenProbability = face.getIsLeftEyeOpenProbability();
        eulerY = face.getEulerY();
        eulerZ = face.getEulerZ();
        //DO NOT SET TO NULL THE NON EXISTENT LANDMARKS. USE OLDER ONES INSTEAD.
        for (Landmark landmark : face.getLandmarks()) {
            switch (landmark.getType()) {
                case Landmark.LEFT_EYE:
                    leftEyePos = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.RIGHT_EYE:
                    rightEyePos = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.NOSE_BASE:
                    noseBasePos = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.LEFT_MOUTH:
                    leftMouthCorner = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.RIGHT_MOUTH:
                    rightMouthCorner = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.BOTTOM_MOUTH:
                    mouthBase = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.LEFT_EAR:
                    leftEar = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.RIGHT_EAR:
                    rightEar = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.LEFT_EAR_TIP:
                    leftEarTip = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.RIGHT_EAR_TIP:
                    rightEarTip = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.LEFT_CHEEK:
                    leftCheek = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
                case Landmark.RIGHT_CHEEK:
                    rightCheek = new PointF(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y));
                    break;
            }
        }

        Paint mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(4);
        if (faceCenter != null)
            canvas.drawBitmap(marker, faceCenter.x, faceCenter.y, null);
        if (noseBasePos != null)
            canvas.drawBitmap(marker, noseBasePos.x, noseBasePos.y, null);
        if (leftEyePos != null)
            canvas.drawBitmap(marker, leftEyePos.x, leftEyePos.y, null);
        if (rightEyePos != null)
            canvas.drawBitmap(marker, rightEyePos.x, rightEyePos.y, null);
        if (mouthBase != null)
            canvas.drawBitmap(marker, mouthBase.x, mouthBase.y, null);
        if (leftMouthCorner != null)
            canvas.drawBitmap(marker, leftMouthCorner.x, leftMouthCorner.y, null);
        if (rightMouthCorner != null)
            canvas.drawBitmap(marker, rightMouthCorner.x, rightMouthCorner.y, null);
        if (leftEar != null)
            canvas.drawBitmap(marker, leftEar.x, leftEar.y, null);
        if (rightEar != null)
            canvas.drawBitmap(marker, rightEar.x, rightEar.y, null);
        if (leftEarTip != null)
            canvas.drawBitmap(marker, leftEarTip.x, leftEarTip.y, null);
        if (rightEarTip != null)
            canvas.drawBitmap(marker, rightEarTip.x, rightEarTip.y, null);
        if (leftCheek != null)
            canvas.drawBitmap(marker, leftCheek.x, leftCheek.y, null);
        if (rightCheek != null)
            canvas.drawBitmap(marker, rightCheek.x, rightCheek.y, null);



//        if (facePosition != null)
//            canvas.drawBitmap(marker, face.getPosition().x, face.getPosition().y, null);
    }
}