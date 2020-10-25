package com.example.cutter.constants;

import android.os.Environment;

public class Constants {
    public final static String AT_IMAGE_TIME ="at_image_time", AT_TIME_INTERVAL = "at_time_interval";
    public final static String INTENT_EXTRA_MAIN_ACTIVITY="_main_activity";
    public final static String savedImagesPath = Environment.getExternalStorageDirectory().toString()+"/noPornPictures";
    public final static String INTENT_EXTRA_WIDTH ="_crop_original_width", INTENT_EXTRA_HEIGHT="_crop_original_height",INTENT_EXTRA_ALL = "_all_selected";
    public final static String FRAME_DIALOG="_frame_dialog";
    public final static int SLEEP_TIME_THREADS_MESSAGES=7000;
}
