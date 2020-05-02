package com.apps.kunalfarmah.echo.Online.ui.albums;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.kunalfarmah.echo.R;

public class AlbumsFragment extends Fragment {

    private AlbumsViewModel albumsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        albumsViewModel =
                ViewModelProviders.of(this).get(AlbumsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_albums, container, false);
        final RecyclerView albums = root.findViewById(R.id.Albums);

        return root;
    }
}
