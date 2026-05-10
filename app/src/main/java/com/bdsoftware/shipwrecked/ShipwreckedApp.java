package com.bdsoftware.shipwrecked;

import android.app.Application;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

public class ShipwreckedApp extends Application implements ViewModelStoreOwner {

    private static ShipwreckedApp instance;
    private final ViewModelStore viewModelStore = new ViewModelStore();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static ShipwreckedApp getInstance() {
        return instance;
    }

    @Override
    public ViewModelStore getViewModelStore() {
        return viewModelStore;
    }
}