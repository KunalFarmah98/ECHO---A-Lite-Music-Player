package com.apps.kunalfarmah.echo.adapter;

import android.content.Context;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.kunalfarmah.echo.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.umass.lastfm.ImageSize;
import de.umass.lastfm.Track;

import static com.apps.kunalfarmah.echo.Constants.API_KEY;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    public static List<Track> Tracks;
    Context mContext;

    public TrackAdapter(Context context, List<Track> tracks) {
        Tracks = tracks;
        mContext = context;
    }
    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.song_item,parent,false);
        return new TrackViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {


            Track track = Tracks.get(position);
            holder.name.setText(track.getName());
            holder.artist.setText(track.getArtist());
            holder.album.setText(track.getAlbum());
            Track t = Track.getInfo(track.getArtist(), track.getMbid(), API_KEY);

//            String url = t.getImageURL(ImageSize.LARGE);
//            RequestOptions options = new RequestOptions()
//                    .centerCrop()
//                    .skipMemoryCache(true)
//                    .placeholder(R.drawable.now_playing_bar_eq_image)
//                    .error(R.drawable.now_playing_bar_eq_image);
//            Glide.with(mContext).load(url).apply(options).into(holder.art);
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }

        getArt fetch = new getArt(holder.art, mContext, track, position);
        fetch.execute("");
//
//        holder.art.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
                String url = t.getLastFmInfo("freeTrackURL");
                String loc = t.getLocation();
//                MediaPlayer player = new MediaPlayer();
//
//                try {
//                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//
//                    player.setDataSource(url);
//
//
//                    player.prepare();
//                    player.start();
//                }
//                catch (IOException e){
//                    e.printStackTrace();
//                }
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return Tracks.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView name, artist, album;
        ImageView art;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.title);
            album = itemView.findViewById(R.id.album);
            artist = itemView.findViewById(R.id.artist);
            art = itemView.findViewById(R.id.albumArt);
        }
    }
    class getArt extends AsyncTask<String,Integer,String>{

        ImageView img;
        Context montext;
        Track track;
        int position;
        public getArt(ImageView img, Context mcontext, Track track, int pos){
            this.img = img;
            this.montext=mcontext;
            this.track = track;
            this.position = pos;
        }
        @Override
        protected String doInBackground(String... strings) {
            String url = "";
            try {
                Track t = Track.getInfo(track.getArtist(), track.getMbid(), API_KEY);
                //Album album = Album.getInfo(t.getArtist(), t.getAlbum(), API_KEY);
                Tracks.set(position,t);
                url = t.getImageURL(ImageSize.LARGE);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return url;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //if(img.getDrawable()!=null) return;
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.now_playing_bar_eq_image)
                    .error(R.drawable.now_playing_bar_eq_image);
            Glide.with(mContext).load(s).apply(options).into(img);
        }


    }
}
