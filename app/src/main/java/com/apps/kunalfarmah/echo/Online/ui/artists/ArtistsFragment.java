package com.apps.kunalfarmah.echo.Online.ui.artists;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.kunalfarmah.echo.Adapters.AlbumsAdapter;
import com.apps.kunalfarmah.echo.Adapters.ArtistsAdapter;
import com.apps.kunalfarmah.echo.Online.OnlineActivity;
import com.apps.kunalfarmah.echo.Online.ui.albums.AlbumViewModelFactory;
import com.apps.kunalfarmah.echo.Online.ui.albums.AlbumsViewModel;
import com.apps.kunalfarmah.echo.R;

import java.util.List;

import de.umass.lastfm.Album;
import de.umass.lastfm.Artist;

public class ArtistsFragment extends Fragment {

    private ArtistsViewModel artistsViewModel;
    private ArtistsAdapter madapter;

    RecyclerView artistsRecycler,tag_scroll;

    public ArtistsFragment(){
        OnlineActivity.currFragment=1;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_artists, container, false);
        artistsRecycler = root.findViewById(R.id.Artists);
        tag_scroll = root.findViewById(R.id.tags);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        artistsViewModel = ViewModelProviders.of(this,new ArtistsViewModelFactory(this.getActivity().getApplication())).get(ArtistsViewModel.class);
        artistsViewModel.getArtists().observe(getViewLifecycleOwner(),artistListUpdateObserver);

        tag_scroll.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
        tag_scroll.setAdapter(OnlineActivity.tagAdapter);

        return root;
    }

    Observer<List<Artist>> artistListUpdateObserver = new Observer<List<Artist>>(){

        @Override
        public void onChanged(List<Artist> artists) {
            madapter = new ArtistsAdapter(getContext(),artists);
            artistsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            artistsRecycler.setAdapter(madapter);
        }
    };
}
