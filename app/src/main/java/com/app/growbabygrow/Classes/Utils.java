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
import android.graphics.ImageFormat;
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;

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
            if (face != null && Math.abs(face.getEulerY()) <= 18) //forward facing
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

    public static void decodeYUV(int[] out, byte[] fg, int width, int height)
            throws NullPointerException, IllegalArgumentException {
        int sz = width * height;
        if (out == null)
            throw new NullPointerException("buffer out is null");
        if (out.length < sz)
            throw new IllegalArgumentException("buffer out size " + out.length
                    + " < minimum " + sz);
        if (fg == null)
            throw new NullPointerException("buffer 'fg' is null");
        if (fg.length < sz)
            throw new IllegalArgumentException("buffer fg size " + fg.length
                    + " < minimum " + sz * 3 / 2);
        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = fg[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
                    Cb = fg[cOff];
                    if (Cb < 0)
                        Cb += 127;
                    else
                        Cb -= 128;
                    Cr = fg[cOff + 1];
                    if (Cr < 0)
                        Cr += 127;
                    else
                        Cr -= 128;
                }
                int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                if (R < 0)
                    R = 0;
                else if (R > 255)
                    R = 255;
                int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
                        + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                if (G < 0)
                    G = 0;
                else if (G > 255)
                    G = 255;
                int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
                if (B < 0)
                    B = 0;
                else if (B > 255)
                    B = 255;
                out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
            }
        }
    }

    public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
        final int frameSize = width * height;

        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, ci = ii; i < height; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv[ci * width + cj]));
                int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    public static void decodeYUV420SP(int[] rgba, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                // rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
                // 0xff00) | ((b >> 10) & 0xff);
                // rgba, divide 2^10 ( >> 10)
                rgba[yp] = ((r << 14) & 0xff000000) | ((g << 6) & 0xff0000)
                        | ((b >> 2) | 0xff00);
            }
        }
    }

    public void decodeYUV420SPv2(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0; else if (r > 262143) r = 262143;
                if (g < 0) g = 0; else if (g > 262143) g = 262143;
                if (b < 0) b = 0; else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    public static void decodeYUV420SPv3(int[] rgba, byte[] yuv420sp, int width, int height) {


        final int frameSize = width * height;
// define variables before loops (+ 20-30% faster algorithm o0`)
        int r, g, b, y1192, y, i, uvp, u, v;
        for (int j = 0, yp = 0; j < height; j++) {
            uvp = frameSize + (j >> 1) * width;
            u = 0;
            v = 0;
            for (i = 0; i < width; i++, yp++) {
                y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                y1192 = 1192 * y;
                r = (y1192 + 1634 * v);
                g = (y1192 - 833 * v - 400 * u);
                b = (y1192 + 2066 * u);

// Java's functions are faster then 'IFs'
                r = Math.max(0, Math.min(r, 262143));
                g = Math.max(0, Math.min(g, 262143));
                b = Math.max(0, Math.min(b, 262143));

                // rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
                // 0xff00) | ((b >> 10) & 0xff);
                // rgba, divide 2^10 ( >> 10)
                rgba[yp] = ((r << 14) & 0xff000000) | ((g << 6) & 0xff0000)
                        | ((b >> 2) | 0xff00);
            }
        }
    }

    public static byte[] swapYV12toI420(byte[] yv12bytes, int _Width, int _Height)
    {
        byte[] i420bytes = new byte[yv12bytes.length];
        for (int i = 0; i < _Width * _Height; i++)
            i420bytes[i] = yv12bytes[i];
        for (int i = _Width * _Height; i < _Width * _Height + (_Width / 2 * _Height / 2); i++)
            i420bytes[i] = yv12bytes[i + (_Width / 2 * _Height / 2)];
        for (int i = _Width * _Height + (_Width / 2 * _Height / 2); i < _Width * _Height + 2 * (_Width / 2 * _Height / 2); i++)
            i420bytes[i] = yv12bytes[i - (_Width / 2 * _Height / 2)];
        return i420bytes;
    }

    public static void swapNV21_NV12(byte[] yuv, int _Width, int _Height)
    {
        int length = 0;
        if (yuv.length % 2 == 0)
            length = yuv.length;
        else
            length = yuv.length - 1; //for uneven we need to shorten loop because it will go out of bounds because of i1 += 2

        for (int i1 = 0; i1 < length; i1 += 2)
        {
            if (i1 >= _Width * _Height)
            {
                byte tmp = yuv[i1];
                yuv[i1] = yuv[i1 + 1];
                yuv[i1 + 1] = tmp;
            }
        }
    }

}
