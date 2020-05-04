package com.apps.kunalfarmah.echo.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.kunalfarmah.echo.Online.OnlineActivity;
import com.apps.kunalfarmah.echo.Online.ui.albums.AlbumsFragment;
import com.apps.kunalfarmah.echo.Online.ui.albums.AlbumsViewModel;
import com.apps.kunalfarmah.echo.Online.ui.artists.ArtistsViewModel;
import com.apps.kunalfarmah.echo.Online.ui.tracks.TracksViewModel;
import com.apps.kunalfarmah.echo.R;

import java.util.List;

import de.umass.lastfm.Tag;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    Context mContext;
    List<Tag> tags;
    public TagAdapter(Context context, List<Tag> tags){
        mContext = context;
        this.tags = tags;
    }
    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.tag_item,parent,false);
        return new TagViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = tags.get(position);
        holder.tag_tv.setText(tag.getName());

        holder.tag_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineActivity.selectedTag = holder.tag_tv.getText().toString();
                switch(OnlineActivity.currFragment){
                    case 0:
                        AlbumsViewModel.update(OnlineActivity.selectedTag);break;
//                    case 1:
//                        ArtistsViewModel.update(OnlineActivity.selectedTag);break;
//                    case 2:
//                        TracksViewModel.update(OnlineActivity.selectedTag);break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public class TagViewHolder extends RecyclerView.ViewHolder {

        TextView tag_tv;
        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tag_tv = itemView.findViewById(R.id.tag);
        }
    }
}
