package com.apps.kunalfarmah.echo.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.kunalfarmah.echo.R;

import java.util.List;

import de.umass.lastfm.Artist;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ArtistViewHolder> {


    List<Artist> Artists;
    Context mContext;

    public ArtistsAdapter(Context context, List<Artist> artists) {
        Artists = artists;
        mContext = context;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.artist_item,parent,false);
        return new ArtistViewHolder(rootView);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = Artists.get(position);
        String name = artist.getName();
//        String display="";
//        int c=0;
//        for(int i=0; i<name.length(); i++){
//
//
//            if(name.charAt(i)==' '){
//                ++c;
//                if(c==2){
//                    display = display.concat("\n");
//                    c=0;
//                    continue;
//                }
//            }
//            display = display.concat(String.valueOf(name.charAt(i)));
//        }
        holder.name.setText(name);
//        String image_url = artist.getImageURL(ImageSize.LARGE);
//        RequestOptions options = new RequestOptions()
//                .centerCrop()
//                .placeholder(R.drawable.now_playing_bar_eq_image)
//                .error(R.drawable.now_playing_bar_eq_image);
//        Glide.with(mContext).load(image_url).apply(options).into(holder.image);
//        holder.playcount.setText(artist.getWikiSummary());
//        holder.listeners.setText(String.valueOf(artist.getListeners()));

        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return Artists.size();
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder {
        ImageButton play;
        TextView name,listeners,playcount;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            play = itemView.findViewById(R.id.play);
            name = itemView.findViewById(R.id.name);
            listeners = itemView.findViewById(R.id.listners);
            playcount = itemView.findViewById(R.id.plays);
        }
    }
}
