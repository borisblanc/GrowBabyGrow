package com.app.growbabygrow.Classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static android.R.attr.bitmap;
import static android.R.attr.data;
import static android.R.attr.width;


public class CustomFaceDetector extends Detector<Face> {

    private ArrayList<FrameData> _faces = new ArrayList<>();
    private Detector<Face> mDelegate;

    private int testcount = 0;

    public CustomFaceDetector(Detector<Face> delegate, ArrayList<FrameData> faces) {
        mDelegate = delegate;
        _faces = faces;
    }

    public SparseArray<Face> detect(Frame frame) {

        ByteBuffer framebuff = frame.getGrayscaleImageData().duplicate();

        Long timestamp = frame.getMetadata().getTimestampMillis();
        SparseArray<Face> faces = mDelegate.detect(frame);
        _faces.add(new FrameData(timestamp, faces));

        //sample code for saving cropped bitmap
        if (testcount == 100)
        {
            Bitmap bitmap = Utils.GetBitmap(framebuff,frame.getMetadata().getWidth(),frame.getMetadata().getHeight() );
            for (int i = 0; i < faces.size(); i++) {          //can't use for-each loops for SparseArrays
                Face face = faces.valueAt(i);
                //get it's coordinates
                Bitmap faceBitmap = Bitmap.createBitmap(bitmap, (int) face.getPosition().x, (int) face.getPosition().y, (int) face.getWidth(), (int) face.getHeight());
                Utils.testSavebitmap(bitmap, "/storage/emulated/0/Android/data/com.app.growbabygrow/files/ass.bmp");
                //Do whatever you want with this cropped Bitmap
            }

        }


        testcount++;

        return faces;
    }



    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}