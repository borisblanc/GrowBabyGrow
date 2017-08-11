package com.app.growbabygrow.Classes;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;

import android.util.Log;
import android.util.SparseIntArray;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import org.jcodec.api.android.SequenceEncoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static android.util.Log.VERBOSE;


public class VideoUtils {


    private static final String LOGTAG = "VideoUtils";
    private static final int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024;

    /**
     * Remove the sound track.
     */
//    public static void startMute(String filePath, SaveVideoFileInfo dstFileInfo) throws IOException {
//            genVideoUsingMuxer(filePath, dstFileInfo.mFile.getPath(), -1.0, -1.0, false, true);
//    }

    public static void TrimVideo(String srcPath, String dstPath, Long startMs, Long endMs, boolean useAudio, boolean useVideo)
    {
        String TAG = ".TrimVideo";
        try {
            genTrimVideoUsingMuxer(srcPath,  dstPath,  startMs,  endMs,  useAudio, useVideo);
        } catch (IOException e) {
            Log.d(LOGTAG + TAG, e.getMessage());
        }
    }


    /**
     * @param srcPath  the path of source video file.
     * @param dstPath  the path of destination video file.
     * @param startMs  starting time in milliseconds for trimming. Set to
     *                 negative if starting from beginning.
     * @param endMs    end time for trimming in milliseconds. Set to negative if
     *                 no trimming at the end.
     * @param useAudio true if keep the audio track from the source.
     * @param useVideo true if keep the video track from the source.
     * @throws IOException
     */
    public static void genTrimVideoUsingMuxer(String srcPath, String dstPath, Long startMs, Long endMs, boolean useAudio, boolean useVideo) throws IOException {
        // Set up MediaExtractor to read from the source.
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(srcPath);
        int trackCount = extractor.getTrackCount();
        // Set up MediaMuxer for the destination.
        MediaMuxer muxer;
        muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>(trackCount);
        int bufferSize = -1;
        for (int i = 0; i < trackCount; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            boolean selectCurrentTrack = false;
            if (mime.startsWith("audio/") && useAudio) {
                selectCurrentTrack = true;
            } else if (mime.startsWith("video/") && useVideo) {
                selectCurrentTrack = true;
            }
            if (selectCurrentTrack) {
                extractor.selectTrack(i);
                int dstIndex = muxer.addTrack(format);
                indexMap.put(i, dstIndex);
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    bufferSize = newSize > bufferSize ? newSize : bufferSize;
                }
            }
        }
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        // Set up the orientation and starting time for extractor.
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        retrieverSrc.setDataSource(srcPath);
        String degreesString = retrieverSrc.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (degreesString != null) {
            int degrees = Integer.parseInt(degreesString);
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees);
            }
        }
        if (startMs > 0) {
            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        int offset = 0;
        int trackIndex = -1;
        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
        BufferInfo bufferInfo = new BufferInfo();
        muxer.start();
        while (true) {
            bufferInfo.offset = offset;
            bufferInfo.size = extractor.readSampleData(dstBuf, offset);
            if (bufferInfo.size < 0) {
                Log.d(LOGTAG, "Saw input EOS.");
                bufferInfo.size = 0;
                break;
            } else {
                bufferInfo.presentationTimeUs = extractor.getSampleTime();
                if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {
                    Log.d(LOGTAG, "The current sample is over the trim end time.");
                    break;
                } else {
                    bufferInfo.flags = extractor.getSampleFlags();
                    trackIndex = extractor.getSampleTrackIndex();
                    muxer.writeSampleData(indexMap.get(trackIndex), dstBuf, bufferInfo);
                    extractor.advance();
                }
            }
        }
        muxer.stop();
        muxer.release();
    }

    //works good for similar videos only, Must have same fps, resolution and codec, might not need it if replacement below is fast enough (MuxMergeVideos)
    public static void Mp4ParserMergeVideos(String outputfilepath, String FirstVideoPath, String SecondVideoPath) {

        Movie[] clips = new Movie[2];
        try {
            //location of the movie clip storage
            //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TestMerge");

            //Build the two clips into movies

            Movie firstClip = MovieCreator.build(FirstVideoPath);
            Movie secondClip = MovieCreator.build(SecondVideoPath);

            //Add both movie clips
            clips[0] = firstClip;
            clips[1] = secondClip;

            //List for audio and video tracks
            List<Track> videoTracks = new LinkedList<>();
            List<Track> audioTracks = new LinkedList<>();

            //Iterate all the movie clips and find the audio and videos
            for (Movie movie : clips) {
                for (Track track : movie.getTracks()) {
                    if (track.getHandler().equals("soun"))
                        audioTracks.add(track);
                    if (track.getHandler().equals("vide"))
                        videoTracks.add(track);
                }
            }

            //Result movie from putting the audio and video together from the two clips
            Movie result = new Movie();

            //Append all audio and video
            if (videoTracks.size() > 0)
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));

            if (audioTracks.size() > 0)
                result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));

            //Output the resulting movie to a new mp4 file
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String outputLocation = mediaStorageDir.getPath() + timeStamp;
            Container out = new DefaultMp4Builder().build(result);
            FileChannel fc = new RandomAccessFile(String.format(outputfilepath), "rw").getChannel();

            out.writeContainer(fc);

            fc.close();
        }
        catch (IOException e) {
            Log.d(LOGTAG, "Merge Error " + e.getMessage());
        }

    }

    //this one works better than Mp4ParserMergeVideos for some different video types, but they still need to have same resolution might not be as fast though
    public static boolean MuxMergeVideos(File dst, File... sources) {

        int MAX_SAMPLE_SIZE = 1000000; //not sure best value for this yet
        int APPEND_DELAY = 0; //not sure best value for this yet

        if ((sources == null) || (sources.length == 0)) {
            return false;
        }

        boolean result;
        MediaExtractor extractor = null;
        MediaMuxer muxer = null;
        try {
            // Set up MediaMuxer for the destination.
            muxer = new MediaMuxer(dst.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            // Copy the samples from MediaExtractor to MediaMuxer.
            boolean sawEOS = false;
            int bufferSize = MAX_SAMPLE_SIZE; //MAX_SAMPLE_SIZE;
            int frameCount = 0;
            int offset = 100;

            ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            long timeOffsetUs = 0;
            int dstTrackIndex = -1;

            for (int fileIndex = 0; fileIndex < sources.length; fileIndex++) {
                int numberOfSamplesInSource = getNumberOfSamples(sources[fileIndex]);
//                if (VERBOSE) {
//                    Log.d(TAG, String.format("Source file: %s", sources[fileIndex].getPath()));
//                }

                // Set up MediaExtractor to read from the source.
                extractor = new MediaExtractor();
                extractor.setDataSource(sources[fileIndex].getPath());

                // Set up the tracks.
                SparseIntArray indexMap = new SparseIntArray(extractor.getTrackCount());
                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    extractor.selectTrack(i);
                    MediaFormat format = extractor.getTrackFormat(i);
                    if (dstTrackIndex < 0) {
                        dstTrackIndex = muxer.addTrack(format);
                        muxer.start();
                    }
                    indexMap.put(i, dstTrackIndex);
                }

                long lastPresentationTimeUs = 0;
                int currentSample = 0;
                sawEOS = false;

                while (!sawEOS) {
                    bufferInfo.offset = offset;
                    bufferInfo.size = extractor.readSampleData(dstBuf, offset);

                    if (bufferInfo.size < 0) {
                        sawEOS = true;
                        bufferInfo.size = 0;
                        timeOffsetUs += (lastPresentationTimeUs + APPEND_DELAY);
                    }
                    else {
                        lastPresentationTimeUs = extractor.getSampleTime();
                        bufferInfo.presentationTimeUs = extractor.getSampleTime() + timeOffsetUs;
                        bufferInfo.flags = extractor.getSampleFlags();
                        int trackIndex = extractor.getSampleTrackIndex();

                        if ((currentSample < numberOfSamplesInSource) || (fileIndex == sources.length - 1)) {
                            muxer.writeSampleData(indexMap.get(trackIndex), dstBuf, bufferInfo);
                        }
                        extractor.advance();

                        frameCount++;
                        currentSample++;
//                        if (VERBOSE) {
//                            Log.d(TAG, "Frame (" + frameCount + ") " +
//                                    "PresentationTimeUs:" + bufferInfo.presentationTimeUs +
//                                    " Flags:" + bufferInfo.flags +
//                                    " TrackIndex:" + trackIndex +
//                                    " Size(KB) " + bufferInfo.size / 1024);
//                        }
                    }
                }
                extractor.release();
                extractor = null;
            }

            result = true;
        }
        catch (IOException e) {
            result = false;
        }
        finally {
            if (extractor != null) {
                extractor.release();
            }
            if (muxer != null) {
                muxer.stop();
                muxer.release();
            }
        }
        return result;
    }


    public static int getNumberOfSamples(File src) {
        MediaExtractor extractor = new MediaExtractor();
        int result;
        try {
            extractor.setDataSource(src.getPath());
            extractor.selectTrack(0);

            result = 0;
            while (extractor.advance()) {
                result ++;
            }
        }
        catch(IOException e) {
            result = -1;
        }
        finally {
            extractor.release();
        }
        return result;
    }

    public static Bitmap drawTextToBitmap(Context gContext, int gResId, String gText, int width, int height, int textsize)
    {
        Resources resources = gContext.getResources();
        //float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);

        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.WHITE);
        // text size in pixels
        paint.setTextSize(textsize);
        // text shadow
        //paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

    //uses jcodec might need to find way to do this with phones codecs instead
    public static void CreatevideoFromBitmaps(File fullpath, ArrayList<Bitmap> bitmaps, int numofframes) {
        try {
            SequenceEncoder encoder = new SequenceEncoder(fullpath);
            for(Bitmap bmap: bitmaps)
            {
                for (int i = 1; i <= numofframes; i++) {
                    encoder.encodeImage(bmap);
                }

                bmap.recycle();
            }
            encoder.finish();

        } catch (IOException e) {
            Log.d(LOGTAG, "CreatevideoFromBitmap Error " + e.getMessage());
        }
    }


    public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                // a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }

                index ++;
            }
        }
    }







}

