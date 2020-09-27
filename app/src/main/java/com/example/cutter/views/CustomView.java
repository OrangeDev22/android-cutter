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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cutter.CropModel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class CustomView extends androidx.appcompat.widget.AppCompatImageView {
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
    final int rectMode = 0, circleMode = 1,freeStyle=2;
    int mode=0;
    private onImageCroppedListener onImageCroppedListener;
    public CustomView(@NonNull Context context) {
        super(context);
        finalContext = context;

        init(null);
    }

    public CustomView(@NonNull Context context, @Nullable AttributeSet attrs) {
       this(context,attrs,0);
       finalContext = context;
    }

    public CustomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
                /*if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHHOLD) {

                    cropModelArrayList.clear();
                    int cx = (screen_width - bmp.getWidth()) >> 1;
                    int cy = (screen_height - bmp.getHeight()) >> 1;
                    canvas.drawBitmap(bmp, cx, cy, null);
                    im_crop_image_view.setImageBitmap(alteredBitmap);

                } else {
                    if (upx != upy) {
                        upx = event.getX();
                        upy = event.getY();


                        canvas.drawLine(downx, downy, upx, upy, paint);
                        clipPath.lineTo(upx, upy);
                        im_crop_image_view.invalidate();

                        crop();
                    }

                }*/
                break;
        }

    }

   /* @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        float strokeWidth = (int)(width*0.00694);
        Log.e("stroke width",strokeWidth+"");
        mPaint.setStrokeWidth(strokeWidth);
        super.onSizeChanged(w, h, oldw, oldh);
    }*/

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
                else{
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
                        onImageCroppedListener.onImageCropped(bitmapToArray(outPut));
                    }
                    else{
                        Log.e("Shape status","Shape dimensions are null");
                    }
                }
                else{

                    upx = event.getX();
                    upy = event.getY();
                    Bitmap outPut = cropImageFreeStyle(bitmap);
                    onImageCroppedListener.onImageCropped(bitmapToArray(outPut));
                    cropModelArrayList.clear();

                }
                drawing = false;
               /* Intent intent = new Intent(getContext(), DisplayActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("bitmap_cropped",byteArray);
                invalidate();
                final Context contextFinal = CustomView.this.getContext();
                contextFinal.startActivity(intent);*/
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
    public void setMainBitmap(Bitmap bitmap){
        mainBitmap = bitmap;

    }
    private byte[] bitmapToArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50,stream);
        byte[] byteArray = stream.toByteArray();
        invalidate();
        return  byteArray;
    }
    private Bitmap cropImage(@NonNull Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        /*int width = rect.width();
        int height = rect.height();*/
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
            //canvas.drawRect(Math.min(mStartX, mEndX),Math.min(mStartY,mEndY),Math.max(mEndX,mStartX),Math.max(mEndY,mStartY),paint);
            //

            //canvas.drawRect(mStartX,mStartY,mEndX,mEndY,paint);
            //Log.e("las time in crop", rect.width()+"x"+rect.height());
            //canvas.drawRect(rect, paint);
            //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            int cy2 = (rect.height()-bitmap.getHeight()) >> 1;

            //canvas.drawColor(Color.RED);
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
        //Bitmap output = bitmap;

        /*Paint cpaint = new Paint();
       cpaint.setAntiAlias(true);
        cpaint.setColor(getResources().getColor(R.color.colorAccent));
        cpaint.setAlpha(100);*/
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

}
