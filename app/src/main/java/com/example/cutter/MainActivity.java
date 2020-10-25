package com.example.cutter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.cutter.constants.Constants;
import com.example.cutter.utils.BitmapUtils;
import com.example.cutter.utils.ImageUtilities;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ImageView openGallery, openCamera;
    private static final int PICK_IMAGE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private String currentPhotoPath,ImagePath;
    private Dialog dialog;
    private Bitmap bitmap;
    private boolean startNewActivity=false;
    private StartNewActivityTask task;
    private Uri image;
    private AdView adView;
    private InterstitialAd interstitialAd;
    int STORAGE_PERMISSION_CODE = 0;
    private boolean imageLoaded=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        interstitialAd.loadAd(new AdRequest.Builder().build());
        openGallery = findViewById(R.id.image_view_gallery);
        openCamera = findViewById(R.id.image_view_camera);

        if (!checkPermission()){
            requestPermissions();
        }
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(checkPermission()){
                  Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                  getIntent.setType("image/*");
                  Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                  pickIntent.setType("image/*");
                  Intent chooserIntent = Intent.createChooser(getIntent,"Select Image");
                  chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                  //chooserIntent.putExtra("ACTION",Constants.addTrayIcon);
                  startActivityForResult(chooserIntent,PICK_IMAGE);
              }
              else{
                  requestPermissions();
              }
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission()){
                    String fileName = "photo";
                    File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    try {
                        File imageFile = File.createTempFile(fileName, ".png", storageDirectory);
                        currentPhotoPath = imageFile.getAbsolutePath();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //Uri uri = FileProvider.getUriForFile(MainActivity.this,"com.example.cutter.fileprovider",imageFile);
                        image = FileProvider.getUriForFile(MainActivity.this,"com.example.cutter.fileprovider",imageFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,image);
                        startActivityForResult(intent,CAMERA_REQUEST);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    requestPermissions();
                }

            }
        });
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
                            ActivityCompat.requestPermissions(MainActivity.this,
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
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK){
            /*try {
                //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                //Uri imageData = data.getData();
                image = data.getData();
                //bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);
                startNewActivity = true;
                //ImagePath = ImageUtilities.encodeImage(bitmap, Bitmap.CompressFormat.PNG,100);
                //startNewActivity();
                //bitmapToImageView(bitmap);
                startAsyncTask(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            image = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(bitmap!=null){
                imageLoaded = true;
                Intent intent = new Intent(MainActivity.this, CropperActivity.class);
                //intent.putExtra("bitmap_CropActivity",ImagePath);
                intent.putExtra(Constants.INTENT_EXTRA_MAIN_ACTIVITY,image.toString());
                startActivity(intent);
            }
            else{
                Toast.makeText(this,getResources().getString(R.string.null_image_message),Toast.LENGTH_LONG).show();
            }

        }else if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                /*bitmap =  BitmapFactory.decodeFile(currentPhotoPath);
                if(bitmap != null){

                }
                else{
                    Toast.makeText(this,getResources().getString(R.string.null_image_message),Toast.LENGTH_LONG).show();
                }*/
            imageLoaded = true;
            Intent intent = new Intent(MainActivity.this, CropperActivity.class);
            //intent.putExtra("bitmap_CropActivity",ImagePath);
            intent.putExtra(Constants.INTENT_EXTRA_MAIN_ACTIVITY,image.toString());
            startActivity(intent);
            startNewActivity = true;
            //startAsyncTask(bitmap);
            //ImagePath = ImageUtilities.encodeImage(bitmap, Bitmap.CompressFormat.PNG,100);
            //bitmapToImageView(bitmap);
        }

    }

    private void startNewActivity(String ImagePath){
        Intent intent = new Intent(MainActivity.this, CropperActivity.class);
        //intent.putExtra("bitmap_CropActivity",ImagePath);
        intent.putExtra(Constants.INTENT_EXTRA_MAIN_ACTIVITY,ImagePath);
        startActivity(intent);
    }
    private void startAsyncTask(Bitmap bitmap){
        dialog = ProgressDialog.show(this, "",
                getResources().getString(R.string.crop_activity_progress_dialog_message), true);
        task = new StartNewActivityTask(this);
        task.execute(bitmap);
    }
    private static class StartNewActivityTask extends AsyncTask<Bitmap,Void,String>{
        private MainActivity activity;
        StartNewActivityTask(MainActivity activity){
            this.activity = activity;
        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            //String imagePath = ImageUtilities.encodeImage(bitmaps[0], Bitmap.CompressFormat.PNG,100);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            activity.startNewActivity(s);
            activity.dialog.dismiss();
            super.onPostExecute(s);
        }
    }
    private Boolean checkPermission(){
        if(RequestPermissionsHelper.verifyPermissions(this)){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(imageLoaded){
            interstitialAd.show();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if(task != null){
            task.cancel(true);
        }
        super.onDestroy();
    }
}