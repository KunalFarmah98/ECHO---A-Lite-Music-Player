package com.apps.kunalfarmah.echo.online.ui.artists;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ArtistsViewModelFactory implements ViewModelProvider.Factory {
    Application mApplication;

    public ArtistsViewModelFactory(Application mApplication) {
        this.mApplication = mApplication;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ArtistsViewModel(mApplication);
    }
}
