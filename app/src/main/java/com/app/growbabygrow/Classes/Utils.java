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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.app.growbabygrow.Classes.FrameData;
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

    public static float GetImageUsability(Face face) //face landmark scores are always 0 to 1 or -1 if not detected
    {
        try
        {
            FrameData.FaceLandMarks Facemarks = new FrameData.FaceLandMarks(face.getLandmarks());


            if (face != null && Math.abs(face.getEulerY()) <= 18 && Facemarks.HasRequiredLandmarks()) //forward facing
            {
                //always zero instead of -1 for these because at least they are forward facing
                float smilescore = face.getIsSmilingProbability() < 0 ? 0 : face.getIsSmilingProbability();
                float righteyescore = face.getIsRightEyeOpenProbability() < 0 ? 0 : face.getIsRightEyeOpenProbability();
                float lefteyescore = face.getIsLeftEyeOpenProbability() < 0 ? 0 : face.getIsLeftEyeOpenProbability();

                return (smilescore + righteyescore + lefteyescore) / 3;
            }
            else if (face != null && Math.abs(face.getEulerY()) > 18) //if not forward facing then return 0
            {
                return 0;
            }
            else if (face != null && !Facemarks.HasRequiredLandmarks()) //if not all required landmarks present give -1 because face has probably gotten clipped offscreen
            {
                return 0;
            }
            else //if no face -1
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            Log.d("utils.GetImageUsability",e.getMessage());
            return 0;
        }
    }



    public static double calculateAverage(ArrayList<FrameData.FaceData>  table) {
        double sum = 0;
        if(!table.isEmpty()) {
            for (FrameData.FaceData mark : table) {
                sum += mark._score;
            }
            return sum / (double) table.size();
        }
        return sum;
    }

    public static double stDev (ArrayList<FrameData.FaceData> table)
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


    public static FrameData.FaceData getMaxFace(ArrayList<FrameData.FaceData> list)
    {
        FrameData.FaceData max = new FrameData.FaceData(Long.valueOf(0) ,0);
        int size = list.size();
        for(int i = 0; i < size; i++)
        {
            FrameData.FaceData current = list.get(i);

            if(current._score > max._score){
                max = current;
            }
        }
        return max;
    }

    public static FrameData.FaceData getMaxFace(ArrayList<FrameData.FaceData> list, ArrayList<FrameData.FaceData> exceptions)
    {
        FrameData.FaceData max = new FrameData.FaceData(Long.valueOf(0) ,0);
        int size = list.size();
        for(int i = 0; i < size; i++)
        {
            FrameData.FaceData current = list.get(i);

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

}
