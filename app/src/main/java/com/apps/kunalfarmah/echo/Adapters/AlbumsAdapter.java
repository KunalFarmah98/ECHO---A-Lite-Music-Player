package com.apps.kunalfarmah.echo.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.kunalfarmah.echo.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import java.util.Date;
import java.util.List;

import de.umass.lastfm.Album;
import de.umass.lastfm.ImageSize;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumsViewHolder> {

    List<Album> Albums;
    Context mContext;

    public AlbumsAdapter(Context context, List<Album> albums) {
        Albums = albums;
        mContext = context;
    }

    @NonNull
    @Override
    public AlbumsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.grid_item,parent,false);
        return new AlbumsViewHolder(rootView);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull AlbumsViewHolder holder, int position) {
        Album album = Albums.get(position);
        holder.name.setText(album.getName());
        Date date = album.getReleaseDate();
        if(date!=null)
        holder.year.setText(album.getReleaseDate().toString());
        String image_url = album.getImageURL(ImageSize.LARGE);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.now_playing_bar_eq_image)
                .error(R.drawable.now_playing_bar_eq_image);
        Glide.with(mContext).load(image_url).apply(options).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return Albums.size();
    }

    public class AlbumsViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name,year;

        public AlbumsViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            year = itemView.findViewById(R.id.year);
        }
    }
}
