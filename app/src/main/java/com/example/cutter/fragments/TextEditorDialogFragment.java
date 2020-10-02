package com.example.cutter.fragments;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cutter.R;
import com.example.cutter.adapters.FontsAdapter;
import com.example.cutter.adapters.TextToolsAdapter;
import com.example.cutter.views.CustomTextViewOutline;


import java.util.ArrayList;
import java.util.List;


public class TextEditorDialogFragment extends DialogFragment implements FontsAdapter.OnItemSelected, TextToolsAdapter.OnItemSelected{

    public static final String TAG = TextEditorDialogFragment.class.getSimpleName();
    public static final String EXTRA_INPUT_TEXT = "extra_input_text";
    public static final String EXTRA_COLOR_CODE = "extra_color_code";
    private SeekBar seekBarSize;
    private SeekBar seekBarAlpha;
    public CustomTextViewOutline mAddTextEditText;
    private FontsAdapter fontAdapter;
    private InputMethodManager mInputMethodManager;
    public int mColorCode;
    private List<String> colors;
    private int mDefaultColor;
    private int mDefaultStrokeColor;
    private TextEditor mTextEditor;
    private RecyclerView bottomMenu, bottomOptions;
    public static int fontPosition=0;
    private int colorSelected = 1;
    private int strokeColorSelected = 8;
    private EditText editText;
    private static Context mContext;
    private ImageView imageTransparency, imageViewSize;
    Boolean isStyleSelected=false;
    Boolean isColorSelected = false;
    private String textMessage="Text Example";
    private TextToolsAdapter mEditingToolsAdapter;
    List<Typeface> fontsList;
    private int colorProgress, colorStrokeProgress;
    private String alphaChanelOutline = String.format("#%02x", 255);;
    private String alphaChanel = String.format("#%02x", 255);;


    @Override
    public void onToolSelected(String toolType) {

        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        RecyclerView.LayoutManager mLayoutManager;
        int deviceheihgt;
        DisplayMetrics displayMetrics;
        double perccentage;
        switch (toolType){

            case "TEXT":
                openKeyboard();
                bottomOptions.setVisibility(View.GONE);
                seekBarSize.setVisibility(View.GONE);
                seekBarAlpha.setVisibility(View.GONE);;
                imageViewSize.setVisibility(View.GONE);
                imageTransparency.setVisibility(View.GONE);

                mEditingToolsAdapter.setSelectedTool(0);
                break;
            case "FONT":
                displayMetrics = new DisplayMetrics();
                ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                deviceheihgt = displayMetrics.heightPixels;
                perccentage = deviceheihgt*0.45;
                deviceheihgt = (int) perccentage;
                bottomOptions.getLayoutParams().height= deviceheihgt;
                editText.requestFocus();
                editText.setFocusableInTouchMode(false);
                imm.hideSoftInputFromWindow(getView().getWindowToken(),0);
                mLayoutManager = new GridLayoutManager(mContext, 1);
                bottomOptions.setLayoutManager(mLayoutManager);
                bottomOptions.setAdapter(fontAdapter);
                bottomOptions.setVisibility(View.VISIBLE);
                seekBarSize.setVisibility(View.GONE);
                seekBarAlpha.setVisibility(View.GONE);
                imageViewSize.setVisibility(View.GONE);
                imageTransparency.setVisibility(View.GONE);
                isStyleSelected = false;
                isColorSelected=false;
                fontAdapter.setSelectedFont(fontPosition);
                mEditingToolsAdapter.setSelectedTool(1);
                break;
            case "COLOR":
                displayMetrics = new DisplayMetrics();
                ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                deviceheihgt = displayMetrics.heightPixels;
                perccentage = deviceheihgt*0.35;
                deviceheihgt = (int) perccentage;
                bottomOptions.getLayoutParams().height= deviceheihgt;
                isStyleSelected=false;
                editText.requestFocus();
                editText.setFocusableInTouchMode(false);
                imm.hideSoftInputFromWindow(getView().getWindowToken(),0);
                mLayoutManager = new GridLayoutManager(mContext, 6);
                bottomOptions.setLayoutManager(mLayoutManager);
                bottomOptions.setVisibility(View.VISIBLE);
                seekBarSize.setVisibility(View.GONE);
                seekBarAlpha.setVisibility(View.VISIBLE);
                imageViewSize.setVisibility(View.GONE);
                imageTransparency.setVisibility(View.VISIBLE);
                isColorSelected=true;
                seekBarAlpha.setProgress(colorProgress);
                mEditingToolsAdapter.setSelectedTool(2);
                break;
            case "STYLE":

                editText.requestFocus();
                editText.setFocusableInTouchMode(false);
                imm.hideSoftInputFromWindow(getView().getWindowToken(),0);
                seekBarSize.setVisibility(View.VISIBLE);
                seekBarAlpha.setVisibility(View.VISIBLE);
                mLayoutManager = new GridLayoutManager(mContext, 6);
                bottomOptions.setLayoutManager(mLayoutManager);
                bottomOptions.setVisibility(View.VISIBLE);
                imageViewSize.setVisibility(View.VISIBLE);
                imageTransparency.setVisibility(View.VISIBLE);
                isStyleSelected = true;
                seekBarAlpha.setProgress(colorStrokeProgress);
                displayMetrics = new DisplayMetrics();
                ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                deviceheihgt = displayMetrics.heightPixels;
                perccentage = deviceheihgt*0.25;
                deviceheihgt = (int) perccentage;
                bottomOptions.getLayoutParams().height= deviceheihgt;
                mEditingToolsAdapter.setSelectedTool(3);
                break;
            case "DONE":
                String inputText = mAddTextEditText.getText().toString();
                if (!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                    mTextEditor.onDone(inputText, mDefaultColor, mAddTextEditText.getTypeface(), mAddTextEditText);
                }
                dismiss();
                break;
        }


    }

    @Override
    public void onFontSelected(int position) {
        mAddTextEditText.setTypeface(fontsList.get(position));
        fontPosition = position;
        fontAdapter.setSelectedFont(fontPosition);

    }



    public interface TextEditor {

        void onDone(String inputText, int colorCode, Typeface typeface, CustomTextViewOutline strokedEditText);
        void onAdd(String inputText, int colorCode, Typeface typeface);
    }

    public void setmContext (Context context){
        mContext = context;

    }
    //Show dialog with provide text and text color
    public static TextEditorDialogFragment show(@NonNull AppCompatActivity appCompatActivity,
                                                @NonNull String inputText,
                                                @ColorInt int colorCode) {

        Bundle args = new Bundle();
        args.putString(EXTRA_INPUT_TEXT, inputText);
        args.putInt(EXTRA_COLOR_CODE, colorCode);
        TextEditorDialogFragment fragment = new TextEditorDialogFragment();
        fragment.setArguments(args);
        fragment.show(appCompatActivity.getSupportFragmentManager(), TAG);

        return fragment;
    }

    //Show dialog with default text input as empty and text color white
    public static TextEditorDialogFragment show(@NonNull AppCompatActivity appCompatActivity) {
        //mContext = context;
        return show(appCompatActivity,
                "", ContextCompat.getColor(appCompatActivity, R.color.white));

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        //Make dialog full screen with transparent background
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0);


        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_text_dialog, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int devicewidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;
        Log.d("DEVICE MEASURE", devicewidth+"x"+deviceHeight);

        mEditingToolsAdapter  = new TextToolsAdapter( this, mContext);
        fontPosition = 0;
        colorProgress = 100;
        colorStrokeProgress = 100;
        colors = new ArrayList<>();
        //starts filling up the font list
        //*******************************************************************************
        //initialise the views
        editText = view.findViewById(R.id.editTextInput);
        mAddTextEditText = view.findViewById(R.id.add_text_edit_text2);
        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        bottomMenu = view.findViewById(R.id.bottomMenu);
        bottomOptions = view.findViewById(R.id.bottomOptions);
        bottomOptions.setVisibility(View.GONE);
        seekBarSize = view.findViewById(R.id.seekBarOutline);
        seekBarAlpha = view.findViewById(R.id.seekBarAlpha);
        imageTransparency = view.findViewById(R.id.imageViewTransparency);
        imageViewSize = view.findViewById(R.id.imageViewSize);
        imageViewSize.setVisibility(View.GONE);
        imageTransparency.setVisibility(View.GONE);
        seekBarSize.setVisibility(View.GONE);
        seekBarAlpha.setVisibility(View.GONE);

        mAddTextEditText.setDrawingCacheEnabled(true);
        seekBarSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float percentage = (float) (6*progress)/100;
                mAddTextEditText.setOutlineWidth(percentage);

                mAddTextEditText.setText(textMessage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int percentage = (255*progress)/100;
                String hex = String.format("#%02x", percentage);
                String color ="";
                if (isStyleSelected)color = colors.get(strokeColorSelected);

                else color = colors.get(colorSelected);
                String finalColor = applyAplhaChanel(color, hex);

                if (isStyleSelected){
                    if (finalColor.length()>=1){
                        Log.d("Color transparency:", finalColor);
                        mAddTextEditText.setOutlineColor(Color.parseColor(finalColor));
                        colorStrokeProgress = progress;
                        mAddTextEditText.setText(textMessage);
                        Log.d("Color Stroke Progress:",colorStrokeProgress+"");
                        Log.d("Color Progress:",colorProgress+"");
                        alphaChanelOutline = hex;
                    }

                }
                else {
                    if (finalColor.length()>=1){
                        Log.d("Color transparency:", finalColor);
                        mAddTextEditText.setTextColor(Color.parseColor(finalColor));
                        colorProgress=progress;
                        Log.d("Color Progress:",colorProgress+"");
                        Log.d("Color Stroke Progress:",colorStrokeProgress+"");
                        alphaChanel = hex;
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        LinearLayoutManager llmTools = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        bottomMenu.setLayoutManager(llmTools);
        bottomMenu.setAdapter(mEditingToolsAdapter);
        fontAdapter = new FontsAdapter(this,fontsList, mContext);
        mDefaultColor = getResources().getColor(R.color.white);
        //mAddTextEditText.setTypeface(fontsList.get(0));
        mDefaultStrokeColor =  getResources().getColor(R.color.black);
        mAddTextEditText.setOutlineColor(mDefaultStrokeColor);
        mAddTextEditText.setTextColor(mDefaultColor);
        mColorCode = getArguments().getInt(EXTRA_COLOR_CODE);
        mAddTextEditText.setFocusable(true);
        openKeyboard();

    }



    //Method that will change the transparency for the colors
    private String applyAplhaChanel(String color, String hex){
        String finalColor="";
        for (int i = 1; i < color.length();i++){
            finalColor += color.charAt(i)+"";
        }
        finalColor = hex+finalColor;
        Log.d("Final Color", finalColor);
        return  finalColor;
    }
    public void setOnTextEditorListener(TextEditor textEditor) {
        mTextEditor = textEditor;
    }
    @Override
    public View getView() {
        return super.getView();


    }

    public void openKeyboard(){

        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
        editText.setCursorVisible(false);
        editText.requestFocus();
        editText.setFocusableInTouchMode(true);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                textMessage = s.toString();
                mAddTextEditText.setText(textMessage);
            }
        });
        mEditingToolsAdapter.setSelectedTool(0);
    }
}