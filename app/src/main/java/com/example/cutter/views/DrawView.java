package com.example.cutter.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cutter.CropModel;
import com.example.cutter.utils.ImageUtilities;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class DrawView extends androidx.appcompat.widget.AppCompatImageView {
    private static final float COLOR_TOLERANCE = 20;
    int mStartX;
    int mStartY;
    int mEndX;
    int mEndY;
    int width;
    int height;
    //FREEE STYLE
    android.graphics.Path clipPath;
    float downx = 0;
    float downy = 0;
    float tdownx = 0;
    float tdowny = 0;
    float upx = 0;
    float upy = 0;
    long lastTouchDown = 0;
    int CLICK_ACTION_THRESHHOLD = 100;
    ArrayList<CropModel> cropModelArrayList;
    float smallx,smally,largex,largey;
    //
    private float angle;
    Paint mPaint = new Paint();
    int mSelectedColor = Color.parseColor("#0099ff");
    boolean drawing = false;
    public Rect rect;
    private RectF rectF;
    public Bitmap bitmap,mainBitmap;
    Context finalContext;
    final int rectMode = 0, circleMode = 1,freeStyle=2,autoClearMode=3;
    int mode=0;
    private onImageCroppedListener onImageCroppedListener;
    public DrawView(@NonNull Context context) {
        super(context);
        finalContext = context;

        init(null);
    }

    public DrawView(@NonNull Context context, @Nullable AttributeSet attrs) {
       this(context,attrs,0);
       finalContext = context;
    }

    public DrawView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        finalContext = context;
        cropModelArrayList = new ArrayList<>();
        mPaint.setColor(mSelectedColor);

        mPaint.setStyle(Paint.Style.STROKE);
        //mPaint.setPathEffect(new DashPathEffect(new float[]{15.0f, 15.0f}, 0));
        setFocusable(true);
        //init(attrs);
    }
    private void init(@NonNull AttributeSet set){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       switch (mode){
            case rectMode:
               rect = new Rect();
                rect.left = mStartX;
                rect.top = mStartY;
                rect.right = mEndX;
                rect.bottom = mEndY;
                Log.e("las time in draw", mStartX+"x"+mStartY+"x"+mEndX+"x"+mEndY);
                //Log.e("las time in draw", rect.width()+"x"+rect.height());
                //rect = new Rect(Math.min(mStartX, mEndX),Math.min(mStartY,mEndY),Math.max(mEndX,mStartX),Math.max(mEndY,mStartY));
                if(drawing){
                    //canvas.drawRect(mStartX,mStartY,Math.max(mEndX,mStartX),Math.max(mEndY,mStartY),mPaint);
                    //Log.e("las time in draw", rect.width()+"x"+rect.height());
                   canvas.drawRect(rect, mPaint);
                }
                else{
                    canvas.drawColor(Color.TRANSPARENT);
                }
                break;
            case circleMode:
                rectF = new RectF();
                rectF.left = mStartX;
                rectF.top = mStartY;
                rectF.right = mEndX;
                rectF.bottom = mEndY;
                if(drawing){
                    canvas.drawOval(rectF, mPaint);
                }
                else{
                    canvas.drawColor(Color.TRANSPARENT);
                }
                break;
            case freeStyle:
               if(drawing){
                   canvas.drawPath(clipPath, mPaint);
                   Log.e("canvas status","drawing line");
                   if(!drawing){
                       canvas.drawLine(downx, downy, upx, upy, mPaint);
                       clipPath.lineTo(upx, upy);
                   }
               }
               else{
                   canvas.drawColor(Color.TRANSPARENT);
               }

                break;
        }

    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        height = View.MeasureSpec.getSize(heightMeasureSpec);
        float strokeWidth = (int)(width*0.00694);
        //Log.e("stroke width",strokeWidth+"");
        Log.e("Height on custom view",height+"");
        mPaint.setPathEffect(new DashPathEffect(new float[]{15.0f, 15.0f}, 0));
        mPaint.setStrokeWidth(strokeWidth);
        setMeasuredDimension(width,height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                if(mode == rectMode){
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    rect = new Rect();
                }
                else if (mode == circleMode){
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    rectF = new RectF();
                }
                else if(mode == freeStyle){
                    downx = event.getX();
                    downy = event.getY();
                    clipPath = new android.graphics.Path();
                    clipPath.moveTo(downx, downy);
                    tdownx = downx;
                    tdowny = downy;
                    smallx = downx;
                    smally = downy;
                    largex = downx;
                    largey = downy;
                    lastTouchDown = System.currentTimeMillis();
                }
                else{
                    startAutoClear(event.getX(),event.getY());
                    Log.e("Mode status","entered auto clear");
                }
                drawing = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if(mode == rectMode || mode == circleMode){
                    mEndX = (int) event.getX();
                    mEndY = (int) event.getY();
                }
                else{
                    upx = event.getX();
                    upy = event.getY();
                    //Log.e("free style cordinates", upx +"x"+ upy);
                    cropModelArrayList.add(new CropModel(upx, upy));
                    clipPath = new Path();
                    clipPath.moveTo(tdownx,tdowny);
                    for(int i = 0; i<cropModelArrayList.size();i++){
                        clipPath.lineTo(cropModelArrayList.get(i).getY(),cropModelArrayList.get(i).getX());
                    }

                    downx = upx;
                    downy = upy;
                }
                invalidate();

                break;

            case MotionEvent.ACTION_UP:
                if(mode == rectMode || mode == circleMode){
                    mEndX = (int) event.getX();
                    mEndY = (int) event.getY();
                    Log.e("final cordinates", mEndX+"x"+mEndY);
                    Log.e("rect dimensions",rect.width()+"x"+rect.height());
                    if(verifyShape()){
                        Bitmap outPut = cropImage(bitmap);
                        onImageCroppedListener.onImageCropped(ImageUtilities.bitmapToArray(outPut));
                    }
                    else{
                        Log.e("Shape status","Shape dimensions are null");
                    }
                }
                else if(mode == freeStyle){

                    upx = event.getX();
                    upy = event.getY();
                    Bitmap outPut = cropImageFreeStyle(bitmap);
                    onImageCroppedListener.onImageCropped(ImageUtilities.bitmapToArray(outPut));
                    cropModelArrayList.clear();

                }
                else{

                }
                drawing = false;
                break;

            default:

                super.onTouchEvent(event);

                break;
        }

        return true;
    }
    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }


    private Bitmap cropImage(@NonNull Bitmap bitmap){
        int cx = (this.width- bitmap.getWidth()) >> 1; // same as (...) / 2
        int cy = (this.height - bitmap.getHeight()) >> 1;
        //Log.e("dimenssions in customview",this.width+"xDD"+this.height);
        //Log.e("las time in crop", mStartX+"x"+mStartY+"x"+mEndX+"x"+mEndY);

        Bitmap output = Bitmap.createBitmap(this.width,this.height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Log.e("Canvas dims in crop",canvas.getWidth()+"x"+canvas.getHeight());
        int color = 0xff424242;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(color);

        //Rect rectangle = new Rect(0,0,,cy);

        if(mode == rectMode){

            canvas.drawRect(mStartX,mStartY,mEndX,mEndY,paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(this.bitmap,cx,cy,paint);
        }
        else{
            canvas.drawOval(rectF,paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap,cx,cy,paint);
        }



        Log.e("BITMAP SIZES","input:"+bitmap.getWidth()+"x"+bitmap.getHeight()+"output: "+output.getWidth()+"x"+output.getHeight());
        return output;
    }
    private Bitmap cropImageFreeStyle(@NonNull Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int cx = (this.width - bitmap.getWidth()) >> 1; // same as (...) / 2
        int cy = (this.height - bitmap.getHeight()) >> 1;
        Bitmap output = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(color);

        clipPath.close();
        //clipPath.setFillType(android.graphics.Path.FillType.INVERSE_WINDING);

        for(int i = 0; i<cropModelArrayList.size();i++){
            if(cropModelArrayList.get(i).getY()<smallx){

                smallx=cropModelArrayList.get(i).getY();
            }
            if(cropModelArrayList.get(i).getX()<smally){

                smally=cropModelArrayList.get(i).getX();
            }
            if(cropModelArrayList.get(i).getY()>largex){

                largex=cropModelArrayList.get(i).getY();
            }
            if(cropModelArrayList.get(i).getX()>largey){

                largey=cropModelArrayList.get(i).getX();
            }
        }

        canvas.drawPath(clipPath, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return  output;
    }
    public interface  onImageCroppedListener{
        void onImageCropped(byte[] bytes);
    }
    public void setOnImageCroppedListener(onImageCroppedListener onImageCroppedListener){
        this.onImageCroppedListener = onImageCroppedListener;
    }
    public void setMode(int mode){
        this.mode = mode;
    }

    private boolean verifyShape(){
        boolean validation = false;
        Log.e("MODE",mode+"");
        if(mode == rectMode){
            if(rect.width() != 0 || rect.height() != 0) validation = true;
            Log.e("figure dimensions", rect.width()+"x"+rect.height());
        }
        else if (mode == circleMode){
            if(rectF.width() != 0 || rectF.height() != 0) validation = true;
            Log.e("figure dimensions", rectF.width()+"x"+rectF.height());
        }
        return validation;
    }
    public void startAutoClear(float x, float y){
        Log.e("Async status","starting async");
        AutomaticPixelClearingTask automaticPixelClearingTask = new AutomaticPixelClearingTask(this);
        automaticPixelClearingTask.execute((int)x, (int)y);

    }
    private static class AutomaticPixelClearingTask extends AsyncTask<Integer, Void, Bitmap> {
        private WeakReference<DrawView> drawViewWeakReference;
        AutomaticPixelClearingTask(DrawView drawView){
            this.drawViewWeakReference = new WeakReference<>(drawView);
        }
        @Override
        protected void onPreExecute(){
            Log.e("Async status","pre execute");
            super.onPreExecute();

        }
        @Override
        protected Bitmap doInBackground(Integer... points) {
            Log.e("Async status","do in background");
            Bitmap oldBitmap = drawViewWeakReference.get().bitmap;
            int colorToReplace = oldBitmap.getPixel(points[0], points[1]);
            int width = oldBitmap.getWidth();
            int height = oldBitmap.getHeight();
            int[] pixels = new int[width*height];
            oldBitmap.getPixels(pixels,0,width,0,0,width,height);
            int rA = Color.alpha(colorToReplace);
            int rR = Color.red(colorToReplace);
            int rG = Color.green(colorToReplace);
            int rB = Color.blue(colorToReplace);
            int pixel;

            for (int y = 0; y< height;++y){
                for (int x = 0; x < width; ++x){
                    int index = y*width+x;
                    pixel = pixels[index];
                    int rrA = Color.alpha(pixel);
                    int rrR = Color.red(pixel);
                    int rrG = Color.green(pixel);
                    int rrB = Color.blue(pixel);
                    if (rA - COLOR_TOLERANCE < rrA && rrA < rA + COLOR_TOLERANCE && rR - COLOR_TOLERANCE < rrR && rrR < rR + COLOR_TOLERANCE &&
                            rG - COLOR_TOLERANCE < rrG && rrG < rG + COLOR_TOLERANCE && rB - COLOR_TOLERANCE < rrB && rrB < rB + COLOR_TOLERANCE) {
                        pixels[index] = Color.TRANSPARENT;
                    }
                }
            }

            Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            newBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return newBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap outPut) {
            super.onPostExecute(outPut);
            Log.e("Async status","post execute");
            BitmapDrawable drawable = new BitmapDrawable(drawViewWeakReference.get().finalContext.getResources(),outPut);
            drawViewWeakReference.get().onImageCroppedListener.onImageCropped(ImageUtilities.bitmapToArray(outPut));
        }
    }
}
