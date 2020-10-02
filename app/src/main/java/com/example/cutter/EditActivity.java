package com.example.cutter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.cutter.fragments.TextEditorDialogFragment;
import com.example.cutter.utils.ImageUtilities;
import com.example.cutter.views.CustomTextViewOutline;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.shashi.mysticker.DrawableSticker;
import com.shashi.mysticker.StickerView;

public class EditActivity extends AppCompatActivity {
    Bitmap bitmap;
    StickerView stickerView;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        stickerView = findViewById(R.id.sticker_view);
        bottomNavigationView = findViewById(R.id.bottomNavigationViewEditImage);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        String imagePath = getIntent().getExtras().getString("bitmap_FilterActivity");
        bitmap = ImageUtilities.decodeImage(imagePath);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        stickerView.setBackground(drawable);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.bottomBackgroundColor:
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
}