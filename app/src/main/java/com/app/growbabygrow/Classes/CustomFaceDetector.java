package com.app.growbabygrow.Classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.R.attr.bitmap;
import static android.R.attr.data;
import static android.R.attr.width;


public class CustomFaceDetector extends Detector<Face> {

    private ArrayList<FrameData> _faces = new ArrayList<>();
    private Detector<Face> mDelegate;

    private Boolean sampleTaken = false;

    private Lock lock = new ReentrantLock();

    public CustomFaceDetector(Detector<Face> delegate, ArrayList<FrameData> faces) {
        mDelegate = delegate;
        _faces = faces;
    }

    public SparseArray<Face> detect(final Frame frame) {


        Long timestamp = frame.getMetadata().getTimestampMillis();
        final SparseArray<Face> faces = mDelegate.detect(frame);
        _faces.add(new FrameData(timestamp, faces));


        //disable for now
//        if (!sampleTaken)
//        {
//            lock.lock();
//            sampleTaken = true;
//            final ByteBuffer framebuff = frame.getGrayscaleImageData().duplicate();
//            Thread th = new Thread(new Runnable() {
//                public void run() {
//                    SaveSampleFaceImage(framebuff, faces, frame);
//                }
//            });
//            th.start();
//            lock.unlock();
//        }




        return faces;
    }


    //don't want to do this here do it in videoedit activity instead by extracting image by timestamp so i don't slow down face detection
    private void SaveSampleFaceImage(ByteBuffer framebuff, SparseArray<Face> faces, Frame frame)
    {
        Face face = Utils.GetFirstFace(faces);
        float facescore = Utils.GetImageUsability(face);

        if (facescore > .7) { //save if good face

            float actualCropX = face.getPosition().x;
            float actualCropY = face.getPosition().y;

            try { //if face.getPosition().y is negative when cropping this blow ups, need to investigate further
                Bitmap originalbitmap = Utils.GetBitmap(framebuff, frame.getMetadata().getWidth(), frame.getMetadata().getHeight());
                Bitmap faceBitmap = Bitmap.createBitmap(originalbitmap,
                        (int) actualCropX < 0f ? (int) 0f : (int) actualCropX , (int) actualCropY < 0f ? (int) 0f : (int) actualCropY,
                        (int) face.getWidth(), (int) face.getHeight()); //crop
                Bitmap Greyfacebitmap = Utils.toGrayscale(faceBitmap); //grey scale
                Bitmap mirroredbitmap = Bitmap.createBitmap(Greyfacebitmap.getWidth(), Greyfacebitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(mirroredbitmap);
                Paint alphapaint = new Paint();
                alphapaint.setAlpha(80); //transparency
                canvas.drawBitmap(Greyfacebitmap, 0, 0, alphapaint);
                Bitmap finalbitmap = flip(mirroredbitmap);
                Utils.testSavebitmap(finalbitmap, "/storage/emulated/0/Android/data/com.app.growbabygrow/files/tempface2.bmp");
                originalbitmap.recycle();
                faceBitmap.recycle();
                Greyfacebitmap.recycle();
                mirroredbitmap.recycle();
                finalbitmap.recycle();
            }
            catch(Exception e) //this is wonky right now
            {
                e.printStackTrace();
            }

        }
        else
        {
            sampleTaken = false; //try again
        }
    }

    private Bitmap flip (Bitmap src)
    {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}