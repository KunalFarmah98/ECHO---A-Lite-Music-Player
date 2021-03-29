package com.apps.kunalfarmah.echo.online.ui.artists;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Tag;

import static com.apps.kunalfarmah.echo.util.Constants.API_KEY;

public class ArtistsViewModel extends AndroidViewModel {

    private static MutableLiveData<List<Artist>> data;
    public static List<Artist> Artists;

    public ArtistsViewModel(Application application) {
        super(application);
        data = new MutableLiveData<>();
        init();
    }

    public void init(){
        Caller.getInstance().setCache(null);
        Caller.getInstance().setUserAgent("Android");


        Artists = (List<Artist>) Tag.getTopArtists("pop",API_KEY);
//        int n = Artists.size();
//        for(int i = 0; i<n; i++){
//            Artists.set(i,Artist.getInfo(Artists.get(i).getName(),API_KEY));
//        }
        data.setValue(Artists);

    }

    public static void update(String tag){
        Artists = (List<Artist>) Tag.getTopArtists(tag,API_KEY);
//        int n = Artists.size();
//        for(int i = 0; i<n; i++){
//            Artists.set(i,Artist.getInfo(Artists.get(i).getName(),API_KEY));
//        }
        data.setValue(Artists);
    }

    public static void updateArtist(String name){
        Artists = (List<Artist>)Artist.search(name,API_KEY);
        data.setValue(Artists);
    }


    public MutableLiveData<List<Artist>> getArtists() {
        return data;
    }
}