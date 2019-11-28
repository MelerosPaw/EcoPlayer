package com.example.playercrop;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtils {

  private ScreenUtils() {
  }

  public static float width(Context context) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return displayMetrics.widthPixels;
  }

  public static float heightProv(Context context) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    return displayMetrics.heightPixels;
  }
}
