package com.apps.kunalfarmah.echo.Online.ui.artists;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.apps.kunalfarmah.echo.Online.ui.albums.AlbumsViewModel;


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
