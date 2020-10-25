package com.example.cutter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cutter.Interface.FiltersListFragmentListener;
import com.example.cutter.adapters.EditToolsAdapter;
import com.example.cutter.adapters.ThumbnailAdapter;
import com.example.cutter.constants.Constants;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;

import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;
import com.yovenny.sticklib.StickerSeriesView;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.SubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;


import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EditActivity extends AppCompatActivity implements  AddTextDialog.TextEditor,
        AddStickerDialog.onStickerListener, EditToolsAdapter.onToolListener,
        FiltersListFragmentListener, AddFilterDailog.onDialogFilterListener {
    private Bitmap originalBitmap,outPutBitmap,tempBitmap;
    private StickerView stickerView;
    private Toolbar toolbar;
    private List<ThumbnailItem> list;
    private int frameWidth=0, frameColor = Color.BLUE,frameCornerDp=0,brightness=0;
    private float contrast=1.0f,saturation=1.0f;
    private List<String> stickerPaths;
    private RecyclerView bottomTools;
    private SubFilterDialog subFilterDialog;
    private saveImageTask task;
    private ProgressDialog dialog;
    //private StickerSeriesView stickerSeriesView;
    private ImageView backgroundImage;
    private boolean hasStarted = false,frameDialogIsOpen,drawFrame=false;
    private FrameDimsRunnable frameRunnable;
    private Handler frameHandler;
    private SubFilterRunnable subFilterRunnable;
    private Handler subFilgerHandler;
    private int originalWidth, originalHeight;
    private InterstitialAd mInterstitialAd;
    float dif;
    static{
        System.loadLibrary("NativeImageProcessor");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        toolbar = findViewById(R.id.toolbar_edit_activity);
        setSupportActionBar(toolbar);
        TextView toolBarTittle = toolbar.findViewById(R.id.toolbar_title);
        toolBarTittle.setText(R.string.toolbar_edit_activity_title);
        stickerView = findViewById(R.id.sticker_view);

        bottomTools = findViewById(R.id.bottomMenuRecyclerView);
        backgroundImage = findViewById(R.id.image_view_background);
        EditToolsAdapter adapter = new EditToolsAdapter(this, EditActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
        bottomTools.setLayoutManager(layoutManager);
        bottomTools.setAdapter(adapter);
        /*bottomNavigationView = findViewById(R.id.bottomNavigationViewEditImage);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);*/
        String imagePath = getIntent().getExtras().getString("bitmap_CropActivity");
        originalBitmap = ImageUtilities.decodeImage(imagePath);

        outPutBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);

        stickerView.configDefaultIcons();
        stickerView.setLocked(false);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), originalBitmap);
        stickerView.setLocked(false);
        stickerView.setConstrained(true);
        backgroundImage.setImageBitmap(originalBitmap);

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            if (!hasStarted){
                /*ViewGroup.LayoutParams params = stickerView.getLayoutParams();
                tempBitmap = BitmapUtils.scaleBitmap(originalBitmap,backgroundImage.getWidth(),backgroundImage.getHeight());
                params.width = backgroundImage.getWidth();
                params.height = backgroundImage.getHeight();
                stickerView.setLayoutParams(params);
                //backgroundImage.setImageBitmap(null);
                //backgroundImage.setBackground(new ColorDrawable(0x00000000));
                //stickerView.setBackground(new BitmapDrawable(getResources(),tempBitmap));
                Log.e("imageview_background",backgroundImage.getWidth()+"X"+backgroundImage.getHeight());*/

                originalWidth = originalBitmap.getWidth();
                originalHeight = originalBitmap.getHeight();
                tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
                //tempBitmap = BitmapUtils.BITMAP_RESIZER(outPutBitmap,backgroundImage.getWidth(),backgroundImage.getHeight());
                backgroundImage.setImageBitmap(tempBitmap);
                float originalWidth = originalBitmap.getWidth();
                float tempWidth = tempBitmap.getWidth();
                dif = originalWidth/tempWidth;
                Log.e("bitmap_size_on_edit", originalBitmap.getWidth()+"X"+originalBitmap.getHeight());
                Log.e("bitmap_size_on_edit", tempBitmap.getWidth()+"X"+tempBitmap.getHeight());
                hasStarted = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_toolbar_edit,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_cancel_edit:
                this.finish();
                break;
            case R.id.action_save_image:

                stickerView.setBackgroundColor(0x00000000);
                backgroundImage.setImageBitmap(null);
                backgroundImage.setBackground(new ColorDrawable(0x00000000));
                Bitmap bitmap = getResizedBitmap(stickerView.createBitmap(),this.originalBitmap.getWidth(),this.originalBitmap.getHeight());
                Bitmap background = Bitmap.createBitmap(outPutBitmap.getWidth(),outPutBitmap.getHeight(), outPutBitmap.getConfig());
                /*Canvas canvas = new Canvas(background);
                canvas.drawBitmap(outPutBitmap, new Matrix(), null);
                canvas.drawBitmap(bitmap, new Matrix(), null);*/
                task = new saveImageTask(this,this,(int)(frameWidth*dif),frameColor,(int)(frameCornerDp*dif));
                dialog = ProgressDialog.show(this, "",
                        getResources().getString(R.string.edit_progress_dialog_message), true);
                task.execute(background,bitmap);
                break;
        }
        return super.onOptionsItemSelected(item);
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
                backgroundImage.setImageBitmap( getRoundedCornerBitmap(tempBitmap,frameColor,frameCornerDp,frameWidth,true));
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
    public  Bitmap getRoundedCornerBitmap(Bitmap bitmap, int color, int cornerDips, int borderDips,boolean drawBitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int borderSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) borderDips,
                getResources().getDisplayMetrics());
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
                //FrameDialog frameDialog = new FrameDialog();
                frameRunnable = new FrameDimsRunnable();
                frameHandler = new Handler();
                frameHandler.post(frameRunnable);
                /*frameDialog.setSelectFrameColor(frameColor);
                frameDialog.setFrameWidth(frameWidth);
                frameDialog.setFrameCorners(frameCornerDp);
                frameDialog.show(getSupportFragmentManager(),"");*/
                break;
            case FILTER:
                displayThumbnail(outPutBitmap);

                break;
            case BRIGHTNESS:
                subFilterRunnable = new SubFilterRunnable(ToolType.BRIGHTNESS,this);
                subFilgerHandler = new Handler();
                subFilgerHandler.post(subFilterRunnable);
                /*subFilterDialog = new SubFilterDialog();
                subFilterDialog.setToolType(ToolType.BRIGHTNESS);
                subFilterDialog.show(getSupportFragmentManager(),"");
                bottomTools.setVisibility(View.GONE);*/
                break;
            case CONTRAST:
                subFilterRunnable = new SubFilterRunnable(ToolType.CONTRAST,this);
                subFilgerHandler = new Handler();
                subFilgerHandler.post(subFilterRunnable);
                /*subFilterDialog = new SubFilterDialog();
                subFilterDialog.setToolType(ToolType.CONTRAST);
                subFilterDialog.show(getSupportFragmentManager(),"");
                bottomTools.setVisibility(View.GONE);*/
                break;
            case SATURATION:
                subFilterRunnable = new SubFilterRunnable(ToolType.SATURATION,this);
                subFilgerHandler = new Handler();
                subFilgerHandler.post(subFilterRunnable);
                /*subFilterDialog = new SubFilterDialog();
                subFilterDialog.setToolType(ToolType.SATURATION);
                subFilterDialog.show(getSupportFragmentManager(),"");
                bottomTools.setVisibility(View.GONE);*/
                break;
        }
    }
    private void applyBitmapChanges(Bitmap bitmap){
        if(frameCornerDp> 0 || frameWidth >0 ){
            //stickerView.setBackground(new BitmapDrawable(getResources(), getRoundedCornerBitmap(filter.processFilter(outPutBitmap),frameColor,frameCornerDp,frameWidth)));
            backgroundImage.setImageBitmap(getRoundedCornerBitmap(bitmap,frameColor,frameCornerDp,frameWidth,true));

        }else{
            //BitmapDrawable drawable = new BitmapDrawable(getResources(),filter.processFilter(outPutBitmap));
            //stickerView.setBackground(drawable);

            backgroundImage.setImageBitmap(bitmap);
            //backgroundImage.setImageBitmap(filter.processFilter(outPutBitmap));
        }

    }
    @Override
    public void onFilterSelected(Filter filter) {
        //outPutBitmap = this.originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
        backgroundImage.setImageBitmap(null);
        //filteredBitmap = filter.processFilter(filteredBitmap);
        /*if(frameCornerDp> 0 || frameWidth >0 ){
            //stickerView.setBackground(new BitmapDrawable(getResources(), getRoundedCornerBitmap(filter.processFilter(outPutBitmap),frameColor,frameCornerDp,frameWidth)));
            backgroundImage.setImageBitmap( getRoundedCornerBitmap(filter.processFilter(tempBitmap),frameColor,frameCornerDp,frameWidth));
        }else{
            //BitmapDrawable drawable = new BitmapDrawable(getResources(),filter.processFilter(outPutBitmap));
            //stickerView.setBackground(drawable);

            backgroundImage.setImageBitmap(filter.processFilter(tempBitmap));
            //backgroundImage.setImageBitmap(filter.processFilter(outPutBitmap));
        }*/
        applyBitmapChanges(filter.processFilter(tempBitmap));

    }
    @Override
    public void FilterSelected(int position) {
        /*Log.e("position",position+"");
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();
        Bitmap temp = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        canvas.drawColor(getColor(R.color.transparent));
        Bitmap filtered = bitmap.copy(bitmap.getConfig(),true);
        filtered = list.get(position).filter.processFilter(filtered);
        //filtered = BitmapUtils.BITMAP_RESIZER(filtered,bitmap.getWidth(),bitmap.getHeight());
        //String tempFile = ImageUtilities.encodeImage(filtered, Bitmap.CompressFormat.PNG,100);
        //filtered = ImageUtilities.decodeImage(tempFile);
        task = new saveImageTask(this,this);
        dialog = ProgressDialog.show(this, "",
                getResources().getString(R.string.edit_progress_dialog_message), true);
        task.execute(filtered);
        stickerView.setBackground(new BitmapDrawable(getResources(), filtered));
       /* String tempFile = ImageUtilities.encodeImage(filtered, Bitmap.CompressFormat.PNG,100);
        filtered = ImageUtilities.decodeImage(tempFile);
        canvas.drawBitmap(this.bitmap, new Matrix(), null);
        canvas.drawBitmap(filtered, new Matrix(), null);

        task = new saveImageTask(this,this);
        dialog = ProgressDialog.show(this, "",
                getResources().getString(R.string.edit_progress_dialog_message), true);
        task.execute(temp);*/
        //Toast.makeText(this,"ASDASDASD"+position,Toast.LENGTH_LONG).show();
    }
    @Override
    public void setFilter(Filter filter,boolean apply) {
        if(apply){
            outPutBitmap = this.originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
            outPutBitmap = filter.processFilter(outPutBitmap);
            tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
            //task = new saveImageTask(this,this);
            //dialog = ProgressDialog.show(this, "",
                    //getResources().getString(R.string.edit_progress_dialog_message), true);
            //task.execute(outPutBitmap);
            Log.e("filtered_dims",outPutBitmap.getWidth()+"X"+outPutBitmap.getHeight());
            //bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888,false);
            /*if(frameCornerDp > 0 || frameWidth >0){
                //temp_bitmap = getRoundedCornerBitmap(bitmap,frameColor,frameCornerDp,frameWidth);
                backgroundImage.setImageBitmap(getRoundedCornerBitmap(tempBitmap,frameColor,frameCornerDp,frameWidth));
            }else{
                backgroundImage.setImageBitmap(tempBitmap);
            }*/
            applyBitmapChanges(tempBitmap);
            //BitmapDrawable drawable = new BitmapDrawable(getResources(),tempBitmap);
            //stickerView.setBackground(drawable);
        }else{
            //outPutBitmap = originalBitmap.copy(originalBitmap.getConfig(),true);
            //stickerView.setBackground(new BitmapDrawable(getResources(),outPutBitmap));
            tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
            /*if(frameCornerDp > 0 || frameWidth >0){
                //temp_bitmap = getRoundedCornerBitmap(bitmap,frameColor,frameCornerDp,frameWidth);
                backgroundImage.setImageBitmap(getRoundedCornerBitmap(tempBitmap,frameColor,frameCornerDp,frameWidth));
            }else{
                backgroundImage.setImageBitmap(tempBitmap);
            }*/
            applyBitmapChanges(tempBitmap);
        }
        bottomTools.setVisibility(View.VISIBLE);
        list.clear();
    }

    private class saveImageTask extends AsyncTask<Bitmap,Void,Void>{
        private EditActivity activity;
        private Context context;
        private int frameWidth, frameColor,frameCornerDp;
        saveImageTask(EditActivity activity, Context context, int frameWidth, int frameColor, int frameCornerDp){
            this.activity = activity;
            this.context = context;
            this.frameWidth = frameWidth;
            this.frameColor = frameColor;
            this.frameCornerDp = frameCornerDp;
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            Bitmap outputBitmap = bitmaps[0];
            Bitmap stickerBitmap = bitmaps[1];
            Canvas canvas = new Canvas(outputBitmap);
            if(this.frameCornerDp > 0 || frameWidth >0){
                canvas.drawBitmap(activity.getRoundedCornerBitmap(activity.outPutBitmap,this.frameColor,this.frameCornerDp,this.frameWidth,false), new Matrix(), null);
            }else{
                canvas.drawBitmap(activity.outPutBitmap, new Matrix(), null);
            }
            canvas.drawBitmap(stickerBitmap, new Matrix(), null);
            ImageUtilities.saveAsFile(Bitmap.CompressFormat.PNG,outputBitmap,100, context);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            activity.mInterstitialAd.show();
            activity.dialog.dismiss();
            activity.finish();
            Toast.makeText(context,getResources().getString(R.string.edit_image_saved_message)+ Constants.savedImagesPath,Toast.LENGTH_LONG).show();
            super.onPostExecute(aVoid);
        }
    }
    public void displayThumbnail(final Bitmap bitmap){
        list = new ArrayList<>();
        list.clear();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.e("filter list","on display");
                Bitmap thumbImg;
                int width = (int) (bitmap.getWidth()*0.6);
                int height = (int)(bitmap.getHeight()*0.6);
                float dp = 80;
                //Resources r = getResources();

                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
                thumbImg = ImageUtilities.createSquaredBitmap(bitmap,width);
                //thumbImg = ImageUtilities.getResizedBitmap(bitmap,width,height);

                Log.e("thumb_nails_dims",width+"x"+height);
                ThumbnailsManager.clearThumbs();
                //list.clear();
                ThumbnailItem thumbnailItem = new ThumbnailItem();
                thumbnailItem.image = thumbImg;
                thumbnailItem.filterName = "normal";
                ThumbnailsManager.addThumb(thumbnailItem);
                List<Filter> filters = FilterPack.getFilterPack(EditActivity.this);
                for(Filter filter:filters){
                    ThumbnailItem tI = new ThumbnailItem();
                    tI.image = thumbImg;
                    tI.filter = filter;
                    tI.filterName = filter.getName();
                    ThumbnailsManager.addThumb(tI);
                }

                list.addAll(ThumbnailsManager.processThumbs(EditActivity.this));
                AddFilterDailog dialogFilters = new AddFilterDailog(list);
                dialogFilters.setBitmap(originalBitmap);
                dialogFilters.show(getSupportFragmentManager(),"");
                //bottomTools.setVisibility(View.GONE);
                Log.e("filters_size",list.size()+"");
            }
        };

        new Handler().post(r);
    }
    @Override
    protected void onDestroy() {

        if(task != null){
            task.cancel(true);
        }
        super.onDestroy();
    }
    public Bitmap getResizedBitmap(Bitmap bitmap,int newWidth,int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ALPHA_8.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }
    private Matrix scaleImage(Bitmap bitmap){
        float originalWidth = bitmap.getWidth();
        float originalHeight = bitmap.getHeight();
        float scale = backgroundImage.getWidth()/originalWidth;
        float xTranslation = 0.0f;
        float yTranslation = (backgroundImage.getHeight() - originalHeight * scale) / 2.0f;
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        return transformation;
    }
    class FrameDimsRunnable implements Runnable, FrameDialog.onFrameWidthListener{
        private FrameDialog dialog;

        @Override
        public void run() {
            dialog = new FrameDialog(this);
            dialog.setSelectFrameColor(frameColor);
            dialog.setFrameWidth(frameWidth);
            dialog.setFrameCorners(frameCornerDp);
            dialog.show(getSupportFragmentManager(),"");
        }

       @Override
        public void onWidthChange(int width, int color, int dpCorners) {
            frameWidth = (int)(width/dif);
            frameColor = color;
            frameCornerDp = (int)(dpCorners/dif);
            drawFrame = true;
            backgroundImage.setImageBitmap(getRoundedCornerBitmap(tempBitmap,frameColor,frameCornerDp,frameWidth,true));
        }

    }
    class SubFilterRunnable implements Runnable, SubFilterDialog.onSubFilterListener{
        private ToolType toolType;
        private EditActivity activity;
        SubFilterRunnable(ToolType toolType, EditActivity activity){
            this.toolType = toolType;
            this.activity = activity;
        }
        @Override
        public void run() {
            subFilterDialog = new SubFilterDialog(this);
            subFilterDialog.setToolType(toolType);
            subFilterDialog.show(getSupportFragmentManager(),"");
            bottomTools.setVisibility(View.INVISIBLE);
        }
        @Override
        public void onBrightnessChanged(int brightness) {
            activity.brightness = brightness;
            final Filter newFilter = new Filter();
            newFilter.addSubFilter(new BrightnessSubFilter(activity.brightness));
            applyBitmapChanges(newFilter.processFilter(tempBitmap.copy(Bitmap.Config.ARGB_8888,true )));
        }

        @Override
        public void onContrastChanged(float contrast) {
            activity.contrast = contrast;
            final Filter newFilter = new Filter();
            newFilter.addSubFilter(new ContrastSubFilter(activity.contrast));
            applyBitmapChanges(newFilter.processFilter(tempBitmap.copy(Bitmap.Config.ARGB_8888,true )));
        }

        @Override
        public void onSaturationChanged(float saturation) {
            activity.saturation = saturation;
            Filter newFilter = new Filter();
            newFilter.addSubFilter(new SaturationSubfilter(saturation));
            applyBitmapChanges(newFilter.processFilter(tempBitmap.copy(Bitmap.Config.ARGB_8888,true )));
        }

        @Override
        public void applyBrightness(int brightness, boolean apply) {
            if (apply){
                //outPutBitmap = originalBitmap.copy(originalBitmap.getConfig(),true);
                Log.e("FACK","FACK");
                activity.brightness = brightness;
                Filter newFilter = new Filter();
                newFilter.addSubFilter(new BrightnessSubFilter(activity.brightness));
                outPutBitmap = newFilter.processFilter(outPutBitmap.copy(Bitmap.Config.ARGB_8888,true ));
                //tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
                applyBitmapChanges(newFilter.processFilter(tempBitmap));

            }else{
                applyBitmapChanges(tempBitmap);
            }
            bottomTools.setVisibility(View.VISIBLE);
        }

        @Override
        public void applyContrast(float contrast, boolean apply) {
            if (apply){
                //outPutBitmap = originalBitmap.copy(originalBitmap.getConfig(),true);
                tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
                activity.contrast = contrast;
                Filter newFilter = new Filter();
                newFilter.addSubFilter(new ContrastSubFilter(activity.contrast));
                outPutBitmap = newFilter.processFilter(outPutBitmap.copy(Bitmap.Config.ARGB_8888,true ));
                applyBitmapChanges(newFilter.processFilter(tempBitmap));

            }else{
                applyBitmapChanges(tempBitmap);
            }
            bottomTools.setVisibility(View.VISIBLE);
        }

        @Override
        public void applySaturation(float saturation, boolean apply) {

            if (apply){
                Log.e("saturation",saturation+"");
                activity.saturation = saturation;
                Filter newFilter = new Filter();
                newFilter.addSubFilter(new SaturationSubfilter(activity.saturation));
                outPutBitmap = newFilter.processFilter(outPutBitmap.copy(Bitmap.Config.ARGB_8888,true ));
                applyBitmapChanges(newFilter.processFilter(tempBitmap));

            }else{
                applyBitmapChanges(tempBitmap);
            }
            bottomTools.setVisibility(View.VISIBLE);

        }

    }
}