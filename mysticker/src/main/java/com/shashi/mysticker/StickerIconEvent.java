package com.shashi.mysticker;

import android.view.MotionEvent;

/**
 * Created by shashi on 6/6/18
 */

public interface StickerIconEvent {
  void onActionDown(StickerView stickerView, MotionEvent event);

  void onActionMove(StickerView stickerView, MotionEvent event);

  void onActionUp(StickerView stickerView, MotionEvent event);
}
