package com.example.cutter.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;
import com.example.cutter.R;


public class CustomTextViewOutline  extends EditText {

    private static final int DEFAULT_STROKE_WIDTH = 0;

    // fields
    private int _strokeColor;
    private float _strokeWidth;
    private int _hintStrokeColor;
    private float _hintStrokeWidth;
    private boolean isDrawing;
    private Bitmap altBitmap;
    private Canvas altCanvas;

    public CustomTextViewOutline(Context context) {
        this(context, null);
    }

    public CustomTextViewOutline(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public CustomTextViewOutline(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }


    public CustomTextViewOutline(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrokedTextAttrs);
            _strokeColor = a.getColor(R.styleable.StrokedTextAttrs_textStrokeColor,
                    getCurrentTextColor());
            _strokeWidth = a.getFloat(R.styleable.StrokedTextAttrs_textStrokeWidth,
                    DEFAULT_STROKE_WIDTH);
            _hintStrokeColor = a.getColor(R.styleable.StrokedTextAttrs_textHintStrokeColor,
                    getCurrentHintTextColor());
            _hintStrokeWidth = a.getFloat(R.styleable.StrokedTextAttrs_textHintStrokeWidth,
                    DEFAULT_STROKE_WIDTH);

            a.recycle();
        } else {
            _strokeColor = getCurrentTextColor();
            _strokeWidth = DEFAULT_STROKE_WIDTH;
            _hintStrokeColor = getCurrentHintTextColor();
            _hintStrokeWidth = DEFAULT_STROKE_WIDTH;
        }
        setStrokeWidth(_strokeWidth);
        setHintStrokeWidth(_hintStrokeWidth);
    }

    @Override
    public void invalidate() {
        // Ignore invalidate() calls when isDrawing == true
        // (setTextColor(color) calls will trigger them,
        // creating an infinite loop)
        if(isDrawing) return;
        super.invalidate();
    }

    public void setHintStrokeColor(int color) {
        _hintStrokeColor = color;
    }

    public void setHintStrokeWidth(float width) {
        //convert values specified in dp in XML layout to
        //px, otherwise stroke width would appear different
        //on different screens
        _hintStrokeWidth = spToPx(getContext(), width);
    }

    public void setHintStrokeWidth(int unit, float width) {
        _hintStrokeWidth = TypedValue.applyDimension(
                unit, width, getContext().getResources().getDisplayMetrics());
    }

    public void setStrokeColor(int color) {
        _strokeColor = color;
    }

    public void setStrokeWidth(float width) {
        //convert values specified in dp in XML layout to
        //px, otherwise stroke width would appear different
        //on different screens
        _strokeWidth = width;
    }

    public void setStrokeWidth(int unit, float width) {
        _strokeWidth = TypedValue.applyDimension(
                unit, width, getContext().getResources().getDisplayMetrics());
    }

    // overridden methods

    @Override
    protected void onDraw(Canvas canvas) {
        boolean paintHint = getHint() != null && getText().length() == 0;
        if((paintHint && _hintStrokeWidth > 0) || (!paintHint && _strokeWidth > 0)) {
            isDrawing = true;
            if(altBitmap == null) {
                altBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                altCanvas = new Canvas(altBitmap);
            } else if(altCanvas.getWidth() != canvas.getWidth() ||
                    altCanvas.getHeight() != canvas.getHeight()) {
                altBitmap.recycle();
                altBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                altCanvas.setBitmap(altBitmap);
            }
            //draw the fill part of text
            super.onDraw(canvas);
            //save the text color
            int currentTextColor = paintHint ? getCurrentHintTextColor() : getCurrentTextColor();
            //clear alternate canvas
            altBitmap.eraseColor(Color.TRANSPARENT);
            //set paint to stroke mode and specify
            //stroke color and width
            //Paint p = getPaint();
            Shader shader = getPaint().getShader();
            Paint p = getPaint();
            if(shader != null){
                  p.setShader(null);
            }

            p.setStyle(Paint.Style.STROKE);
            if(paintHint) {
                p.setStrokeWidth(_hintStrokeWidth);
                setHintTextColor(_hintStrokeColor);
            } else {
                p.setStrokeWidth(_strokeWidth);
                setTextColor(_strokeColor);
            }
            //draw text stroke
            super.onDraw(altCanvas);
            canvas.drawBitmap(altBitmap, 0, 0, null);
            getPaint().setShader(shader);
            //revert the color back to the one
            //initially specified
            if(paintHint) {
                setHintTextColor(currentTextColor);
            } else {
                setTextColor(currentTextColor);
            }
            //set paint to fill mode (restore)
            p.setStyle(Paint.Style.FILL);
            isDrawing = false;
        } else {
            super.onDraw(canvas);
        }
    }
    protected static int spToPx(Context context, float sp) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;

        return (int) (sp * scale + 0.5f);
    }

    public Bitmap getAltBitmap(){
        return altBitmap;
    }
    public float getStrokeWidth(){
        return _strokeWidth;
    }
}