package com.app.growbabygrow.Classes;

/**
 * Created by boris on 2017-07-27.
 */


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;


import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.width;
import static android.R.attr.y;
import static android.R.id.list;

public class Utils {

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getScreenWidth(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static float getScreenRatio(Context c) {
        DisplayMetrics metrics = c.getResources().getDisplayMetrics();
        return ((float)metrics.heightPixels / (float)metrics.widthPixels);
    }

    public static int getScreenRotation(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getRotation();
    }

    public static int distancePointsF(PointF p1, PointF p2) {
        return (int) Math.sqrt((p1.x - p2.x) *  (p1.x - p2.x) + (p1.y - p2.y) *  (p1.y - p2.y));
    }

    public static PointF middlePoint(PointF p1, PointF p2) {
        if(p1 == null || p2 == null)
            return null;
        return new PointF((p1.x+p2.x)/2, (p1.y+p2.y)/2);
    }

    public static Size[] sizeToSize(android.util.Size[] sizes) {
        Size[] size = new Size[sizes.length];
        for(int i=0; i<sizes.length; i++) {
            size[i] = new Size(sizes[i].getWidth(), sizes[i].getHeight());
        }
        return size;
    }


    public static void testSaveRawImage(Size s, byte [] mPendingFrameData)
    {
        File _filesdir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String createfilepath = new File(_filesdir, Calendar.getInstance().getTimeInMillis() + ".png").getAbsolutePath();

        BufferedOutputStream bos = null;
        try {
            //ByteBuffer imagedata = outputFrame.getGrayscaleImageData().duplicate();
            YuvImage yuvimage = new YuvImage(mPendingFrameData, ImageFormat.NV21, s.getWidth(), s.getHeight(), null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, s.getWidth(), s.getHeight()), 100, baos); // Where 100 is the quality of the generated jpeg
            byte[] jpegArray = baos.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

            bos = new BufferedOutputStream(new FileOutputStream(createfilepath));

            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bitmap.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception ex){
            String a = ex.toString();
        }
        finally {
            if (bos != null) try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static void testSavebitmap(Bitmap bitmap, String fullfilepath)
    {
//        File _filesdir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        String createfilepath = new File(_filesdir, Calendar.getInstance().getTimeInMillis() + ".png").getAbsolutePath();

        BufferedOutputStream bos = null;
        try {

            //Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

            bos = new BufferedOutputStream(new FileOutputStream(fullfilepath));

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            //bitmap.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception ex){
            String a = ex.toString();
        }
        finally {
            if (bos != null) try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static Face GetFirstFace(SparseArray<Face> faces)
    {
        int arraySize = faces.size();
        if (arraySize != 0) {
            for (int i = 0; i < arraySize; i++) {
                if (faces.valueAt(i) != null)
                    return faces.valueAt(i);
            }
        }
        return null;
    }

    public static float GetImageUsability(Face face, int framewidth, int frameheight) //face landmark scores are always 0 to 1 or -1 if not detected
    {
        if (face == null)
            return -1;

        float result = -1;
        try
        {
            //Helpers.FaceLandMarks Facemarks = new Helpers.FaceLandMarks(face.getLandmarks()); //don't use for now
            int vClipSeverity = VerticalClipSeverity(face.getPosition().y, face.getHeight(), frameheight);
            int hCloseSeverity = HorizontalClosenessSeverity(face.getPosition().x, face.getWidth(), framewidth);


            //documentation says eulerY of > +-18 is not facing but thats not really true per testing i was still able to get most landmarks
            if (Math.abs(face.getEulerY()) <= 19 && vClipSeverity <= 20 && hCloseSeverity <= 20) //face exists, forward facing, not too much top or bottom clipping, not too close to sides
            {
                //always zero instead of -1 for these because at least they are forward facing
                float smilescore = face.getIsSmilingProbability() < 0 ? 0 : face.getIsSmilingProbability();
                float righteyescore = face.getIsRightEyeOpenProbability() < 0 ? 0 : face.getIsRightEyeOpenProbability();
                float lefteyescore = face.getIsLeftEyeOpenProbability() < 0 ? 0 : face.getIsLeftEyeOpenProbability();

                result = (smilescore + righteyescore + lefteyescore) / 3;
            }
            else if (Math.abs(face.getEulerY()) > 19) //if not forward facing then return 0
            {
                result = 0;
            }
            else if (vClipSeverity > 20) //if too much top or bottom clipping return 0
            {
                result = 0;
            }
            else if (hCloseSeverity > 20) //if too close to sides return 0
            {
                result = 0;
            }
        }
        catch (Exception e)
        {
            Log.d("utils.GetImageUsability",e.getMessage());
        }
        return result;
    }

    //not so strict because in landscape a close face can easily be clipped by bottom or top of screen
    public static int VerticalClipSeverity(float y, float face_height, int frame_height)
    {
        //bottom and top are inverted in front facing camera!!
        int bottomclipseverity = 0;
        int topclipseverity = 0;
        int framescale20 = (int)(frame_height * .2) * -1; //20 percent scale clipped
        int framescale10 = (int)(frame_height * .1) * -1; //10 percent scale clipped
        int framescale0 = 0; //5 percent scale clipped

        if (y >= framescale0) //no bottom clipping
            bottomclipseverity = 0;
        else if (y < framescale0 && y >= framescale10) //small clipping of bottom
            bottomclipseverity = 10;
        else if (y < framescale10 && y >= framescale20) //medium clipping of bottom
            bottomclipseverity = 20;
        else
            bottomclipseverity = 51; //too much clipping


        if (y + face_height <= frame_height) //no top clipping
            topclipseverity = 0;
        else if (y + face_height > frame_height) //clipping confirmed
        {
            if ((y + face_height) - frame_height > framescale0 && (y + face_height) - frame_height <= Math.abs(framescale10))
                topclipseverity = 10; //small clipping of top
            else if ((y + face_height) - frame_height > Math.abs(framescale10) && (y + face_height) - frame_height <= Math.abs(framescale20))
                topclipseverity = 20; //medium clipping of top
            else
                topclipseverity = 51; //too much clipping
        }


        int result=0;

        if (topclipseverity > 0 && bottomclipseverity > 0) //face too close, very bad, can happen in landscape because of small height of screen
            result = 51;
        else {
            if (topclipseverity > 0) //any clipping
                result = topclipseverity;
            if (bottomclipseverity > 0) //any clipping
                result = bottomclipseverity;
        }


        return result;
    }

    //strict because in landscape we want to give lower score to frames around the far right or left edges
    //severity here is not based on clipping but on closeness to edge
    public static int HorizontalClosenessSeverity(float x, float face_width, int frame_width)
    {
        //right and left are inverted in front facing camera!!
        int rightclipseverity = 0;
        int leftclipseverity = 0;
        int framescale20 = (int)(frame_width * .2); //20 percent scale
        int framescale10 = (int)(frame_width * .1); //10 percent scale
        int framescale5 = (int)(frame_width * .05); //5 percent scale


        if (x >= framescale20) //not close to left
            leftclipseverity = 0;
        else if (x < framescale20 && x >= framescale10) //close to left
            leftclipseverity = 10;
        else if (x < framescale10 && x >= framescale5) //very close to left
            leftclipseverity = 20;
        else
            leftclipseverity = 51; //clipping


        if (x + face_width + framescale20 <= frame_width) //not close to right
            rightclipseverity = 0;
        else if (x + face_width + framescale20 > frame_width) //too close to right confirmed
        {
            if (Math.abs((x + face_width) - frame_width) < framescale20 && Math.abs((x + face_width) - frame_width) >= framescale10)
                rightclipseverity = 10; //close to right
            else if (Math.abs((x + face_width) - frame_width) < framescale10 && Math.abs((x + face_width) - frame_width) >= framescale5)
                rightclipseverity = 20; //very close to right
            else
                rightclipseverity = 51; //basically clipping
        }


        int result = 0;
        if (leftclipseverity > 0) //any left is bad
            result = leftclipseverity;
        else if (rightclipseverity > 0) //any right is bad
            result = rightclipseverity;


        return result;
    }


    public static double calculateAverage(ArrayList<Helpers.FaceData>  table) {
        double sum = 0;
        if(!table.isEmpty()) {
            for (Helpers.FaceData mark : table) {
                sum += mark._score;
            }
            return sum / (double) table.size();
        }
        return sum;
    }

    public static double stDev (ArrayList<Helpers.FaceData> table)
    {
        // Step 1:
        double mean = calculateAverage(table);
        double temp = 0;

        for (int i = 0; i < table.size(); i++)
        {
            double val = table.get(i)._score;

            // Step 2:
            double squrDiffToMean = Math.pow(val - mean, 2);

            // Step 3:
            temp += squrDiffToMean;
        }

        // Step 4:
        double meanOfDiffs = temp / (double) table.size();

        // Step 5:
        return Math.sqrt(meanOfDiffs);
    }


    public static Helpers.FaceData getMaxFace(ArrayList<Helpers.FaceData> list)
    {
        Helpers.FaceData max = new Helpers.FaceData(Long.valueOf(0) ,0);
        int size = list.size();
        for(int i = 0; i < size; i++)
        {
            Helpers.FaceData current = list.get(i);

            if(current._score > max._score){
                max = current;
            }
        }
        return max;
    }

    public static Helpers.FaceData getMaxFace(ArrayList<Helpers.FaceData> list, ArrayList<Helpers.FaceData> exceptions)
    {
        Helpers.FaceData max = new Helpers.FaceData(Long.valueOf(0) ,0);
        int size = list.size();
        for(int i = 0; i < size; i++)
        {
            Helpers.FaceData current = list.get(i);

            if (exceptions.contains(current)) //if in exceptions then skip, can't be best face
                continue;

            if(current._score > max._score){
                max = current;
            }
        }
        return max;
    }

    public static boolean RenameFile(File dir, String fromname, String toname)
    {
        File from = new File(dir, fromname);
        File to = new File(dir, toname);

        return from.exists() && from.renameTo(to);
    }


    /**
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     * @param duration     Animation duration in ms
     */
    public static void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = toVisibility == View.VISIBLE;
        view.bringToFront();
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }

    public static Bitmap GetBitmap(ByteBuffer framebuff, int FrameWidth, int FrameHeight)
    {
        YuvImage yuvimage = GetYUVImage(framebuff, FrameWidth, FrameHeight);

        //framebuff.rewind();
        //int[] argb8888 = new int[framebuff.remaining()];
        //swapNV21_NV12(yuvimage.getYuvData(), FrameWidth, FrameHeight);
        Bitmap b;

        ByteArrayOutputStream baos = new ByteArrayOutputStream ();

        yuvimage.compressToJpeg(new Rect(0, 0, FrameWidth, FrameHeight), 100, baos); // Where 100 is the quality of the generated jpeg
        byte[] jpegArray = baos.toByteArray();
        b = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

        return b;
    }

    public static YuvImage GetYUVImage(ByteBuffer framebuff, int FrameWidth, int FrameHeight)
    {
        byte[] barray = new byte[framebuff.remaining()];
        framebuff.get(barray);

        return new YuvImage(barray, ImageFormat.NV21, FrameWidth, FrameHeight, null);
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int scalefactor) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        final int newwidth = bitmapWidth/ scalefactor;
        final int newheight = bitmapHeight/ scalefactor;

//        final float scale = Math.min((float) newwidth / (float) bitmapWidth, (float) newheight / (float) bitmapHeight);
//
//        final int scaledWidth = (int) (bitmapWidth * scale);
//        final int scaledHeight = (int) (bitmapHeight * scale);

        final Bitmap decoded = Bitmap.createScaledBitmap(bitmap, newwidth, newheight, true);
        final Canvas canvas = new Canvas(decoded);

        return decoded;
    }

//    private static int ConvertDpToPixels(int value)
//    {
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
//    }

}
