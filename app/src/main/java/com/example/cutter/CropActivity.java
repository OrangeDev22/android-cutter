package com.example.cutter;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.cutter.adapters.IconMenuAdapter;
import com.example.cutter.utils.ImageUtilities;
import com.example.cutter.views.DrawView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.skydoves.powermenu.CustomPowerMenu;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class CropActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, DrawView.onImageCroppedListener{
    private DrawView im_crop_image;
    private Bitmap bmp;
    private Bitmap alteredBitmap;
    private Canvas canvas;
    private int screen_width,screen_height;
    private static final int PICK_IMAGE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private boolean cropped= false;
    private ProgressDialog dialog;
    private boolean trimming = false;
    private String currentPhotoPath;
    private BottomNavigationView bottomNav;
    private CustomPowerMenu customPowerMenu;
    private Toolbar toolbar;
    private ImageUtilities imageUtilities;
    private Drawable drawable;
    int STORAGE_PERMISSION_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

       if(RequestPermissionsHelper.verifyPermissions(this)){
           initApp();
           bitmapToImageView(bmp);
        }
        else{
          requestPermissions();
        }
        Log.e("app status","on create");
       // im_crop_image.setOnTouchListener(this);

       /* canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bmp, cx,cy,null);
        drawable = new BitmapDrawable(getApplicationContext().getResources(),bmp);
        im_crop_image.setBackground(drawable);*/

        //im_crop_image.setImageBitmap(alteredBitmap);

    }
    private void requestPermissions(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(CropActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            ,Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ,Manifest.permission.CAMERA}, STORAGE_PERMISSION_CODE);
        }
            /*ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.CAMERA},PERMISION_CODE);*/
        //RequestPermissionsHelper.requestPermission(this);
    }

    private boolean checkPermissions(){
        boolean permission = false;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            permission = true;
             if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                 if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

                 }
                 else{
                     permission = false;
                 }
             }else{
                 permission = false;
             }
        }
        else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1);
        }
        return true;
    }

    private void initApp(){
        init();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark));
        }


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
       if(hasFocus){
           //Log.e("toolbard and bottombar dims",bottomNav.getHeight()+"x"+toolbar.getHeight());
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(bmp,scaleImage(bmp),paint);
        bitmapToImageView(bmp);
        im_crop_image.setBitmap(alteredBitmap);
        cropped = false;
    }

    private void init(){
        toolbar = findViewById(R.id.toolbar);
        customPowerMenu = new CustomPowerMenu.Builder<>(this, new IconMenuAdapter())
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_image_24),"Image"))
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_camera_alt_24),"Camera"))
                .setOnMenuItemClickListener(onIconMenuItemClickListener)
                .setAnimation(MenuAnimation.SHOWUP_BOTTOM_LEFT)
                //setMenuRadius(10f)
                .setMenuShadow(10f)
                .setWidth(screen_width)
                .build();
        setSupportActionBar(toolbar);
        TextView textView = toolbar.findViewById(R.id.toolbar_title);
        textView.setText("Cropper");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        im_crop_image =  findViewById(R.id.im_crop_image);
        im_crop_image.setOnImageCroppedListener(this);
        im_crop_image.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.bottomCropSquare);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_width = metrics.widthPixels;
        screen_height = displayMetrics.heightPixels- getStatusBarHeight();

        imageUtilities = new ImageUtilities();

        initCanvas();
    }
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    public void initCanvas(){
        Drawable d = ContextCompat.getDrawable(this, R.drawable.chiki_drawing);
        Rect dest = new Rect(0, 0, screen_width, screen_height);
         /*Bitmap background = Bitmap.createScaledBitmap
                (bmp, screen_width, screen_height, false);*/
        //bmp = ((BitmapDrawable)d).getBitmap();
        //bmp = ((BitmapDrawable)d).getBitmap();
        //bmp = Bitmap.createScaledBitmap(((BitmapDrawable)d).getBitmap(), screen_width,screen_height,false);
        Bitmap temp = ((BitmapDrawable)d).getBitmap();
        bmp = ((BitmapDrawable)d).getBitmap();

        alteredBitmap = Bitmap.createBitmap(screen_width, screen_height,bmp.getConfig());
        canvas = new Canvas(alteredBitmap);

        im_crop_image.setBitmap(alteredBitmap);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_toolbar_cropper,menu);

        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.bottomCropImage:
                    showPopup(bottomNav);
                    break;
                case R.id.bottomCropSquare:
                    im_crop_image.setMode(0);
                    break;
                case R.id.bottomCropOval:
                    im_crop_image.setMode(1);
                    break;
                case R.id.bottomCropFree:
                    im_crop_image.setMode(2);
                    break;
                case R.id.bottomCropSelectAll:
                    startNewActivity();
                    //im_crop_image.setMode(3);
                    //Toast.makeText(getBaseContext(),"Please click on the area you want to delete",Toast.LENGTH_LONG).show();
                    //im_crop_image.startAutoClear(0,0);
                    break;
            }
            return true;
        }
    };
    private void showPopup(View v){
        /*PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_select_item_menu);
        popupMenu.show();*/


        Log.e("bottom nav height",bottomNav.getHeight()+"x"+customPowerMenu.getContentViewHeight());
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        customPowerMenu.showAsDropDown(bottomNav,
                bottomNav.getMeasuredWidth()/2 - customPowerMenu.getContentViewWidth(),
                -bottomNav.getMeasuredHeight() - customPowerMenu.getContentViewHeight());

    }
    private OnMenuItemClickListener<IconPowerMenuItem> onIconMenuItemClickListener = new OnMenuItemClickListener<IconPowerMenuItem>() {
        @Override
        public void onItemClick(int position, IconPowerMenuItem item) {
            Toast.makeText(getBaseContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
            switch(item.getTitle()){
                case "Image":
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");
                    Intent chooserIntent = Intent.createChooser(getIntent,"Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                    //chooserIntent.putExtra("ACTION",Constants.addTrayIcon);
                    startActivityForResult(chooserIntent,PICK_IMAGE);
                    break;
                case "Camera":
                    String fileName = "photo";
                    File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    try {
                        File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                        currentPhotoPath = imageFile.getAbsolutePath();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        Uri uri = FileProvider.getUriForFile(CropActivity.this,"com.example.cutter.fileprovider",imageFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                        startActivityForResult(intent,CAMERA_REQUEST);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            customPowerMenu.dismiss();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bitmap bitmap;
        switch (item.getItemId()){
            case R.id.action_flip:
                bitmap = imageUtilities.flipBitmap(bmp);
                bitmapToImageView(bitmap);
                return true;
            case R.id.action_rotate_right:
                bitmap = imageUtilities.rotateBitmap(bmp,90f);
                bitmapToImageView(bitmap);
                return  true;
            case R.id.action_rotate_left:
                bitmap = imageUtilities.rotateBitmap(bmp,-90f);
                bitmapToImageView(bitmap);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }


    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.popUpImage:
                break;
            case R.id.popUpCamera:

                break;
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    private CropModel projectXY(ImageView iv, Bitmap bm, int x, int y){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return null;
        }else{
            int projectedX = (int)((double)x * ((double)bm.getWidth()/(double)iv.getWidth()));
            int projectedY = (int)((double)y * ((double)bm.getHeight()/(double)iv.getHeight()));

            return new CropModel(projectedX, projectedY);
        }
    }

    @Override
    public void onImageCropped(ByteArrayOutputStream bytes) {
        Log.e("CROPPING", "in");
        Bitmap temp_bitmap = BitmapFactory.decodeByteArray(bytes.toByteArray(),0,bytes.toByteArray().length);

        if(temp_bitmap.getWidth() > 0 && temp_bitmap.getHeight() >0){
            Bitmap emptyBitmap = Bitmap.createBitmap(temp_bitmap.getWidth(), temp_bitmap.getHeight(),temp_bitmap.getConfig());
            if(!temp_bitmap.sameAs(emptyBitmap)){
                cropped = true;
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                startAsyncTask(temp_bitmap);
            }else{
                Log.e("Bitmap_Status","Bitmap is empty");
            }
        }


    }

    @Override
    public void onBackPressed() {
        /*if(this.cropped){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            int cx = (screen_width - bmp.getWidth()) >> 1;
            int cy = (screen_height - bmp.getHeight()) >> 1;
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            canvas.drawBitmap(bmp,scaleImage(bmp),paint);
            //canvas.drawBitmap(bmp, scaleImage(bmp), paint);
            //im_crop_image.setImageBitmap(alteredBitmap);
            bitmapToImageView(bmp);
            im_crop_image.setBitmap(alteredBitmap);
            //bitmapToImageView(bmp);
            cropped = false;
        }else*/
        if(customPowerMenu.isShowing()){
            customPowerMenu.dismiss();
        }
        else{
            super.onBackPressed();
        }
    }



    public void startAsyncTask(Bitmap bitmap){
        TrimAsyncTask task = new TrimAsyncTask(this);
        if(!trimming){
            task.execute(bitmap);
            dialog = ProgressDialog.show(CropActivity.this, "",
                    "Loading. Please wait...", true);
        }
        else{
            Log.d("Trim status","active please wait for original thread to finish");
        }
    }



    private static class TrimAsyncTask extends AsyncTask<Bitmap, Integer, Bitmap> {
        private WeakReference<CropActivity> activityWeakReference;
        TrimAsyncTask(CropActivity activity){
            activityWeakReference = new WeakReference<>(activity);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CropActivity activity = activityWeakReference.get();
            if(activity == null || activity.isFinishing() ){
                return;
            }
            activity.trimming = true;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = ImageUtilities.trim(bitmaps[0]);
            return  bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            CropActivity activity = activityWeakReference.get();
            if(activity == null || activity.isFinishing() ){
                return;
            }
            super.onPostExecute(bitmap);
            int cx = (activity.screen_width - bitmap.getWidth()) >> 1;
            int cy = (activity.screen_height - bitmap.getHeight()) >> 1;
            activity.canvas.drawBitmap(bitmap,cx,cy,null);
            Rect rect = new Rect(0,0,activity.screen_width,activity.screen_height);
            Canvas canvas = new Canvas();
            canvas.drawBitmap(bitmap,rect,rect,null);
            //Bitmap canvasBitmap = Bitmap.createBitmap(activity.screen_width,activity.screen_height,)
            Log.e("bitmap dimensions after trimming",bitmap.getWidth()+"x"+bitmap.getHeight());
            //Drawable drawable = new BitmapDrawable(activity.getResources(),activity.alteredBitmap);
            //activity.im_crop_image.setBackground(drawable);
            activity.dialog.dismiss();
            activity.trimming = false;
            activity.startNewActivity();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //FileUtils.initializeDirectories(this);
        if(RequestPermissionsHelper.verifyPermissions(this)){
            initApp();
            int cx = (screen_width - bmp.getWidth())/2;
            int cy = (screen_height - bmp.getHeight())/2;
            Log.e("cx and cy", cx+"x"+cy);
            Rect dest = new Rect(0, 0, screen_width, screen_height);
            Paint paint =new Paint();
            paint.setFilterBitmap(true);
            bitmapToImageView(bmp);
            //overridePendingTransition(0, 0);
        }
        else{

            Toast.makeText(this, "we need access to write and read files in your phone", Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK){
            try {
                Uri imageData = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);
                bitmapToImageView(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            bitmap =  BitmapFactory.decodeFile(currentPhotoPath);
            bitmapToImageView(bitmap);
        }
    }
    private void bitmapToImageView(Bitmap bitmap){
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();

        bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),scaleImage(bitmap),true);
        bmp = bitmap;
        Log.e("Bitmap original Dimensions",inWidth +"X"+inHeight);
        Log.e("Bitmap new Dimensions",bitmap.getWidth() +"X"+bitmap.getHeight());
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Rect dest = new Rect(0, 0, screen_width, bitmap.getHeight());
        int cx = (screen_width- bmp.getWidth()) >> 1;
        int cy = (screen_height - bmp.getHeight()) >> 1;
        Matrix transformation = scaleImage(bmp);
        canvas.drawBitmap(bmp, cx,cy, paint);
        drawable = new BitmapDrawable(getResources(),alteredBitmap);
        //im_crop_image.setImageBitmap(alteredBitmap);
        Log.e("Imageview and bitmap heig", screen_width+"x"+screen_height);
        im_crop_image.setBackground(drawable);
    }

    private Matrix scaleImage(Bitmap bitmap){
        float originalWidth = bitmap.getWidth();
        float originalHeight = bitmap.getHeight();
        float scale = screen_width/originalWidth;
        float xTranslation = 0.0f;
        float yTranslation = (screen_height - originalHeight * scale) / 2.0f;
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        return transformation;
    }
    private void startNewActivity(){
        String ImagePath = ImageUtilities.encodeImage(alteredBitmap, Bitmap.CompressFormat.PNG,100);
        Intent intent = new Intent(CropActivity.this, FiltersActivity.class);
        intent.putExtra("bitmap_CropActivity",ImagePath);
        startActivity(intent);
    }
}

