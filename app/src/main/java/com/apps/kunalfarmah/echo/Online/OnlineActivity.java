package com.apps.kunalfarmah.echo.Online;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.kunalfarmah.echo.Adapters.TagAdapter;
import com.apps.kunalfarmah.echo.Online.ui.albums.AlbumsViewModel;
import com.apps.kunalfarmah.echo.Online.ui.artists.ArtistsViewModel;
import com.apps.kunalfarmah.echo.R;
import com.apps.kunalfarmah.echo.activities.MainActivity;
import com.apps.kunalfarmah.echo.activities.SplashActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.List;

import de.umass.lastfm.Caller;
import de.umass.lastfm.Tag;

import static com.apps.kunalfarmah.echo.Constants.API_KEY;

public class OnlineActivity extends AppCompatActivity {
    List<Tag> tags;
    public static TagAdapter tagAdapter;
    public static String selectedTag;
    public static int currFragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_albums, R.id.navigation_artists, R.id.navigation_tracks)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Caller.getInstance().setCache(null);
        Caller.getInstance().setUserAgent("Android");

        // setting up tags in activity as it is used by all
        tags = (List<Tag>) Tag.getTopTags(API_KEY);
        tagAdapter = new TagAdapter(this, tags);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.online_options, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (currFragment == 0)
            searchView.setQueryHint("Enter Album name or select any tag");
        else if (currFragment == 1)
            searchView.setQueryHint("Enter Artist name or select any tag");
        else
            searchView.setQueryHint("Enter Track name or select any tag");


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (currFragment == 0)
                    AlbumsViewModel.updateAlbum(s);
              else if(currFragment==1)
                    ArtistsViewModel.updateArtist(s);
//               else if(currFragment==0)
//                    AlbumsViewModel.updateTrack(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });


        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                AlbumsViewModel.update("pop");
                return false;
            }
        });

        //MenuItem offline = menu.findItem(R.id.action_goOffline);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_goOffline:
                SharedPreferences pref  = getSharedPreferences("Mode",0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("mode","offline");
                editor.apply();
                finish();
                startActivity(new Intent(this, SplashActivity.class));
                return true;
        }
        return false;
    }
}
