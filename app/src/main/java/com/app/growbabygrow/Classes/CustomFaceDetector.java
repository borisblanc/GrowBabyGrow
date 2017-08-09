package com.app.growbabygrow.Classes;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class CustomFaceDetector extends Detector<Face> {

    private ArrayList<Helpers.FrameData> _faces = new ArrayList<>();
    private Detector<Face> mDelegate;

    public CustomFaceDetector(Detector<Face> delegate, ArrayList<Helpers.FrameData> faces) {
        mDelegate = delegate;
        _faces = faces;
    }

    public SparseArray<Face> detect(final Frame frame) {

        Long timestamp = frame.getMetadata().getTimestampMillis();
        final SparseArray<Face> faces = mDelegate.detect(frame);
        _faces.add(new Helpers.FrameData(timestamp, faces));

        return faces;
    }


    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}