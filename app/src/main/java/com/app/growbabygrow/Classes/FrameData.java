package com.app.growbabygrow.Classes;

import android.util.SparseArray;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;


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

    public static class FaceLandMarks //class used to make sure all essential landmarks are present, helps with score when face gets clipped offscreen
    {
        public Boolean leftEyePos = false;
        public Boolean rightEyePos = false;
        public Boolean noseBasePos = false;
        public Boolean leftMouthCorner = false;
        public Boolean rightMouthCorner = false;
        public Boolean mouthBase = false;
        public Boolean leftEar = false;
        public Boolean leftEarTip = false;
        public Boolean rightEarTip = false;
        public Boolean rightEar = false;
        public Boolean leftCheek = false;
        public Boolean rightCheek = false;


        public FaceLandMarks(List<Landmark> landmarks)
        {
            for (Landmark landmark : landmarks) {
                switch (landmark.getType()) {
                    case Landmark.LEFT_EYE:
                        leftEyePos = true;
                        break;
                    case Landmark.RIGHT_EYE:
                        rightEyePos = true;
                        break;
                    case Landmark.NOSE_BASE:
                        noseBasePos = true;
                        break;
                    case Landmark.LEFT_MOUTH:
                        leftMouthCorner = true;
                        break;
                    case Landmark.RIGHT_MOUTH:
                        rightMouthCorner = true;
                        break;
                    case Landmark.BOTTOM_MOUTH:
                        mouthBase = true;
                        break;
                    case Landmark.LEFT_EAR:
                        leftEar = true;
                        break;
                    case Landmark.RIGHT_EAR:
                        rightEar = true;
                        break;
                    case Landmark.LEFT_EAR_TIP:
                        leftEarTip = true;
                        break;
                    case Landmark.RIGHT_EAR_TIP:
                        rightEarTip = true;
                        break;
                    case Landmark.LEFT_CHEEK:
                        leftCheek = true;
                        break;
                    case Landmark.RIGHT_CHEEK:
                        rightCheek = true;
                        break;
                }
            }
        }

        public Boolean HasRequiredLandmarks()
        {
            return mouthBase && rightEyePos && leftEyePos;
        }

    }

}

