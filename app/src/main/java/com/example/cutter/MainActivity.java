package com.example.cutter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.cutter.constants.Constants;
import com.example.cutter.utils.BitmapUtils;
import com.example.cutter.utils.ImageUtilities;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openGallery = findViewById(R.id.image_view_gallery);
        openCamera = findViewById(R.id.image_view_camera);
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                Intent chooserIntent = Intent.createChooser(getIntent,"Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                //chooserIntent.putExtra("ACTION",Constants.addTrayIcon);
                startActivityForResult(chooserIntent,PICK_IMAGE);
            }
        });
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = "photo";
                File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try {
                    File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                    currentPhotoPath = imageFile.getAbsolutePath();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri = FileProvider.getUriForFile(MainActivity.this,"com.example.cutter.fileprovider",imageFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                    startActivityForResult(intent,CAMERA_REQUEST);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK){
            try {
                //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                Uri imageData = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);
                startNewActivity = true;
                //ImagePath = ImageUtilities.encodeImage(bitmap, Bitmap.CompressFormat.PNG,100);
                //startNewActivity();
                //bitmapToImageView(bitmap);
                startAsyncTask(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            bitmap =  BitmapFactory.decodeFile(currentPhotoPath);
            startNewActivity = true;
            startAsyncTask(bitmap);
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
            String imagePath = ImageUtilities.encodeImage(bitmaps[0], Bitmap.CompressFormat.PNG,100);
            return imagePath;
        }

        @Override
        protected void onPostExecute(String s) {
            activity.startNewActivity(s);
            activity.dialog.dismiss();
            super.onPostExecute(s);
        }
    }

    @Override
    protected void onDestroy() {
        if(task != null){
            task.cancel(true);
        }
        super.onDestroy();
    }
}