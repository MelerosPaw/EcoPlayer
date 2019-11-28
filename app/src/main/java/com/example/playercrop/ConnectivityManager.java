package com.example.playercrop;

import android.content.Context;
import android.net.NetworkInfo;

import androidx.annotation.Nullable;

public class ConnectivityManager {

  public static boolean isWifiConnected(@Nullable Context context) {
    boolean isWifiConnected = false;

    if (context != null) {
      android.net.ConnectivityManager connManager =
          (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connManager != null) {
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();

        isWifiConnected = activeNetworkInfo != null
            && activeNetworkInfo.getType() == android.net.ConnectivityManager.TYPE_WIFI
            && activeNetworkInfo.isConnected();
      }
    }

    return isWifiConnected;
  }
}
