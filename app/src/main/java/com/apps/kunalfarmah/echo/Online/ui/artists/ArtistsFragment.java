package com.apps.kunalfarmah.echo.Online.ui.artists;

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

public class ArtistsFragment extends Fragment {

    private ArtistsViewModel artistsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        artistsViewModel =
                ViewModelProviders.of(this).get(ArtistsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_artists, container, false);
        final RecyclerView artists = root.findViewById(R.id.Artists);
        return root;
    }
}
