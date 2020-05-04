package com.apps.kunalfarmah.echo.Online;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;

import com.apps.kunalfarmah.echo.Adapters.TagAdapter;
import com.apps.kunalfarmah.echo.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        tags = (List<Tag>)Tag.getTopTags(API_KEY);
        tagAdapter = new TagAdapter(this,tags);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
