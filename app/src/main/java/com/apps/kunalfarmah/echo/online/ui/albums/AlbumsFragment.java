package com.apps.kunalfarmah.echo.online.ui.albums;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.kunalfarmah.echo.adapter.AlbumsAdapter;
import com.apps.kunalfarmah.echo.online.OnlineActivity;
import com.apps.kunalfarmah.echo.R;


import java.util.List;

import de.umass.lastfm.Album;

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
    }
}
