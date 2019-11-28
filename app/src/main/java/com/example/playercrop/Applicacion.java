package com.example.playercrop;

import android.app.Application;
import android.content.Context;

public class Applicacion extends Application {

    private static Applicacion app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static Context get() { return app; }
}
