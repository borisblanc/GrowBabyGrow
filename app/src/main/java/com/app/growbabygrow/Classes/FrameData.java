package com.app.growbabygrow.Classes;

import android.util.SparseArray;
import com.google.android.gms.vision.face.Face;
import java.util.Comparator;


public class FrameData {

    public SparseArray<Face> _faces;
    public Long _timeStamp;

    public FrameData(Long timeStamp, SparseArray<Face> faces) {
        _timeStamp = timeStamp;
        _faces = faces;
    }

    public static class FaceData {

        public double _score;
        public Long _timeStamp;

        public FaceData(Long timeStamp, double score) {
            _timeStamp = timeStamp;
            _score = score;
        }

    }


    public static class Tuple<X, Y> {
        public final X firstTimeStamp;
        public final Y lastTimeStamp;
        public Tuple(X x, Y y) {
            this.firstTimeStamp = x;
            this.lastTimeStamp = y;
        }
    }

}

