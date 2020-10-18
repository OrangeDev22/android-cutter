package com.example.cutter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.cutter.Interface.FiltersListFragmentListener;
import com.example.cutter.adapters.EditToolsAdapter;
import com.example.cutter.dialogs.AddFilterDailog;
import com.example.cutter.dialogs.AddStickerDialog;
import com.example.cutter.dialogs.AddTextDialog;
import com.example.cutter.dialogs.FrameDialog;
import com.example.cutter.dialogs.SubFilterDialog;
import com.example.cutter.fragments.TextEditorDialogFragment;
import com.example.cutter.tools.ToolType;
import com.example.cutter.utils.BitmapUtils;
import com.example.cutter.utils.ImageUtilities;
import com.example.cutter.views.CustomTextViewOutline;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;

import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.SubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;


import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EditActivity extends AppCompatActivity implements FrameDialog.onFrameWidthListener, AddTextDialog.TextEditor,
        AddStickerDialog.onStickerListener, EditToolsAdapter.onToolListener,
        FiltersListFragmentListener, AddFilterDailog.onDialogFilterListener, SubFilterDialog.onSubFilterListener {
    Bitmap bitmap,filteredBitmap;
    StickerView stickerView;
    //BottomNavigationView bottomNavigationView;
    private int frameWidth=0, frameColor = Color.BLUE,frameCornerDp=0,brightness=0;
    private float contrast=1.0f,saturation=1.0f;
    private List<String> stickerPaths;
    private RecyclerView bottomTools;
    private SubFilterDialog subFilterDialog;
    static{
        System.loadLibrary("NativeImageProcessor");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        stickerView = findViewById(R.id.sticker_view);
        bottomTools = findViewById(R.id.bottomMenuRecyclerView);
        EditToolsAdapter adapter = new EditToolsAdapter(this, EditActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
        bottomTools.setLayoutManager(layoutManager);
        bottomTools.setAdapter(adapter);
        /*bottomNavigationView = findViewById(R.id.bottomNavigationViewEditImage);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);*/
        String imagePath = getIntent().getExtras().getString("bitmap_CropActivity");
        bitmap = ImageUtilities.decodeImage(imagePath);
        filteredBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        stickerView.configDefaultIcons();
        stickerView.setLocked(false);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        Drawable gd = getDrawable(R.drawable.golden_deer);
        stickerView.addSticker(new DrawableSticker(gd));
        stickerView.setLocked(false);
        stickerView.setConstrained(true);
        stickerView.setBackgroundDrawable(drawable);

        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {

            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {
                Log.e("amout_of_stickers",stickerView.getStickerCount()+"");
            }

            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerDeleted(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerDragFinished(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerTouchedDown(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {

            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {

            }
        });
    }

    private void openColorPicker(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.color_picker_dialog);
        final ImageView imageView = dialog.findViewById(R.id.previewColor);
        final ImageView imageViewCloseColorPicker = dialog.findViewById(R.id.image_view_close_color_picker);
        final ColorPicker colorPicker = dialog.findViewById(R.id.colorPicker);
        //imageView.getBackground().setColorFilter(-1, PorterDuff.Mode.MULTIPLY);

        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener(){
            @Override
            public void onColorSelected(int color) {
                super.onColorSelected(color);
                imageView.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                frameColor = color;
                BitmapDrawable drawable = new BitmapDrawable(getResources(), getRoundedCornerBitmap(bitmap,color,frameCornerDp,frameWidth));
                stickerView.setBackground(drawable);
            }

            /*@Override
            public void onColorSelectionEnd(int color) {
                super.onColorSelectionEnd(color);

                Bitmap alteredBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),bitmap.getConfig());
                Canvas canvas = new Canvas(alteredBitmap);
                canvas.drawColor(color);
                canvas.drawBitmap(bitmap,0,0,null);
                BitmapDrawable drawable = new BitmapDrawable(getResources(),alteredBitmap);
                stickerView.setBackground(drawable);
            }*/
        });
        dialog.show();
        imageViewCloseColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    public void onWidthChange(int width, int color,int dpCorner) {
        frameWidth = width;
        frameColor = color;
        frameCornerDp = dpCorner;
        BitmapDrawable drawable = new BitmapDrawable(getResources(), getRoundedCornerBitmap(bitmap,color,dpCorner,width));
        stickerView.setBackground(drawable);
    }
    private Bitmap addFrameBorder(int width,int color){
        Bitmap bmpWithBorder = Bitmap.createBitmap(bitmap.getWidth()+width, bitmap.getHeight()+width, bitmap.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(color);
        canvas.drawBitmap(bitmap, width, width, null);
        /*RectF targetRect = new RectF(left+10, top+10, left + scaledWidth, top + scaledHeight);
        Bitmap dest = Bitmap.createBitmap(newWidth+20, newHeight+20, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(source, null, targetRect, null);*/
        return bmpWithBorder;
    }

    @Override
    public void onDone(String inputText, int colorCode, int backgroundColor, Typeface typeface, CustomTextViewOutline strokedEditText) {
        strokedEditText.setCursorVisible(false);
        strokedEditText.buildDrawingCache();
        float[] sharp = { -0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f,
                -0.15f, -0.15f
        };
        Bitmap bitmap = Bitmap.createBitmap(strokedEditText.getDrawingCache());
        bitmap = BitmapUtils.doSharpen(bitmap,sharp,EditActivity.this);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        DrawableSticker drawableSticker = new DrawableSticker(drawable);
        stickerView.addSticker(drawableSticker);
    }

    @Override
    public void onStickerSelected(String fileName) {
        /*int rawId = getResources().getIdentifier(fileName,"raw",getPackageName());
        /*String resourcePath = "android.resource://" + getPackageName() + "/" + rawId ;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),rawId);
        BitmapDrawable drawable = new BitmapDrawable(getResources(),bitmap);
        Drawable drawable = ContextCompat.getDrawable(this,rawId);
        stickerView.addSticker(new DrawableSticker(drawable));*/
        /*try {
            Drawable drawable = Drawable.createFromStream(getAssets().open(fileName), null);
            stickerView.addSticker(new DrawableSticker(drawable));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        BitmapDrawable drawable = new BitmapDrawable(getResources(),BitmapUtils.getBitmapFromAsset(this,fileName));
        stickerView.addSticker(new DrawableSticker(drawable));

    }
    public  Bitmap getRoundedCornerBitmap(Bitmap bitmap, int color, int cornerDips, int borderDips) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int borderSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) borderDips, getResources().getDisplayMetrics());
        final int cornerSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) cornerDips,
                getResources().getDisplayMetrics());
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        // prepare canvas for transfer
        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // draw border
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) borderSizePx);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        return output;
    }

    @Override
    public void onToolSelected(ToolType toolType) {
        switch(toolType){
            case COLOR:
                openColorPicker();
                break;
            case STICKER:
                AddStickerDialog dialogSticker = new AddStickerDialog();
                dialogSticker.show(getSupportFragmentManager(),"");
                break;
            case TEXT:
                AddTextDialog dialog = new AddTextDialog();
                dialog.show(getSupportFragmentManager(),"");
                break;
            case FRAME:
                FrameDialog frameDialog = new FrameDialog();
                frameDialog.setSelectFrameColor(frameColor);
                frameDialog.setFrameWidth(frameWidth);
                frameDialog.setFrameCorners(frameCornerDp);
                frameDialog.show(getSupportFragmentManager(),"");
                break;
            case FILTER:
                AddFilterDailog dialogFilters = new AddFilterDailog();
                dialogFilters.setBitmap(bitmap);
                dialogFilters.show(getSupportFragmentManager(),"");
                break;
            case BRIGHTNESS:
                subFilterDialog = new SubFilterDialog();
                subFilterDialog.setToolType(ToolType.BRIGHTNESS);
                subFilterDialog.show(getSupportFragmentManager(),"");
                break;
            case CONTRAST:
                subFilterDialog = new SubFilterDialog();
                subFilterDialog.setToolType(ToolType.CONTRAST);
                subFilterDialog.show(getSupportFragmentManager(),"");
                break;
            case SATURATION:
                subFilterDialog = new SubFilterDialog();
                subFilterDialog.setToolType(ToolType.SATURATION);
                subFilterDialog.show(getSupportFragmentManager(),"");
                break;
        }
    }

    @Override
    public void onFilterSelected(Filter filter) {
        filteredBitmap = this.bitmap.copy(Bitmap.Config.ARGB_8888,true);
        //filteredBitmap = filter.processFilter(filteredBitmap);
        if(frameCornerDp> 0 || frameWidth >0 ){
            stickerView.setBackground(new BitmapDrawable(getResources(), getRoundedCornerBitmap(filter.processFilter(filteredBitmap),frameColor,frameCornerDp,frameWidth)));
        }else{
            BitmapDrawable drawable = new BitmapDrawable(getResources(),filter.processFilter(filteredBitmap));
            stickerView.setBackground(drawable);
        }

    }

    @Override
    public void setFilter(Filter filter,boolean apply) {
        if(apply){
            filteredBitmap = this.bitmap.copy(Bitmap.Config.ARGB_8888,true);
            filteredBitmap = filter.processFilter(filteredBitmap);
            bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888,false);
            Bitmap temp_bitmap;
            if(frameCornerDp > 0 || frameWidth >0){
                temp_bitmap = getRoundedCornerBitmap(bitmap,frameColor,frameCornerDp,frameWidth);
            }else{
                temp_bitmap = bitmap;
            }
            BitmapDrawable drawable = new BitmapDrawable(getResources(),temp_bitmap);
            stickerView.setBackground(drawable);
        }
    }

    @Override
    public void onBrightnessChanged(int brightness) {
        this.brightness = brightness;
        Filter newFilter = new Filter();
        newFilter.addSubFilter(new BrightnessSubFilter(this.brightness));
        if(frameWidth > 0 || frameCornerDp > 0){
            stickerView.setBackground(new BitmapDrawable(getResources(),getRoundedCornerBitmap(newFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true )),frameColor,
                    frameCornerDp,frameWidth)));
        }else{
            stickerView.setBackground(new BitmapDrawable(getResources(),newFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true ))));
        }

    }

    @Override
    public void onContrastChanged(float contrast) {
        this.contrast = contrast;
        Filter newFilter = new Filter();
        newFilter.addSubFilter(new ContrastSubFilter(contrast));
        if(frameWidth > 0 || frameCornerDp > 0){
            stickerView.setBackground(new BitmapDrawable(getResources(),getRoundedCornerBitmap(newFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true )),frameColor,
                    frameCornerDp,frameWidth)));
        }else{
            stickerView.setBackground(new BitmapDrawable(getResources(),newFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true ))));
        }
    }

    @Override
    public void onSaturationChanged(float saturation) {
        this.saturation = saturation;
        Filter newFilter = new Filter();
        newFilter.addSubFilter(new SaturationSubfilter(saturation));
        if(frameWidth > 0 || frameCornerDp > 0){
            stickerView.setBackground(new BitmapDrawable(getResources(),getRoundedCornerBitmap(newFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true )),frameColor,
                    frameCornerDp,frameWidth)));
        }else{
            stickerView.setBackground(new BitmapDrawable(getResources(),newFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true ))));
        }
    }

    @Override
    public void applyBrightness(int brightness, boolean apply) {
        if (apply){
            this.brightness = brightness;
            Filter newFilter = new Filter();
            newFilter.addSubFilter(new BrightnessSubFilter(this.brightness));
            filteredBitmap = newFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true ));
            bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888,false);
            stickerView.setBackground(new BitmapDrawable(getResources(),bitmap));
        }
        if(frameCornerDp>0 || frameWidth >0){
            stickerView.setBackground(new BitmapDrawable(getResources(),getRoundedCornerBitmap(bitmap,frameColor,frameCornerDp,frameWidth)));
        }
        else{
            stickerView.setBackground(new BitmapDrawable(getResources(),bitmap));
        }

    }

    @Override
    public void applyContrast(float contrast, boolean apply) {
        if (apply){
            this.contrast = contrast;
            Filter newFilter = new Filter();
            newFilter.addSubFilter(new ContrastSubFilter(this.contrast));
            filteredBitmap = newFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true ));
            bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888,false);
        }
        if(frameCornerDp>0 || frameWidth >0){
            stickerView.setBackground(new BitmapDrawable(getResources(),getRoundedCornerBitmap(bitmap,frameColor,frameCornerDp,frameWidth)));
        }
        else{
            stickerView.setBackground(new BitmapDrawable(getResources(),bitmap));
        }
    }

    @Override
    public void applySaturation(float saturation, boolean apply) {
        if (apply){
            Log.e("saturation",saturation+"");
            this.saturation = saturation;
            Filter newFilter = new Filter();
            newFilter.addSubFilter(new SaturationSubfilter(this.saturation));
            filteredBitmap = newFilter.processFilter(filteredBitmap.copy(Bitmap.Config.ARGB_8888,true ));
            bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888,false);
        }
        if(frameCornerDp>0 || frameWidth >0){
            stickerView.setBackground(new BitmapDrawable(getResources(),getRoundedCornerBitmap(bitmap,frameColor,frameCornerDp,frameWidth)));
        }
        else{
            stickerView.setBackground(new BitmapDrawable(getResources(),bitmap));
        }
    }


}