package com.app.growbabygrow.Classes.Bigflake;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;


//See http://b.android.com/37769 for a discussion of input format pitfalls.
//See http://b.android.com/37769 for a discussion of input format pitfalls.
//See http://b.android.com/37769 for a discussion of input format pitfalls.
//See http://b.android.com/37769 for a discussion of input format pitfalls.

import com.app.growbabygrow.Classes.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.width;
import static com.app.growbabygrow.Classes.VideoUtils.encodeYUV420SP;
import static junit.framework.Assert.fail;

public class EncoderMuxer
{

    private static String TAG = "EncoderMuxer";


    private static String _Filepath;

    //  parameters for the encoder
    private static String MIME_TYPE = "video/avc";

    //  H.264 Advanced Video Coding
    private static int _frameRate;

    //  15fps
    private static int IFRAME_INTERVAL = 1;

    //  size of a frame, in pixels
    private int _Width = -1;

    private int _Height = -1;

    //  bit rate, in bits per second
    private int _BitRate = -1;

    //  encoder / muxer state
    private MediaCodec _Encoder;

    //private CodecInputSurface mInputSurface;

    private MediaMuxer _Muxer;

    private int _TrackIndex;

    private boolean _MuxerStarted;

    //  allocate one of these up front so we don't need to do it every time

    private ArrayList<Bitmap> _ByteBuffers;

//    private static MediaCodecCapabilities _SelectedCodecColor;
//
  // private static ImageFormat _CameraColorFormat = ImageFormat.NV21; //ImageFormatType NV21 or YV12 should be the image formats all Android cameras save under ?nv21 should always work i think?


    public EncoderMuxer(int width, int height, int bitRate, int framerate, String oFilePath, ArrayList<Bitmap> byteBuffers)
    {
        _Width = width;
        _Height = height;
        _BitRate = bitRate;
        _Filepath = oFilePath;
        _ByteBuffers = byteBuffers;
        _frameRate = framerate;
    }

    public void EncodeVideoToMp4()
    {
        try
        {
            PrepareEncoder();
            EncodeMux();
        }
        catch (Exception e)
        {
            Log.d(TAG, "Encoder & Mux failed", e);
        }
        finally
        {
            //  release encoder, muxer
            releaseEncoder();
        }
    }

    private void PrepareEncoder()
    {
        MediaCodecInfo codecInfo = selectCodec(MIME_TYPE);

        if (codecInfo == null)
        {
            return;
        }

        int colorFormat;
        try
        {
            colorFormat = selectColorFormat(codecInfo, MIME_TYPE);
        }
        catch (Exception e)
        {
            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        }

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, _Width, _Height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        format.setInteger(MediaFormat.KEY_BIT_RATE, _BitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, _frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        try {
            _Encoder = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        _Encoder.configure(format, null, null,  MediaCodec.CONFIGURE_FLAG_ENCODE);

        _Encoder.start();

        //  Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        //  because our MediaFormat doesn't have the Magic Goodies.  These can only be
        //  obtained from the encoder after it has started processing data.
        try
        {
            _Muxer = new MediaMuxer(_Filepath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        }
        catch (Exception e)
        {
            Log.w(TAG, e.getMessage(), e);

        }

        _TrackIndex = -1;
        _MuxerStarted = false;
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    private static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        fail("couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return 0;   // not reached
    }

    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    private void releaseEncoder()
    {
        if (_Encoder != null)
        {
            _Encoder.stop();
            _Encoder.release();
            _Encoder = null;
        }

        if (_Muxer != null && _MuxerStarted)
        {
            _Muxer.stop();
            _Muxer.release();
            _Muxer = null;
        }
    }

    private static long computePresentationTime(int frameIndex)
    {
        long value = frameIndex;
        return 132 + (value * (1000000 / _frameRate));
    }

    private void EncodeMux()
    {
        int TIMEOUT_USEC = 10000;

        ByteBuffer[] encoderInputBuffers = _Encoder.getInputBuffers();

        Boolean inputDone = false;
        int frameIndex = 0;
        try
        {
            while (true)
            {
                if (!inputDone)
                {
                    int inputBufIndex = _Encoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex >= 0)
                    {
                        long ptsUsec = computePresentationTime(frameIndex);
                        if (frameIndex == _ByteBuffers.size())
                        {
                            //  Send an empty frame with the end-of-stream flag set.  If we set EOS on a frame with data, that frame data will be ignored, and the output will be short one frame.
                            _Encoder.queueInputBuffer(inputBufIndex, 0, 0, ptsUsec, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                            Log.w(TAG, "sent input EOS (with zero-length frame)");
                        }
                        else
                        {

                            ByteBuffer inputBuf = encoderInputBuffers[inputBufIndex];
                            Bitmap b = _ByteBuffers.get(frameIndex);
                            int chunkSize = 0;

                            if (b == null)
                            {
                                //Log.d(TAG, String.Format("Adding _ByteBuffers image index {0} to encoder", frameIndex));
                            }
                            else
                            {

                                byte[] yuv = new byte[b.getWidth() * b.getHeight() * 3 / 2];
                                int[] argb = new int[b.getWidth() * b.getHeight()];
                                b.getPixels(argb, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());

                                //this conversion will vary depending on device and what the codec wants as input for color format, see selectColorFormat above
                                //that's why i need to use a surface and not add bitmaps to buffer
                                encodeYUV420SP(yuv, argb, b.getWidth(), b.getHeight());

                                inputBuf.put(yuv);

                                chunkSize = yuv.length;

                                //yuv = null;
                                //GC.Collect(); //essential to fix memory leak from new YuvImage allocation above
                            }


                            //  the buffer should be sized to hold one full frame
                            inputBuf.clear();
                            _Encoder.queueInputBuffer(inputBufIndex, 0, chunkSize, ptsUsec, 0);
                            frameIndex++;

                        }
                        //b.recycle();
                    }
                    else
                    {
                        //  either all in use, or we timed out during initial setup
                        Log.d(TAG, "input buffer not available");
                    }

                }

                ByteBuffer[] encoderOutputBuffers = _Encoder.getOutputBuffers();
                MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

                int encoderStatus = _Encoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);

                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
                {
                    Log.d(TAG, "no output available, spinning to await EOS");
                }
                else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
                {
                    //  not expected for an encoder
                    Log.d(TAG, "not expected OutputBuffersChanged happened");
                    encoderOutputBuffers = _Encoder.getOutputBuffers();
                }
                else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                {
                    //  should happen before receiving buffers, and should only happen once
                    if (_MuxerStarted)
                    {
                        Log.e(TAG, "format changed twice and should never happen");
                        throw new RuntimeException("format changed twice");
                    }

                    MediaFormat newFormat = _Encoder.getOutputFormat();

                    Log.d(TAG, "format changed and starting MUX");
                    _TrackIndex = _Muxer.addTrack(newFormat);
                    _Muxer.start();
                    _MuxerStarted = true;
                }
                else if (encoderStatus < 0)
                {
                    Log.d(TAG, "unexpected but lets ignore");
                    //  let's ignore it
                }
                else
                {
                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                    if (encodedData == null)
                    {
                        Log.e(TAG, String.format("encoderOutputBuffer {0} was null!!", encoderStatus));
                        throw new RuntimeException(String.format("encoderOutputBuffer {0} was null!!", encoderStatus));
                    }

                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
                    {
                        //  The codec config data was pulled out and fed to the muxer when we got
                        //  the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        mBufferInfo.size = 0;
                    }

                    if (mBufferInfo.size != 0)
                    {
                        if (!_MuxerStarted)
                        {
                            Log.e(TAG, "muxer hasnt started!!");
                            throw new RuntimeException("muxer hasnt started");
                        }

                        //  adjust the ByteBuffer values to match BufferInfo (not needed?) old
                        //encodedData.Position(mBufferInfo.Offset);
                        //encodedData.Limit(mBufferInfo.Offset + this.mBufferInfo.Size);

                        _Muxer.writeSampleData(_TrackIndex, encodedData, mBufferInfo);
                        Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                    }

                    _Encoder.releaseOutputBuffer(encoderStatus, false);
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
                    {
                        Log.d(TAG, "End of Stream Reached!!");
                        break;
                    }

                }

            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "Decode or Muxer failed", e);

        }
    }

    //used for all possible cases of color correction accounting for discrepencies between android camera saved images and codec color formats
    //navigate to http://bigflake.com/mediacodec/ & see question 5 at the bottom

//    private static void colorcorrection(ref byte[] yuv, int width, int height)
//    {
//        string codecformat;
//
//        switch (_SelectedCodecColor)
//        {
//            case MediaCodecCapabilities.Formatyuv420semiplanar: //NV12
//            case MediaCodecCapabilities.Formatyuv420packedsemiplanar: //NV12
//            case MediaCodecCapabilities.TiFormatyuv420packedsemiplanar: //NV12
//                codecformat = "NV12";
//                break;
//            case MediaCodecCapabilities.Formatyuv420planar: //I420
//            case MediaCodecCapabilities.Formatyuv420packedplanar: //I420
//                codecformat = "I420";
//                break;
//            default:
//                codecformat = null;
//                break;
//        }
//
//        if (codecformat == "NV12" &&  _CameraColorFormat == ImageFormatType.Nv21) //works as tested on pixel
//        {
//            Utils.swapNV21_NV12(ref yuv, width, height);
//        }
//        else if (codecformat == "I420" && _CameraColorFormat == ImageFormatType.Nv21) //not tested on device that has this config so not sure if it works
//        {
//            //if codeec is I420 it might be easier to convert from yv12 as seen below, maybe try to switch cam output to YV12? and do below conversion?
//            throw new NotImplementedException();
//        }
//        else if (codecformat == "I420" && _CameraColorFormat == ImageFormatType.Yv12) //not tested on device that has this config so not sure if it works
//        {
//            yuv = Utils.swapYV12toI420(yuv, width, height);
//        }
//        else if (codecformat == "NV12" && _CameraColorFormat == ImageFormatType.Yv12) //not tested on device that has this config so not sure if it works
//        {
//            //find conversion and put it here you shit
//            throw new NotImplementedException();
//        }
//
//    }





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



}


