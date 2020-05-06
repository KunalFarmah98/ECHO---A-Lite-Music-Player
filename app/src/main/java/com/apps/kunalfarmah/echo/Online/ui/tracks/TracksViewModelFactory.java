package com.apps.kunalfarmah.echo.Online.ui.tracks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.apps.kunalfarmah.echo.Online.ui.artists.ArtistsViewModel;


public class TracksViewModelFactory implements ViewModelProvider.Factory {
    Application mApplication;

    public TracksViewModelFactory(Application mApplication) {
        this.mApplication = mApplication;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new TracksViewModel(mApplication);
    }
}
