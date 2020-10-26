package com.example.cutter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cutter.Interface.FiltersListFragmentListener;
import com.example.cutter.adapters.EditToolsAdapter;
import com.example.cutter.constants.Constants;
import com.example.cutter.dialogs.AddFilterDailog;
import com.example.cutter.dialogs.AddStickerDialog;
import com.example.cutter.dialogs.AddTextDialog;
import com.example.cutter.dialogs.FrameDialog;
import com.example.cutter.dialogs.SubFilterDialog;
import com.example.cutter.tools.ToolType;
import com.example.cutter.utils.BitmapUtils;
import com.example.cutter.utils.ImageUtilities;
import com.example.cutter.views.CustomTextViewOutline;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;

import com.xiaopo.flying.sticker.DrawableSticker;
import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.StickerView;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;


public class EditActivity extends AppCompatActivity implements  AddTextDialog.TextEditor,
        AddStickerDialog.onStickerListener, EditToolsAdapter.onToolListener,
        FiltersListFragmentListener, AddFilterDailog.onDialogFilterListener {
    private Bitmap originalBitmap,outPutBitmap,tempBitmap;
    private StickerView stickerView;
    private Toolbar toolbar;
    private List<ThumbnailItem> ThumbNailList;
    private String TAG = "_EDIT_ACTIVITY";
    private int frameWidth=0, frameColor = Color.BLUE,frameCornerDp=0,brightness=0,vignette = 0;
    private float contrast=1.0f,saturation=1.0f;
    private RecyclerView bottomToolsMenu;
    private SubFilterDialog subFilterDialog;
    private saveImageTask task;
    private ProgressDialog dialog;
    private ImageView backgroundImage;
    private boolean activityHasStarted = false;
    private FrameDimsRunnable frameRunnable;
    private Handler frameHandler;
    private SubFilterRunnable subFilterRunnable;
    private Handler subFilgerHandler;
    private InterstitialAd mInterstitialAd;
    float widthDif;
    static{
        System.loadLibrary("NativeImageProcessor");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        InitialiseViews();
        initialiseAdd();

        EditToolsAdapter adapter = new EditToolsAdapter(this, EditActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
        bottomToolsMenu.setLayoutManager(layoutManager);
        bottomToolsMenu.setAdapter(adapter);
        String imagePath = getIntent().getExtras().getString("bitmap_CropActivity");
        originalBitmap = ImageUtilities.decodeImage(imagePath);

        outPutBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
        stickerView.configDefaultIcons();
        stickerView.setLocked(false);
        stickerView.setLocked(false);
        stickerView.setConstrained(true);
        backgroundImage.setImageBitmap(originalBitmap);
        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {

            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {
                Log.d(TAG+"_amount_of_stickers",stickerView.getStickerCount()+"");
            }

            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {
                stickerView.bringToFrontCurrentSticker();
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


    private void InitialiseViews(){
        toolbar = findViewById(R.id.toolbar_edit_activity);
        final TextView toolBarTittle = toolbar.findViewById(R.id.toolbar_title);
        bottomToolsMenu = findViewById(R.id.bottomMenuRecyclerView);
        backgroundImage = findViewById(R.id.image_view_background);
        stickerView = findViewById(R.id.sticker_view);
        setSupportActionBar(toolbar);
        toolBarTittle.setText(R.string.toolbar_edit_activity_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
    private void initialiseAdd() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            if (!activityHasStarted){
                tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
                backgroundImage.setImageBitmap(tempBitmap);
                float originalWidth = originalBitmap.getWidth();
                float tempWidth = tempBitmap.getWidth();
                widthDif = originalWidth/tempWidth;
                Log.i(TAG+"_bitmap_original_size_on_edit", originalBitmap.getWidth()+"X"+originalBitmap.getHeight());
                Log.i(TAG+"_bitmap_temp_size_on_edit", tempBitmap.getWidth()+"X"+tempBitmap.getHeight());
                activityHasStarted = true;
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
                Bitmap bitmapStickerView = getResizedBitmap(stickerView.createBitmap(),this.originalBitmap.getWidth(),this.originalBitmap.getHeight());
                Bitmap background = Bitmap.createBitmap(outPutBitmap.getWidth(),outPutBitmap.getHeight(), outPutBitmap.getConfig());
                task = new saveImageTask(this,this,(int)(frameWidth*widthDif),frameColor,(int)(frameCornerDp*widthDif));
                dialog = ProgressDialog.show(this, "",
                        getResources().getString(R.string.edit_progress_dialog_message), true);
                task.execute(background,bitmapStickerView);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openColorPicker(){
        final Dialog colorPickerDialog = new Dialog(this);

        colorPickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        colorPickerDialog.setContentView(R.layout.color_picker_dialog);

        final ImageView imageViewPreviewColor = colorPickerDialog.findViewById(R.id.previewColor);
        final ImageView imageViewCloseColorPicker = colorPickerDialog.findViewById(R.id.image_view_close_color_picker);
        final ColorPicker colorPicker = colorPickerDialog.findViewById(R.id.colorPicker);

        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener(){
            @Override
            public void onColorSelected(int color) {
                super.onColorSelected(color);
                imageViewPreviewColor.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                frameColor = color;
                backgroundImage.setImageBitmap( getRoundedCornerBitmap(tempBitmap,frameColor,frameCornerDp,frameWidth));
            }

        });

        colorPickerDialog.show();

        imageViewCloseColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerDialog.dismiss();
            }
        });

        colorPickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }


    @Override
    public void onEditTextDone(CustomTextViewOutline strokedEditText) {
        strokedEditText.setCursorVisible(false);
        strokedEditText.buildDrawingCache();
        float[] sharp = { -0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f,
                -0.15f, -0.15f
        };
        Bitmap bitmapStrokedEditText = Bitmap.createBitmap(strokedEditText.getDrawingCache());
        bitmapStrokedEditText = BitmapUtils.doSharpen(bitmapStrokedEditText,sharp,EditActivity.this);
        Drawable drawable = new BitmapDrawable(getResources(), bitmapStrokedEditText);
        DrawableSticker drawableSticker = new DrawableSticker(drawable);
        stickerView.addSticker(drawableSticker);
    }

    @Override
    public void onStickerSelected(String fileName) {
        BitmapDrawable drawable = new BitmapDrawable(getResources(),BitmapUtils.getBitmapFromAsset(this,fileName));
        stickerView.addSticker(new DrawableSticker(drawable));

    }
    public  Bitmap getRoundedCornerBitmap(Bitmap bitmap, int color, int cornerDips, int borderDips) {
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


        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

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
                frameRunnable = new FrameDimsRunnable();
                frameHandler = new Handler();
                frameHandler.post(frameRunnable);
                break;
            case FILTER:
                displayThumbnail(outPutBitmap);
                break;
            case BRIGHTNESS:
                subFilterRunnable = new SubFilterRunnable(ToolType.BRIGHTNESS,this);
                subFilgerHandler = new Handler();
                subFilgerHandler.post(subFilterRunnable);
                break;
            case CONTRAST:
                subFilterRunnable = new SubFilterRunnable(ToolType.CONTRAST,this);
                subFilgerHandler = new Handler();
                subFilgerHandler.post(subFilterRunnable);
                break;
            case SATURATION:
                subFilterRunnable = new SubFilterRunnable(ToolType.SATURATION,this);
                subFilgerHandler = new Handler();
                subFilgerHandler.post(subFilterRunnable);
                break;
            case VIGNETTE:
                subFilterRunnable = new SubFilterRunnable(ToolType.VIGNETTE,this);
                subFilgerHandler = new Handler();
                subFilgerHandler.post(subFilterRunnable);
                break;
        }
    }

    //Draws Frame if width or corners are bigger than 0
    private void applyBitmapChanges(Bitmap bitmap){
        if(frameCornerDp> 0 || frameWidth >0 ){
            backgroundImage.setImageBitmap(getRoundedCornerBitmap(bitmap,frameColor,frameCornerDp,frameWidth));

        }else{
            backgroundImage.setImageBitmap(bitmap);
        }

    }
    @Override
    public void onFilterSelected(Filter filter) {
        tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
        backgroundImage.setImageBitmap(null);
        applyBitmapChanges(filter.processFilter(tempBitmap));

    }
    @Override
    public void setFilter(Filter filter,boolean apply) {
        if(apply){
            outPutBitmap = this.originalBitmap.copy(Bitmap.Config.ARGB_8888,true);
            outPutBitmap = filter.processFilter(outPutBitmap);
        }
        tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
        applyBitmapChanges(tempBitmap);
        bottomToolsMenu.setVisibility(View.VISIBLE);
        ThumbNailList.clear();
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
                canvas.drawBitmap(activity.getRoundedCornerBitmap(activity.outPutBitmap,this.frameColor,this.frameCornerDp,this.frameWidth), new Matrix(), null);
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
        ThumbNailList = new ArrayList<>();
        ThumbNailList.clear();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Bitmap thumbImg;
                int width = (int) (bitmap.getWidth()*0.9);
                float dp = 80;
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
                thumbImg = ImageUtilities.createSquaredBitmap(bitmap,width);
                Log.i(TAG+"_thumb_nails_dimensions",width+"x"+width);
                ThumbnailsManager.clearThumbs();
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
                ThumbNailList.addAll(ThumbnailsManager.processThumbs(EditActivity.this));
                AddFilterDailog dialogFilters = new AddFilterDailog(ThumbNailList);
                dialogFilters.setBitmap(originalBitmap);
                dialogFilters.show(getSupportFragmentManager(),"");
                Log.i(TAG+"_filters_list_size",ThumbNailList.size()+"");
            }
        };

        new Handler().post(r);
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
            frameWidth = (int)(width/widthDif);
            frameColor = color;
            frameCornerDp = (int)(dpCorners/widthDif);
            backgroundImage.setImageBitmap(getRoundedCornerBitmap(tempBitmap,frameColor,frameCornerDp,frameWidth));
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

        }
        @Override
        public void onBrightnessChanged(int brightness) {
            activity.brightness = brightness;
            applyBitmapChanges(ImageUtilities.changeBitmapBrightness(tempBitmap,brightness));
        }

        @Override
        public void onContrastChanged(float contrast) {
            activity.contrast = contrast;
            applyBitmapChanges(ImageUtilities.changeBitmapContrast(tempBitmap,contrast));
        }

        @Override
        public void onSaturationChanged(float saturation) {
            activity.saturation = saturation;
            applyBitmapChanges(ImageUtilities.changeBitmapSaturation(tempBitmap,saturation));
        }

        @Override
        public void onVignetteChanged(int vignette) {
            int color = Color.parseColor(getVignetteAlphaChannel(vignette));
            applyBitmapChanges(ImageUtilities.changeBitmapVignette(tempBitmap,vignette,color));
        }

        @Override
        public void applyBrightness(int brightness, boolean apply) {
            if (apply){
                activity.brightness = brightness;
                outPutBitmap = ImageUtilities.changeBitmapBrightness(outPutBitmap,brightness);
                tempBitmap = ImageUtilities.changeBitmapBrightness(tempBitmap,brightness);

            }
            applyBitmapChanges(tempBitmap);
            bottomToolsMenu.setVisibility(View.VISIBLE);
        }

        @Override
        public void applyContrast(float contrast, boolean apply) {
            if (apply){
                tempBitmap = Bitmap.createBitmap(outPutBitmap,0,0,outPutBitmap.getWidth(), outPutBitmap.getHeight(),scaleImage(outPutBitmap),true);
                activity.contrast = contrast;
                outPutBitmap = ImageUtilities.changeBitmapContrast(outPutBitmap,contrast);
                tempBitmap = ImageUtilities.changeBitmapContrast(tempBitmap,contrast);

            }
            applyBitmapChanges(tempBitmap);
            bottomToolsMenu.setVisibility(View.VISIBLE);
        }

        @Override
        public void applySaturation(float saturation, boolean apply) {
            if (apply){
                activity.saturation = saturation;
                outPutBitmap = ImageUtilities.changeBitmapSaturation(outPutBitmap,saturation);
                tempBitmap = ImageUtilities.changeBitmapSaturation(tempBitmap,saturation);
            }
            applyBitmapChanges(tempBitmap);
            bottomToolsMenu.setVisibility(View.VISIBLE);

        }

        @Override
        public void applyVignette(int vignette, boolean apply) {
            if (apply){
                activity.vignette = vignette;
                int color = Color.parseColor(getVignetteAlphaChannel(vignette));
                outPutBitmap = ImageUtilities.changeBitmapVignette(outPutBitmap,vignette,color);
                tempBitmap = ImageUtilities.changeBitmapVignette(tempBitmap,vignette,color);
            }
            applyBitmapChanges(tempBitmap);
        }

    }
    private String getVignetteAlphaChannel(int vignette){
        int percentage = (255*vignette)/100;
        String alpha = String.format("#%02x", percentage);
        return alpha+"000000";
    }

    @Override
    protected void onDestroy() {
        if(task != null){
            task.cancel(true);
        }
        super.onDestroy();
    }
}