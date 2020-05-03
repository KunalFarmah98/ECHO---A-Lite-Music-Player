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
import com.vpaliy.last_fm_api.LastFm;
import com.vpaliy.last_fm_api.LastFmService;
import com.vpaliy.last_fm_api.model.Album;
import com.vpaliy.last_fm_api.model.AlbumPage;
import com.vpaliy.last_fm_api.model.Artist;
import com.vpaliy.last_fm_api.model.Tag;
import com.vpaliy.last_fm_api.model.TagPage;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AlbumsViewModel extends AndroidViewModel {

    private MutableLiveData<List<Album>> data;
    List<Album> Albums;
    List<Tag> tags;

    public AlbumsViewModel(@NonNull Application application) {
        super(application);
        data = new MutableLiveData<>();
        init();
    }

    public void init(){
        //get the service
        LastFmService service= LastFm.create(Constants.API_KEY)
                .createService(this.getApplication());



        //request an artist
        service.fetchArtist("Eminem")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    Log.d("Error",String.valueOf(response.error)+response.message);
                    Artist artist=response.result;
                });

        //request tags
        service.fetchTopTags()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    TagPage tag = response.result;
                    Log.d("Error",String.valueOf(response.error)+response.message);
                    tags = tag.tag;
                });

        //request an album
        service.fetchTagTopAlbums("pop")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    AlbumPage albums = response.result;
                    Log.d("Error",String.valueOf(response.error)+response.message);
                    Albums = albums.album;
                });

        data.setValue(Albums);

    }

    public MutableLiveData<List<Album> > getAlbums() {
        return data;
    }
}