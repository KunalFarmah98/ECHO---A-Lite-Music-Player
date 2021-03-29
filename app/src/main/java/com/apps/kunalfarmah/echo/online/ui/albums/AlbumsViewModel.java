package com.apps.kunalfarmah.echo.online.ui.albums;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


import java.util.List;

import de.umass.lastfm.Album;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Tag;


import static com.apps.kunalfarmah.echo.util.Constants.API_KEY;

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
        //ArtistsViewModel.Artists = (List<Artist>) Tag.getTopArtists("pop",API_KEY) ;

        data.setValue(Albums);

    }

    public static void update(String tag){
        Albums = (List<Album>) Tag.getTopAlbums(tag,API_KEY);
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