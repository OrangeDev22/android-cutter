package com.example.cutter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.cutter.adapters.CropToolsAdapter;
import com.example.cutter.adapters.EditToolsAdapter;
import com.example.cutter.constants.Constants;
import com.example.cutter.tools.ToolType;
import com.example.cutter.utils.ImageUtilities;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CropperActivity extends AppCompatActivity implements CropToolsAdapter.onToolListener {
    private Bitmap bitmap;
    private CropImageView cropImageView;
    private ImageUtilities imageUtilities;
    private Toolbar toolbar;
    private Rect rect;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;
    /*private SeekBar seekBarAngle;
    private TextView textViewAngle;*/
    private int angle = 0;
    String ImagePath;
    private Boolean changeRect = true;
    private CropImageTask task;
    private Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);
        imageUtilities = new ImageUtilities();
        cropImageView = findViewById(R.id.cropImageView);
        toolbar = findViewById(R.id.toolbar_cropper);;
        recyclerView = findViewById(R.id.bottomMenuRecyclerViewCropper);
        CropToolsAdapter adapter = new CropToolsAdapter(this, CropperActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.GONE);
        bottomNav = findViewById(R.id.bottomNavigationViewCropper);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.bottomCropSquare);
        setSupportActionBar(toolbar);
        final TextView textView = toolbar.findViewById(R.id.toolbar_title);
        textView.setText(getString(R.string.tool_bar_crop_title));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        String imagePath = getIntent().getExtras().getString(Constants.INTENT_EXTRA_MAIN_ACTIVITY);
        bitmap = ImageUtilities.decodeImage(imagePath);
        cropImageView.setImageBitmap(bitmap);

        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {

            }
        });
        rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        cropImageView.getCroppedImageAsync();
        cropImageView.setGuidelines(CropImageView.Guidelines.ON);
        cropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
        cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        cropImageView.setAutoZoomEnabled(true);
        cropImageView.setShowProgressBar(true);
        cropImageView.setCropRect(rect);

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

                case R.id.bottomCropSquare:
                    cropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
                    if (recyclerView.getVisibility() == View.VISIBLE){
                        recyclerView.setVisibility(View.GONE);
                    }
                    return true;
                case R.id.bottomCropOval:
                    cropImageView.setCropShape(CropImageView.CropShape.OVAL);
                    if (recyclerView.getVisibility() == View.VISIBLE){
                        recyclerView.setVisibility(View.GONE);
                    }
                    return true;
                case R.id.bottomAspectRatio:
                    recyclerView.setVisibility(View.VISIBLE);
                    return false;
                default:
                    return true;
            }

        }
    };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bitmap bitmap;
        switch (item.getItemId()){
            case R.id.action_flip:
                cropImageView.flipImageHorizontally();
                return true;
            case R.id.action_rotate_right:
                //seekBarAngle.setProgress(angle-);
                cropImageView.rotateImage(90);
                return  true;
            case R.id.action_rotate_left:
                cropImageView.rotateImage(-90);
                return true;
            case R.id.action_crop:
                Bitmap croppedBitmap = cropImageView.getCroppedImage();
                StartAsyncTaskCropImage(croppedBitmap);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onToolSelected(ToolType toolType) {
        switch (toolType){
            case ONEBYONE:
                cropImageView.setAspectRatio(1,1);
                break;
            case THREEBYFOUR:
                cropImageView.setAspectRatio(3,4);
                break;
            case FOURBYTHREE:
                cropImageView.setAspectRatio(4,3);
                break;
            case FIVEBYFOUR:
                cropImageView.setAspectRatio(5,4);
                break;
            case SIXTEENBYNINE:
                cropImageView.setAspectRatio(16,9);
                break;
            case NINEBYSIXTEEN:
                cropImageView.setAspectRatio(9,16);
                break;
            case SELECTALL:
                cropImageView.clearAspectRatio();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (recyclerView.getVisibility() == View.VISIBLE){
            recyclerView.setVisibility(View.GONE);
        }
        else{
            super.onBackPressed();
        }
    }
    private void StartAsyncTaskCropImage(Bitmap bitmap){
        task = new CropImageTask(this);
        dialog = ProgressDialog.show(CropperActivity.this, "",
                getResources().getString(R.string.crop_activity_progress_dialog_message), true);
        task.execute(bitmap);
    }
    private static class CropImageTask extends AsyncTask<Bitmap,Void,Void> {
        private CropperActivity activity;
        CropImageTask(CropperActivity activity){
            this.activity = activity;
        }
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            activity.ImagePath = ImageUtilities.encodeImage(bitmaps[0], Bitmap.CompressFormat.PNG,100);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            activity.dialog.dismiss();
            activity.startNewActivity();
            activity.finish();
            super.onPostExecute(aVoid);
        }
    }
    private void startNewActivity(){
        Intent intent = new Intent(CropperActivity.this, EditActivity.class);
        //intent.putExtra("bitmap_CropActivity",ImagePath);
        intent.putExtra("bitmap_CropActivity",ImagePath);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        if(task!= null){
            task.cancel(true);
        }
        super.onDestroy();
    }

}