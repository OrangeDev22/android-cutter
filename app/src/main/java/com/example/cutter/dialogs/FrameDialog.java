package com.example.cutter.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cutter.R;
import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;

public class FrameDialog extends DialogFragment {
    //private ImageView selectFrameColor;
    private SeekBar seekBarFrameWidth,seekBarFrameCorners;
    public onFrameWidthListener listener;
    private int selectedColor,frameWidth=0,dpCorners=0;
    private boolean isShowing = false;
    public FrameDialog(onFrameWidthListener listener){
        this.listener = listener;
        isShowing = true;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_frame_dialog,null);
        builder.setView(view)
                .setTitle("");
        //selectFrameColor = view.findViewById(R.id.image_view_select_frame_color);
        seekBarFrameWidth = view.findViewById(R.id.seek_bar_frame_width);
        seekBarFrameCorners = view.findViewById(R.id.seek_bar_frame_corners);
        //set Progress 1 due to UI glitch on android 10
        if(dpCorners == 0){
            seekBarFrameCorners.setProgress(1);
        }else{
            seekBarFrameCorners.setProgress(dpCorners);
        }
        seekBarFrameCorners.invalidate();
        if(frameWidth == 0){
            seekBarFrameWidth.setProgress(1);
        }else{
            seekBarFrameWidth.setProgress(frameWidth);
        }
        seekBarFrameWidth.invalidate();
        seekBarFrameWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                frameWidth = progress;
                listener.onWidthChange(frameWidth,selectedColor,dpCorners);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarFrameCorners.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dpCorners = progress;
                listener.onWidthChange(frameWidth,selectedColor,dpCorners);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.dimAmount = 0;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    public interface onFrameWidthListener {
        void onWidthChange(int width, int color,int dpCorners);
    }
    public onFrameWidthListener getListener(){
        return listener;
    }
    @Override
    public void onDestroyView() {
        isShowing = false;
        super.onDestroyView();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
    public void setSelectFrameColor(int color){
        selectedColor = color;
    }
    public void setFrameWidth(int width){
        frameWidth = width;
    }
    public void setFrameCorners(int dpCorners){
        this.dpCorners = dpCorners;
    }
}
