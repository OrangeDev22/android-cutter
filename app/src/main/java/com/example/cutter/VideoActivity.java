package com.example.cutter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.cutter.adapters.FramesPreviewAdapter;
import com.example.cutter.constants.Constants;
import com.example.cutter.utils.ImageUtilities;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class VideoActivity extends AppCompatActivity implements FramesPreviewAdapter.OnItemSelected {
    private VideoView videoView;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FramesPreviewAdapter framesPreviewAdapter;
    private SeekBar seekBar;
    private TextView textView;
    private List<Bitmap> previews;
    public static Uri path;
    private int REQUEST_TAKE_GALLERY_VIDEO =1;
    private ProgressDialog dialog;
    private int totalImagesToSave = 0;
    private int imagesSaved=0, seconds=0;
    String videoType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoView = findViewById(R.id.video_view);
        toolbar = findViewById(R.id.toolbar_video_activity);
        recyclerView = findViewById(R.id.bottomFramesPreviews);
        seekBar = findViewById(R.id.seek_bar_time);
        textView = findViewById(R.id.text_view_seconds);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView textView = toolbar.findViewById(R.id.toolbar_title);
        textView.setText("Convert Video to Image");
        videoType = getIntent().getExtras().getString("video_type");
        if(videoType.equals(Constants.AT_IMAGE_TIME)){
            initVideoAtImageTime();
        }
        else if(videoType.equals(Constants.AT_TIME_INTERVAL)){
            initAtTimeInterval();
        }
        String videoPath = "android.resource://" + getPackageName() + "/"/* +*R.raw.tangerine*/ ;
        path = Uri.parse(videoPath);
        videoView.setVideoURI(path);
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        mediaController.setAnchorView(videoView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_toolbar_video,menu);
        if(videoType.equals(Constants.AT_TIME_INTERVAL)){
            MenuItem menuItem = menu.findItem(R.id.action_create_image_video);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_create_image_video:
                videoView.buildDrawingCache();
                Bitmap bitmap = createVideoThumbnail(getApplicationContext(),path,videoView.getCurrentPosition());
                previews.add(bitmap);
                framesPreviewAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_load_file:
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);
                return true;
            case R.id.action_done_video:
                if(videoType.equals(Constants.AT_IMAGE_TIME)){
                    SaveImagesToStorage task = new SaveImagesToStorage(VideoActivity.this);
                    totalImagesToSave = previews.size();
                    task.execute(previews);
                }else{
                    createAtTimeIntervalTask task = new createAtTimeIntervalTask(VideoActivity.this);
                    task.execute();
                    totalImagesToSave = (int)(videoView.getDuration()/(seconds*1000))+2;
                }
                dialog = ProgressDialog.show(VideoActivity.this, "",
                        imagesSaved+" images saved out of " + totalImagesToSave+" please wait... ", true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initVideoAtImageTime(){
        seekBar.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        previews = new ArrayList<>();
        framesPreviewAdapter = new FramesPreviewAdapter(this,previews,VideoActivity.this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(VideoActivity.this, RecyclerView.HORIZONTAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(framesPreviewAdapter);
    }
    private void initAtTimeInterval(){
        recyclerView.setVisibility(View.GONE);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seconds = progress/10;
                textView.setText(seconds+" seconds");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri videoUri = data.getData();
                path = data.getData();
                videoView.setVideoURI(videoUri);
                videoView.requestFocus();
                videoView.start();
            }
        }
    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }



    protected void previewVideo(File file) {
        videoView.setVideoPath(file.getAbsolutePath());

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);
        videoView.setVisibility(View.VISIBLE);
        videoView.start();
    }
    public  Bitmap createVideoThumbnail(Context context, Uri uri, long time)
    {
        MediaMetadataRetriever mediametadataretriever = new MediaMetadataRetriever();
        try {
            mediametadataretriever.setDataSource(context, uri);
            long pos = time*1000;
            Bitmap bitmap = mediametadataretriever.getFrameAtTime(pos, MediaMetadataRetriever.OPTION_CLOSEST);
            /*if(null != bitmap)
            {
                //int j = getThumbnailSize(context, i);
                return ThumbnailUtils.extractThumbnail(bitmap, videoView.getWidth(), videoView.getHeight(), 2);
            }*/
            return bitmap;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        } finally {
            try
            {
                mediametadataretriever.release();
            }
            catch(RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPreviewSelected(int position) {

    }

    @Override
    public void onRemoveButtonSelected(int position) {
        //List<>
        previews.remove(position);
        LinkedHashSet<Bitmap> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.addAll(previews);
        previews.clear();
        previews.addAll(linkedHashSet);
        framesPreviewAdapter.notifyDataSetChanged();
    }
    private static class SaveImagesToStorage extends AsyncTask<List<Bitmap>,String , Boolean>{
        private WeakReference<VideoActivity> activityWeakReference;
        private VideoActivity videoActivity;
        private int imagesSaved = 0;
        SaveImagesToStorage(VideoActivity videoActivity){
            this.videoActivity = videoActivity;
        }

        @Override
        protected Boolean doInBackground(List<Bitmap>... lists) {
            List<Bitmap> previews = lists[0];
            for(Bitmap bitmapDrawable : previews){
               // ImageUtilities.saveAsFile(".png",bitmapDrawable,videoActivity.getApplicationContext());
                imagesSaved++;
                publishProgress();
            }
            return true;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            videoActivity.dialog.setMessage(imagesSaved+" images saved out of " + videoActivity.totalImagesToSave+" please wait... ");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            videoActivity.dialog.dismiss();
            videoActivity.finish();

        }
    }
    private class createAtTimeIntervalTask extends AsyncTask<Void,Integer,Boolean>{
        private VideoActivity activity;
        private int ImagesSaved=1;
        private int totalImages,seconds;
        private long duration,position = 0;
        public createAtTimeIntervalTask(VideoActivity activity){
            this.activity = activity;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Bitmap bitmap;
            for(int i = 0; i < totalImages; i++){
                Log.e("time_inteverl_task",position+"");
                if(position < duration){
                    bitmap = activity.createVideoThumbnail(activity.getApplicationContext(),activity.path,position);
                }else{
                    bitmap = activity.createVideoThumbnail(activity.getApplicationContext(),activity.path,duration);
                }
                //ImageUtilities.saveAsFile(".png",bitmap,activity.getApplicationContext());
                position += seconds;
                publishProgress();
                imagesSaved++;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            seconds = activity.seconds *1000;
            duration = activity.videoView.getDuration();
            totalImages = (int) (duration/seconds)+2;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            activity.dialog.dismiss();
            activity.finish();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            activity.dialog.setMessage(imagesSaved+" images saved out of " + totalImages+" please wait... ");
        }
    }
}