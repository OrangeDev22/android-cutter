package com.example.cutter.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.R;
import com.example.cutter.tools.ToolType;

public class SubFilterDialog extends DialogFragment {
    private SeekBar seekbar;;
    private onSubFilterListener listener;
    private ToolType toolType;
    private TextView textView;
    private String message="value";
    private Button positiveButton, negativeButton;
    private int brightness=0;
    private float contrast = 1.0f, saturation = 1.0f;
    private boolean apply=false;
    public SubFilterDialog(onSubFilterListener listener){
        this.listener = listener;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.fragmentNavBarColor);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_value_dialog,null);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false);
        seekbar = view.findViewById(R.id.seekbar_sub_filter);
        textView = view.findViewById(R.id.text_view_sub_filter_value);
        setSeekBar();
        positiveButton = view.findViewById(R.id.dialog_sub_filter_positive_button);
        negativeButton = view.findViewById(R.id.dialog_sub_filter_negative_button);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apply = true;
                dismiss();
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (toolType == ToolType.BRIGHTNESS){
                    textView.setText((progress-100)+"");
                    brightness = progress-100;
                    listener.onBrightnessChanged(brightness);
                }else if(toolType == ToolType.CONTRAST){
                    //textView.setText(message+": "+(progress-100));
                    //progress+=100;
                    contrast = (progress*0.001f);
                    if(contrast >= 0.2f){
                        listener.onContrastChanged(contrast);
                    }
                    textView.setText(((progress/10)-100)+"");

                }
                else{
                    saturation = (progress*0.001f);
                    listener.onSaturationChanged(saturation);
                    textView.setText(((progress/10)-100)+"");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setView(view)
                .setTitle("");
        return builder.create();
    }
    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.dimAmount = 0;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(),R.color.colorPrimary)));
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    public interface onSubFilterListener{
        void onBrightnessChanged(int brightness);
        void onContrastChanged(float contrast);
        void onSaturationChanged(float saturation);
        void applyBrightness(int brightness,boolean apply);
        void applyContrast(float contrast, boolean apply);
        void applySaturation(float saturation,boolean apply);
    }
    public void setToolType(ToolType toolType){
        this.toolType = toolType;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            //listener = (onSubFilterListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement onSubFilterListener");
        }
    }

    private void setSeekBar(){
        switch (toolType){
            case BRIGHTNESS:
                seekbar.setMax(200);
                seekbar.setProgress(100);
                break;
            case CONTRAST:
            case SATURATION:
                seekbar.setMax(2000);
                seekbar.setProgress(1000);
                break;
        }
    }

    @Override
    public void onDestroy() {
        switch (toolType){
            case BRIGHTNESS:
                listener.applyBrightness(brightness,apply);
                break;
            case CONTRAST:
                listener.applyContrast(contrast,apply);
                break;
            case SATURATION:
                listener.applySaturation(saturation,apply);
                break;
        }
        super.onDestroy();
    }
}
