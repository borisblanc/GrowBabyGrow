package com.app.growbabygrow.Classes;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.util.ArrayList;



public class CustomFaceDetector extends Detector<Face> {

    private ArrayList<Helpers.FrameData> _faces = new ArrayList<>();
    private Detector<Face> mDelegate;

    public CustomFaceDetector(Detector<Face> delegate, ArrayList<Helpers.FrameData> faces) {
        mDelegate = delegate;
        _faces = faces;
    }

    public SparseArray<Face> detect(Frame frame) {
        SparseArray<Face> faces = mDelegate.detect(frame);
        _faces.add(new Helpers.FrameData(frame.getMetadata().getTimestampMillis(), faces, frame.getMetadata().getWidth(),frame.getMetadata().getHeight()));
        return faces;
    }


    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}