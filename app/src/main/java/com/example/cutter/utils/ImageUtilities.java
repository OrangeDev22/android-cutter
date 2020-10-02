package com.example.cutter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

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
    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) ;
        float scaleHeight = ((float) newHeight);
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        //Bitmap.createBitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
    public static int dpToPx(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
    public static Bitmap createSquaredBitmap(Bitmap srcBmp,int newWidth) {

        Bitmap dstBmp = Bitmap.createBitmap(newWidth, newWidth, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBmp);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(srcBmp, (newWidth - srcBmp.getWidth()) / 2, (newWidth - srcBmp.getHeight()) / 2, null);

        //Rect rect = new Rect(0,0,dstBmp.getWidth(), dstBmp.getHeight());
        //canvas.drawBitmap(srcBmp,null);
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
    /*public static String encodeImage(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        try{
            image.compress(compressFormat, quality, byteArrayOS);
        }catch (Exception e){
            e.printStackTrace();
        }
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }*/
    public static String encodeImage(Bitmap image, Bitmap.CompressFormat compressFormat, int quality){
        String directory = Environment.getExternalStorageDirectory()+"/inpaint/";
        File f3=new File(directory);
        if(!f3.exists())
            f3.mkdirs();
        OutputStream outStream = null;
        File file = new File(Environment.getExternalStorageDirectory() + "/inpaint/"+"seconds"+".png");
        String imageTempPath = "";
        try {
            outStream = new FileOutputStream(file);
            image.compress(compressFormat, quality, outStream);
            outStream.close();
            imageTempPath = file.getPath();
            Log.e("temp file","Image Saved");
            //Toast.makeText(, "", Toast.LENGTH_SHORT).show();.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageTempPath;
    }

    public static Bitmap decodeImage(String input) {
        Log.e("bitmap temp",input);
        Bitmap bitmap = BitmapFactory.decodeFile(input);
        return bitmap;
    }
    public static void saveAsFile(String format,Bitmap bitmap,Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root, "/noPornPictures");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = File.separator + "Image-" + n + format;
        File file = new File(myDir + "" + fname);
        Log.i("TAG", "" + file);
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, out);
            out.flush();
            out.close();
            MediaScannerConnection.scanFile(context,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {

                        @Override
                        public void onScanCompleted(String path, Uri uri) {

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
