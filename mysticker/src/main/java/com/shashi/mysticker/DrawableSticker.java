package com.shashi.mysticker;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;


/**
 * Created by shashi on 6/6/18
 */

public class DrawableSticker extends Sticker {

  private Drawable drawable;
  private Rect realBounds;
  private String type;
  public DrawableSticker(Drawable drawable) {
    this.drawable = drawable;
    this.type = type;
    realBounds = new Rect(0, 0, getWidth(), getHeight());
  }

  @NonNull
  @Override
  public Drawable getDrawable() {
    return drawable;
  }

  @Override
  public DrawableSticker setDrawable(@NonNull Drawable drawable) {
    this.drawable = drawable;
    return this;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    canvas.save();
    canvas.concat(getMatrix());
    drawable.setBounds(realBounds);
    drawable.draw(canvas);
    canvas.restore();
  }

  @NonNull
  @Override
  public DrawableSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    drawable.setAlpha(alpha);
    return this;
  }

  @Override
  public int getWidth() {
    return drawable.getIntrinsicWidth();
  }

  @Override
  public int getHeight() {
    return drawable.getIntrinsicHeight();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public void release() {
    super.release();
    if (drawable != null) {
      drawable = null;
    }
  }


}
