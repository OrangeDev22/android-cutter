package com.example.cutter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.cutter.fragments.TextEditorDialogFragment;
import com.example.cutter.utils.ImageUtilities;
import com.example.cutter.views.CustomTextViewOutline;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;
import com.shashi.mysticker.DrawableSticker;
import com.shashi.mysticker.StickerView;

public class EditActivity extends AppCompatActivity {
    Bitmap bitmap;
    //StickerView stickerView;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        //stickerView = findViewById(R.id.sticker_view);
        bottomNavigationView = findViewById(R.id.bottomNavigationViewEditImage);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        String imagePath = getIntent().getExtras().getString("bitmap_FilterActivity");
        bitmap = ImageUtilities.decodeImage(imagePath);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
       /* stickerView.setBackground(drawable);
        stickerView.getLayoutParams().height = bitmap.getHeight();
        stickerView.getLayoutParams().width = bitmap.getWidth();*/
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.bottomBackgroundColor:
                    openColorPicker();
                    break;
                case R.id.bottomText:
                    final TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(EditActivity.this);
                    textEditorDialogFragment.setmContext(EditActivity.this);
                    textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
                        @Override
                        public void onDone(String inputText, int colorCode, Typeface typeface, CustomTextViewOutline strokedTextView) {
                            //creates bitmap for the textView.
                            strokedTextView.buildDrawingCache();
                            Bitmap bmp = Bitmap.createBitmap(strokedTextView.getDrawingCache());
                            //bmp = BITMAP_RESIZER(bmp, (int)(bmp.getWidth()*0.7), (int)(bmp.getHeight()*0.7));
                            //bmp = blur(bmp);
                            Drawable drawable = new BitmapDrawable(getResources(), bmp);
                            DrawableSticker drawableSticker = new DrawableSticker(drawable);
                            drawableSticker.setType("text");
                            stickerView.addSticker(drawableSticker);
                        }
                        @Override
                        public void onAdd(String inputText, int colorCode, Typeface typeface) {
                        }
                    });
                    break;

            }
            return true;
        }
    };
    private void openColorPicker(){
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        final Dialog dialog = new Dialog(this);
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.color_picker_dialog);
        int height = displayMetrics.heightPixels;
        final ImageView imageView = dialog.findViewById(R.id.previewColor);
        final ColorPicker colorPicker = dialog.findViewById(R.id.colorPicker);
        //imageView.getBackground().setColorFilter(-1, PorterDuff.Mode.MULTIPLY);
        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener(){
            @Override
            public void onColorSelected(int color) {
                super.onColorSelected(color);
                //mDefaultColor = color;
               // mphotoEditor.setBackgroundColor(mDefaultColor);
                imageView.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        });
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = (int)(height*0.5);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
}