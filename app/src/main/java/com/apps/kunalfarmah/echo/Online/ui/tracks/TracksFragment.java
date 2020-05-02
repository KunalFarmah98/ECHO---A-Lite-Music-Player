package com.apps.kunalfarmah.echo.Online.ui.tracks;

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

public class TracksFragment extends Fragment {

    private TracksViewModel tracksViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        tracksViewModel =
                ViewModelProviders.of(this).get(TracksViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tracks, container, false);
        final RecyclerView textView = root.findViewById(R.id.Tracks);
        return root;
    }
}
