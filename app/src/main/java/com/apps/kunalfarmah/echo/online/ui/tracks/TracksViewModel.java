package com.apps.kunalfarmah.echo.online.ui.tracks;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;



import static com.apps.kunalfarmah.echo.Constants.API_KEY;

public class TracksViewModel extends AndroidViewModel {

    private static MutableLiveData<List<Track>> data;
    public static List<Track> Tracks;

    public TracksViewModel(Application application) {
       super(application);
       data = new MutableLiveData<>();
       init();
    }

    public void init(){
        Caller.getInstance().setCache(null);
        Caller.getInstance().setUserAgent("Android");

        Tracks = new ArrayList<>();

//        task fetch = new task("pop");
//        fetch.execute("pop","","");
        Tracks = (List<Track>) Tag.getTopTracks("pop",API_KEY);
        data.setValue(Tracks);
    }

    public static void update(String tag){
//        task fetch = new task(tag);
//        fetch.execute(tag,"","");
        Tracks = (List<Track>) Tag.getTopTracks(tag,API_KEY);
        data.setValue(Tracks);
    }

    public static void updateTracks(String name){
        Tracks = (ArrayList<Track>) Track.search(name,API_KEY);
        data.setValue(Tracks);
    }


    public MutableLiveData<List<Track>> getTracks() {
        return data;
    }


    static class task extends AsyncTask<String,String,String>{
        String tag;
        public task(String tag){
            this.tag = tag;
        }
        @Override
        protected String doInBackground(String... strings) {
            List<Track> temp = (List<Track>) Tag.getTopTracks(tag,API_KEY);
            int n = temp.size();
            for(int i=0; i<n; i++){
                Tracks.add(Track.getInfo(temp.get(i).getArtist(),temp.get(i).getMbid(),API_KEY));
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            data.setValue(Tracks);
        }
    }
}