package com.example.cutter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.example.cutter.constants.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

public class ImageUtilities {
    public ImageUtilities(){

    }
    public Bitmap rotateBitmap(Bitmap bitmap, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(),true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }
    public Bitmap flipBitmap(Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.postScale(-1,1,0,0);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    public static Bitmap createSquaredBitmap(Bitmap srcBmp,int newWidth) {

        Bitmap dstBmp = Bitmap.createBitmap(newWidth, newWidth, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBmp);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(srcBmp, (newWidth - srcBmp.getWidth()) / 2, (newWidth - srcBmp.getHeight()) / 2, null);

        return dstBmp;
    }
    public static Bitmap trim(Bitmap source) {
        int firstX = 0, firstY = 0;
        int lastX = source.getWidth();
        int lastY = source.getHeight();
        int[] pixels = new int[source.getWidth() * source.getHeight()];
        source.getPixels(pixels, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());
        loop:
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstX = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = firstX; x < source.getWidth(); x++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstY = y;
                    break loop;
                }
            }
        }
        loop:
        for (int x = source.getWidth() - 1; x >= firstX; x--) {
            for (int y = source.getHeight() - 1; y >= firstY; y--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastX = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = source.getHeight() - 1; y >= firstY; y--) {
            for (int x = source.getWidth() - 1; x >= firstX; x--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastY = y;
                    break loop;
                }
            }
        }
        return Bitmap.createBitmap(source, firstX, firstY, lastX - firstX, lastY - firstY);
    }
    public static  ByteArrayOutputStream  bitmapToArray(Bitmap bitmap){
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
        return bs;
    }

    public static String encodeImage(Bitmap image, Bitmap.CompressFormat compressFormat, int quality,Context context){
        String directory = context.getFilesDir()+"/inpaint/";
        File f3=new File(directory);
        if(!f3.exists())
            f3.mkdirs();
        OutputStream outStream = null;
        File file = new File(context.getFilesDir() + "/inpaint/"+"temp"+".png");
        String imageTempPath = "";
        try {
            outStream = new FileOutputStream(file);
            image.compress(compressFormat, quality, outStream);
            outStream.close();
            imageTempPath = file.getPath();
            Log.d("temp_image_file","Image Saved");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageTempPath;
    }

    public static Bitmap decodeImage(String input) {
        Log.d("decoding_bitmap_path:",input);
        Bitmap bitmap = BitmapFactory.decodeFile(input);
        return bitmap;
    }
    public static void saveAsFile(Bitmap.CompressFormat compressFormat, Bitmap image, int quality,Context context) {
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String directory = Constants.savedImagesPath;
        File f3=new File(directory);
        if(!f3.exists())
            f3.mkdirs();
        OutputStream outStream;
        File file = new File(Constants.savedImagesPath+"/"+n+".png");
        try {
            outStream = new FileOutputStream(file);
            image.compress(compressFormat, quality, outStream);
            outStream.close();
            Log.d("temp file","Image Saved");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaScannerConnection.scanFile(context,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }
    public static Bitmap changeBitmapSaturation(Bitmap src, float settingSat) {

        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap bitmapResult =
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvasResult = new Canvas(bitmapResult);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(settingSat);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvasResult.drawBitmap(src, 0, 0, paint);

        return bitmapResult;
    }

    public static Bitmap changeBitmapContrast(Bitmap bmp, float contrast)
    {
        float translate = (-.5f * contrast + .5f) * 255.f;
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, translate,
                        0, contrast, 0, 0, translate,
                        0, 0, contrast, 0, translate,
                        0, 0, 0, 1, 0
                });
        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public static Bitmap changeBitmapBrightness(Bitmap mBitmap, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        1, 0, 0, 0, brightness,
                        0, 1, 0, 0, brightness,
                        0, 0, 1, 0, brightness,
                        0, 0, 0, 1, 0
                });
        Bitmap mEnhancedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap
                .getConfig());
        Canvas canvas = new Canvas(mEnhancedBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(mBitmap, 0, 0, paint);
        return mEnhancedBitmap;
    }

    public static Bitmap changeBitmapVignette(Bitmap bm, int p, int blackIntensity){
        Bitmap image = Bitmap.createBitmap(bm.getWidth(),bm.getHeight(), Bitmap.Config.ARGB_8888);
        int rad;
        Canvas canvas = new Canvas(image);
        canvas.drawBitmap(bm, 0, 0, new Paint());
        if(bm.getWidth()<bm.getHeight()){
            int o = (bm.getHeight()*2)/100;
            Log.d("_vignette_oval_and_bm_height",o+"x"+bm.getHeight());
            rad = bm.getHeight() - o*p/5;
        }else{
            int o = (bm.getWidth()*2)/100;
            Log.d("_vignette_oval_and_bm_width",o+"x"+bm.getWidth());
            rad = bm.getWidth() - o*p/5;
        }
        Rect rect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
        RectF rectf = new RectF(rect);
        int[] colors;
        colors = new int[] { 0, 0, blackIntensity };
        float[] pos = new float[] { 0.0f, 0.1f, 1.0f };
        Shader linGradLR = new RadialGradient(rect.centerX(), rect.centerY(),rad, colors, pos, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(linGradLR);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setAlpha(255);
        canvas.drawRect(rectf, paint);
        return image;
    }
}
