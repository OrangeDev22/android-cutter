package com.example.cutter.fragments;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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

import com.example.cutter.EditActivity;
import com.example.cutter.R;
import com.example.cutter.adapters.FontsAdapter;
import com.example.cutter.adapters.TextToolsAdapter;
import com.example.cutter.views.CustomTextViewOutline;
import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;


import java.util.ArrayList;
import java.util.List;


public class TextEditorDialogFragment extends DialogFragment implements FontsAdapter.OnItemSelected{

    public static final String TAG = TextEditorDialogFragment.class.getSimpleName();
    public static final String EXTRA_INPUT_TEXT = "extra_input_text";
    public static final String EXTRA_COLOR_CODE = "extra_color_code";
    private SeekBar seekBarSize;
    public CustomTextViewOutline mAddTextEditText;
    private FontsAdapter fontAdapter;
    private InputMethodManager mInputMethodManager;
    private List<String> colors;
    private int mDefaultColor;
    private int mDefaultStrokeColor;
    private int gradientColor=Color.GREEN;
    private int returnedColor;
    private String colorMode="TEXT_COLOR";
    private String textColor = "TEXT_COLOR",gradient="GRADIENT_COLOR";
    private TextEditor mTextEditor;
    private RecyclerView /*bottomMenu,*/ bottomOptions;
    public static int fontPosition=0;
    private static Context mContext;
    private ImageView imageViewDone,imageViewTextColor,imageViewStroke, imageViewTextGradient, imageViewCloseDialog;
    List<Typeface> fontsList;
    int backgroundColor;
    private float percentage=0;
    private static boolean hasStarted=false;


    @Override
    public void onFontSelected(int position) {
        mAddTextEditText.setTypeface(fontsList.get(position));
        fontPosition = position;
        fontAdapter.setSelectedFont(fontPosition);

    }



    public interface TextEditor {

        void onDone(String inputText, int colorCode, int backgroundColor, Typeface typeface, CustomTextViewOutline strokedEditText);
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
    public static TextEditorDialogFragment show(@NonNull AppCompatActivity appCompatActivity,int theme) {
        //mContext = context;
        //appCompatActivity.setTheme(theme);
        return show(appCompatActivity,
                "", ContextCompat.getColor(appCompatActivity, R.color.white));

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME,R.style.fragmentNavBarColor);
        //fragment.setStyle(DialogFragment.STYLE_NO_TITLE,R.style.fragmentNavBarColor);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        //Make dialog full screen with transparent background
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            //getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_text_dialog, container, false);

        //return inflater.from(mContext.getApplicationContext()).inflate(R.layout.add_text_dialog,null);
        //return getActivity().getLayoutInflater().inflate(R.layout.add_text_dialog,null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.fragmentNavBarColor);

        return super.onCreateDialog(savedInstanceState);
        //return builder.create();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        fontPosition = 0;
        colors = new ArrayList<>();
        fontsList = new ArrayList<>();
        fillFontList();
        //initialise the views
        mAddTextEditText = view.findViewById(R.id.add_text_edit_text2);
        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        //bottomMenu = view.findViewById(R.id.botto);
        bottomOptions = view.findViewById(R.id.bottomFonts);
        seekBarSize = view.findViewById(R.id.seekBarOutline);
        imageViewDone = view.findViewById(R.id.imageViewDone);
        //seekBarSize.setVisibility(View.GONE);
        imageViewTextColor = view.findViewById(R.id.imageViewTextColor);
        imageViewStroke = view.findViewById(R.id.imageViewTextStroke);
        imageViewTextGradient = view.findViewById(R.id.imageViewTextGradient);
        imageViewCloseDialog = view.findViewById(R.id.image_view_close_dialog);
        setImageViewsClickListeners();
        mAddTextEditText.setDrawingCacheEnabled(true);

        seekBarSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percentage = (float) (6*progress)/100;
                mAddTextEditText.setStrokeWidth(percentage);
                /*Log.e("stroke width", percentage+"");
                Log.e("seekbar_progress", progress+"");*/
                mAddTextEditText.invalidate();
                //mAddTextEditText.setText(textMessage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        LinearLayoutManager llmTools = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        fontAdapter = new FontsAdapter(this,fontsList, mContext);
        mDefaultColor = getResources().getColor(R.color.white);
        //mAddTextEditText.setTypeface(fontsList.get(0));
        mDefaultStrokeColor =  getResources().getColor(R.color.black);
        mAddTextEditText.setStrokeColor(mDefaultStrokeColor);
        mAddTextEditText.setTextColor(mDefaultColor);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        bottomOptions.setLayoutManager(mLayoutManager);
        bottomOptions.setAdapter(fontAdapter);
        mAddTextEditText.setFocusable(true);
        //openKeyboard();

    }

    private void setImageViewsClickListeners() {
        imageViewTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(colorMode.equals(gradient)){
                    mAddTextEditText.setStrokeWidth(percentage);
                    mAddTextEditText.invalidate();
                    colorMode = textColor;
                }
                openColorPicker(mDefaultColor);
            }
        });
        imageViewStroke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorPickerDialog.Builder(mContext)
                        .setTitle("Select stroke color")
                        .setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton("OK",
                                new ColorEnvelopeListener() {
                                    @Override
                                    public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {

                                        //mAddTextEditText.setOutlineColor(envelope.getColor());
                                        backgroundColor = envelope.getColor();
                                        //mAddTextEditText.setBackgroundColor(backgroundColor);
                                        mAddTextEditText.setStrokeColor(backgroundColor);
                                        mAddTextEditText.invalidate();
                                    }
                                })
                        .setNegativeButton("FUCK U",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                        .attachAlphaSlideBar(true) // the default value is true.
                        .attachBrightnessSlideBar(true)// the default value is true.
                        .setBottomSpace(12).show();// set a bottom space between the last slidebar and buttons.
            }
        });
        imageViewTextGradient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorMode = gradient;
                openColorPicker(gradientColor);
            }
        });
        imageViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = mAddTextEditText.getText().toString();
                if (!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                    mAddTextEditText.clearFocus();
                    mAddTextEditText.clearComposingText();
                    mTextEditor.onDone(inputText, mDefaultColor,backgroundColor, mAddTextEditText.getTypeface(), mAddTextEditText);
                }
                dismiss();
            }
        });
        imageViewCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


    public void setOnTextEditorListener(TextEditor textEditor) {
        mTextEditor = textEditor;
    }
    @Override
    public View getView() {
        return super.getView();
    }


    private void openColorPicker(int backgroundColor){
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.color_picker_dialog);
        final ImageView imageView = dialog.findViewById(R.id.previewColor);
        imageView.getBackground().setColorFilter(backgroundColor,PorterDuff.Mode.MULTIPLY);
        final ColorPicker colorPicker = dialog.findViewById(R.id.colorPicker);
        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener(){
            @Override
            public void onColorSelected(int color) {
                super.onColorSelected(color);
                imageView.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                if(colorMode.equals(textColor)){
                    mDefaultColor = color;
                    mAddTextEditText.getPaint().setShader(null);
                    mAddTextEditText.setTextColor(color);
                }
                else if(colorMode.equals(gradient)){
                    if(percentage == 0){
                        mAddTextEditText.setStrokeWidth(1);
                    }
                    gradientColor = color;
                    setGradient();
                    //mAddTextEditText.invalidate();
                }


            }

        });
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_transparent)));
        dialog.show();

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fillFontList(){
        fontsList.add(getResources().getFont(R.font.aligator));
        fontsList.add(getResources().getFont(R.font.all_the_roll_personal_use));
        fontsList.add(getResources().getFont(R.font.antonellie_callygraphy_demo));
        fontsList.add(getResources().getFont(R.font.autography));
        fontsList.add(getResources().getFont(R.font.bastball));
        fontsList.add(getResources().getFont(R.font.batmfa));
        fontsList.add(getResources().getFont(R.font.better_you_smile));
        fontsList.add(getResources().getFont(R.font.black_robins));
        fontsList.add(getResources().getFont(R.font.blacksword));
        fontsList.add(getResources().getFont(R.font.blowbrush));
        fontsList.add(getResources().getFont(R.font.brighton_spring));
        fontsList.add(getResources().getFont(R.font.brigitter_eigner));
        fontsList.add(getResources().getFont(R.font.cherolina));
        fontsList.add(getResources().getFont(R.font.cute_gorilla));
        fontsList.add(getResources().getFont(R.font.cute_pinkies));
        fontsList.add(getResources().getFont(R.font.cyberpunks));
        fontsList.add(getResources().getFont(R.font.dimbo_regular));
        fontsList.add(getResources().getFont(R.font.ds_digi));
        fontsList.add(getResources().getFont(R.font.firestarter));
        fontsList.add(getResources().getFont(R.font.gabrwffr));
        fontsList.add(getResources().getFont(R.font.godofwar));
        fontsList.add(getResources().getFont(R.font.internet_friend));
        fontsList.add(getResources().getFont(R.font.japanese));
        fontsList.add(getResources().getFont(R.font.mangat));
        fontsList.add(getResources().getFont(R.font.marline_free));
        fontsList.add(getResources().getFont(R.font.outrun));
        fontsList.add(getResources().getFont(R.font.powerpuff_girls_font));
        fontsList.add(getResources().getFont(R.font.rhodeport_regular));
        fontsList.add(getResources().getFont(R.font.rockies));
        fontsList.add(getResources().getFont(R.font.space_age));
        fontsList.add(getResources().getFont(R.font.spongeboy_me));
        fontsList.add(getResources().getFont(R.font.vampire_wars));
        fontsList.add(getResources().getFont(R.font.vcr));
        fontsList.add(getResources().getFont(R.font.ved_relret));
        fontsList.add(getResources().getFont(R.font.zeldadxt));
        fontAdapter = new FontsAdapter(this,fontsList, mContext);
    }
    private void setGradient(){
        Rect rect = new Rect();
        Paint textPaint = mAddTextEditText.getPaint();
        textPaint.getTextBounds(mAddTextEditText.getText().toString(),0,mAddTextEditText.getText().length(),rect);
        int width = rect.width();
        Log.e("text_view_dims",width+"-"+mAddTextEditText.getWidth());
        Shader textShader=new LinearGradient(0, 0, width, mAddTextEditText.getTextSize(),
                new int[]{mDefaultColor,gradientColor},
                null, Shader.TileMode.CLAMP);
        mAddTextEditText.getPaint().setShader(textShader);
        mAddTextEditText.invalidate();
    }
}