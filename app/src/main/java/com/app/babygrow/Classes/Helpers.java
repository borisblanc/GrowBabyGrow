package com.app.babygrow.Classes;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.app.babygrow.R;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class Helpers {

    public static class FrameData {

        public SparseArray<Face> _faces;
        public Long _timeStamp;
        public int _frameHeight;
        public int _frameWidth;

        FrameData(Long timeStamp, SparseArray<Face> faces, int frameWidth, int frameHeight) {
            _timeStamp = timeStamp;
            _faces = faces;
            _frameWidth = frameWidth;
            _frameHeight = frameHeight;
        }

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


        public FaceLandMarks(List<Landmark> landmarks) {
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

        public Boolean HasRequiredLandmarks() {
            return mouthBase && rightEyePos && leftEyePos;
        }

    }

    public static class NavItem {
        public String mName;
        public String mTitle;
        public String mSubtitle;
        public int mIcon;

        public NavItem(String name, String title, String subtitle, int icon) {
            mName = name;
            mTitle = title;
            mSubtitle = subtitle;
            mIcon = icon;
        }
    }

    public static class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<NavItem> mNavItems;
        public HashMap<Integer, View> Views = new HashMap <>();
        ListView mDrawerList;
        int mDefaultPosition;

        public DrawerListAdapter(Context context, ArrayList<NavItem> navItems, ListView DrawerList, int DefaultPosition) {
            mContext = context;
            mNavItems = navItems;
            mDrawerList = DrawerList;
            mDefaultPosition = DefaultPosition;
        }

        @Override
        public int getCount() {
            return mNavItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNavItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            titleView.setText( mNavItems.get(position).mTitle );
            subtitleView.setText( mNavItems.get(position).mSubtitle );
            iconView.setImageResource(mNavItems.get(position).mIcon);

            Views.put(position,view);

            if(position == mDefaultPosition){
                mDrawerList.performItemClick(view, position, mDrawerList.getItemIdAtPosition(position));
            }

            return view;
        }

//        public View getSavedView(int position) {
//            return Views.get(position);
//        }

    }

    public static class Logger
    {

        public static void LogExceptionToFile(String Tag, File fullfilepath, Throwable ex)
        {
            LogExceptionToFile(Tag, fullfilepath, ex, null);
        }

        public static void LogExceptionToFile(String Tag, File fullfilepath, Throwable ex, @Nullable String Threadname)
        {

            PrintStream ps = null;

            if (!fullfilepath.exists()) {
                try {
                    fullfilepath.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());

            try {
                ps = new PrintStream(new FileOutputStream(fullfilepath, true));
                ps.append("\r\n");
                ps.append("TimeStamp: ").append(formattedDate);
                ps.append("\r\n");
                ps.append("Location: ").append(Tag);
                ps.append("\r\n");
                if (Threadname != null)
                {
                    ps.append("ThreadName: ").append(Threadname);
                    ps.append("\r\n");
                }
                ps.append("StackTrace..." );
                ps.append("\r\n");
                ex.printStackTrace(ps);
            } catch (FileNotFoundException e) {
                Log.d("Logger", ex.getMessage(), ex);
            }

            if (ps != null) {
                ps.close();
            }
        }


        public static File ErrorLoggerFilePath(Context context, String FileName)
        {
            File error_dir = context.getExternalFilesDir(context.getString(R.string.Error_Log_Dir));
            if (!error_dir.exists())
                error_dir.mkdir();

            return new File(error_dir, FileName + ".txt");
        }
    }

}

