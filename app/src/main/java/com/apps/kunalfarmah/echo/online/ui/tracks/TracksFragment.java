package com.apps.kunalfarmah.echo.online.ui.tracks;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.kunalfarmah.echo.adapter.TrackAdapter;
import com.apps.kunalfarmah.echo.online.OnlineActivity;
import com.apps.kunalfarmah.echo.R;

import java.util.List;

import de.umass.lastfm.Track;

public class TracksFragment extends Fragment {

    private TracksViewModel tracksViewModel;
    private TrackAdapter madapter;

    RecyclerView tracksRecycler,tag_scroll;

    public TracksFragment(){
        OnlineActivity.currFragment=2;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tracks, container, false);
        tracksRecycler = root.findViewById(R.id.Tracks);
        tag_scroll = root.findViewById(R.id.tags);

        tracksRecycler.setItemViewCacheSize(100);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        tracksViewModel = ViewModelProviders.of(this,new TracksViewModelFactory(this.getActivity().getApplication())).get(TracksViewModel.class);
        tracksViewModel.getTracks().observe(getViewLifecycleOwner(),trackListUpdateObserver);

        tag_scroll.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
        tag_scroll.setAdapter(OnlineActivity.tagAdapter);

        return root;
    }

    Observer<List<Track>> trackListUpdateObserver = new Observer<List<Track>>(){

        @Override
        public void onChanged(List<Track> tracks) {
            madapter = new TrackAdapter(getContext(),tracks);
            tracksRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            tracksRecycler.setAdapter(madapter);
        }
    };
}
