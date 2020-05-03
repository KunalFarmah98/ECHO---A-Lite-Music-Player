package com.apps.kunalfarmah.echo.Online.ui.albums;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.apps.kunalfarmah.echo.R;
import com.vpaliy.last_fm_api.model.Album;
import com.vpaliy.last_fm_api.model.User;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends Fragment {

    private AlbumsViewModel albumsViewModel;
    private AlbumsAdapter madapter;

    RecyclerView albumsRecycler;

    @SuppressLint("FragmentLiveDataObserve")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        albumsViewModel = new ViewModelProvider(this).get(AlbumsViewModel.class);
        albumsViewModel.getAlbums().observe(this,albumListUpdateObserver);

        View root = inflater.inflate(R.layout.fragment_albums, container, false);
        albumsRecycler = root.findViewById(R.id.Albums);

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
}
