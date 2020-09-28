package com.example.cutter;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.ImageView;

public class EditorActivity extends AppCompatActivity {
    private ImageView stickerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        stickerView = findViewById(R.id.stickerView);
        byte[] bytes = getIntent().getByteArrayExtra("bitmap_CropActivity");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        stickerView.setBackground(bitmapDrawable);
    }
}