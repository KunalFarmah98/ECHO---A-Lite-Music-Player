package com.apps.kunalfarmah.echo.Online.ui.albums;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.kunalfarmah.echo.Adapters.AlbumsAdapter;
import com.apps.kunalfarmah.echo.Online.OnlineActivity;
import com.apps.kunalfarmah.echo.R;
import com.apps.kunalfarmah.echo.Songs;


import java.util.ArrayList;
import java.util.List;

import de.umass.lastfm.Album;
import de.umass.lastfm.Tag;

import static com.apps.kunalfarmah.echo.Constants.API_KEY;

public class AlbumsFragment extends Fragment {

    private AlbumsViewModel albumsViewModel;
    private AlbumsAdapter madapter;

    public AlbumsFragment(){
        OnlineActivity.currFragment=0;
    }

    RecyclerView albumsRecycler,tag_scroll;


    @SuppressLint("FragmentLiveDataObserve")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_albums, container, false);
        albumsRecycler = root.findViewById(R.id.Albums);
        tag_scroll = root.findViewById(R.id.tags);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        albumsViewModel = ViewModelProviders.of(this,new AlbumViewModelFactory(this.getActivity().getApplication())).get(AlbumsViewModel.class);
        albumsViewModel.getAlbums().observe(getViewLifecycleOwner(),albumListUpdateObserver);

        tag_scroll.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
        tag_scroll.setAdapter(OnlineActivity.tagAdapter);

        return root;
    }

    Observer<List<Album>> albumListUpdateObserver = new Observer<List<Album>>(){

        @Override
        public void onChanged(List<Album> albums) {
                madapter = new AlbumsAdapter(getContext(),albums);
                albumsRecycler.setLayoutManager(new GridLayoutManager(getContext(),2));
                albumsRecycler.setAdapter(madapter);
        }
    };

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.online_options, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setQueryHint("Enter Album name or select any tag");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                AlbumsViewModel.updateAlbum(s);
                return true;
            }
        });

        return;
    }
}
