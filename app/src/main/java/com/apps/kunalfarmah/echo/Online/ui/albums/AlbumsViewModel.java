package com.apps.kunalfarmah.echo.Online.ui.albums;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apps.kunalfarmah.echo.Constants;
import com.apps.kunalfarmah.echo.Online.OnlineActivity;


import java.util.ArrayList;
import java.util.List;

import de.umass.lastfm.Album;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Tag;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.apps.kunalfarmah.echo.Constants.API_KEY;

public class AlbumsViewModel extends AndroidViewModel {

    private static MutableLiveData<List<Album>> data;
    static List<de.umass.lastfm.Album> Albums;
    static List<Tag> tags;

    public AlbumsViewModel(@NonNull Application application) {
        super(application);
        data = new MutableLiveData<>();
        init();
    }

    public void init(){

        Caller.getInstance().setCache(null);
        Caller.getInstance().setUserAgent("Android");


        Albums = (List<Album>) Tag.getTopAlbums("pop",API_KEY);

        data.setValue(Albums);

    }

    public static void update(String tag){
        Albums = (List<Album>) Tag.getTopAlbums(OnlineActivity.selectedTag,API_KEY);
        data.setValue(Albums);
    }

    public static void updateAlbum(String name){
        Albums = (List<Album>)Album.search(name,API_KEY);
        data.setValue(Albums);
    }

    public MutableLiveData<List<Album> > getAlbums() {
        return data;
    }
}