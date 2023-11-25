package com.apps.kunalfarmah.echo.activity

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity.Statified.notify
import com.apps.kunalfarmah.echo.adapter.NavigationDrawerAdapter
import com.apps.kunalfarmah.echo.databinding.ActivityMainBinding
import com.apps.kunalfarmah.echo.fragment.FavoriteFragment
import com.apps.kunalfarmah.echo.fragment.MainScreenFragment
import com.apps.kunalfarmah.echo.fragment.OfflineAlbumsFragment
import com.apps.kunalfarmah.echo.fragment.SearchFragment
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment.Staticated.mSensorListener
import com.apps.kunalfarmah.echo.util.BottomBarUtils
import com.apps.kunalfarmah.echo.util.Constants
import com.apps.kunalfarmah.echo.util.MediaUtils
import com.apps.kunalfarmah.echo.util.MediaUtils.mediaPlayer
import com.apps.kunalfarmah.echo.viewModel.SongsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val viewModel: SongsViewModel by viewModels()
    var song: SongPlayingFragment? = null
    var bottomNav: BottomNavigationView? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var sharedPreferences:SharedPreferences ?= null
    private lateinit var binding: ActivityMainBinding

    private var fragments: SparseArray<Fragment> ?= null

    //setting up a broadcast receiver to close the activity when notification is closed

    var mLocalBroadcastManager: LocalBroadcastManager? = null
    var mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constants.ACTION.CLOSE) {
                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop()
                        mediaPlayer.playWhenReady = false
                        //mediaPlayer.release()
                    }
                }catch (e:Exception){}
                SongPlayingFragment.Staticated.mSensorManager?.unregisterListener(mSensorListener)
                song!!.unregister()
                // SongPlayingFragment.Statified.mediaPlayer?.release()
                finishAffinity()
            }
        }
    }


    /**
     * creating a broadcast receiver to register earphones unplugging
     *
     */
    private val mNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (mediaPlayer != null && MediaUtils.isMediaPlayerPlaying()) {
                mediaPlayer.pause()
                SongPlayingFragment.Statified.inform = true
                BottomBarUtils.updatePlayPause()
                SongPlayingFragment.Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)
            }
        }
    }


    /*The list for storing the names of the items in list of navigation drawer*/
    var navigationDrawerIconsList: ArrayList<String> = arrayListOf()

    /*Images which will be used inside navigation drawer*/
    var images_for_navdrawer = arrayOf(R.drawable.navigation_allsongs, R.drawable.navigation_settings, R.drawable.ic_baseline_help_24, R.drawable.baseline_share_white_36dp, R.drawable.baseline_star_rate_white_36dp)

    object Statified {
        var drawerLayout: DrawerLayout? = null
        var notify = false
        var notificationManager: NotificationManager? = null
        var settingsOn: Boolean = false
        var AboutOn: Boolean = false
        var MainorFavOn: Boolean = false

    }


    fun setNotify_val(bool: Boolean) {
        notify = bool
    }

    fun getnotify_val(): Boolean {
        return notify
    }

    val songObserver = Observer<Boolean> {
        if (it) {
            binding.mainLayout.nowPlayingBottomBar.playPause.setImageDrawable(resources.getDrawable(R.drawable.pause_icon))
            binding.mainLayout.nowPlayingBottomBar.next.visibility = View.VISIBLE
        }
        else {
            binding.mainLayout.nowPlayingBottomBar.playPause.setImageDrawable(resources.getDrawable(R.drawable.play_icon))
            binding.mainLayout.nowPlayingBottomBar.next.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences(Constants.APP_PREFS,Context.MODE_PRIVATE)
        // loop should be off on app launch
        sharedPreferences?.edit()?.putBoolean(Constants.LOOP,false)?.apply()
        MediaUtils.isShuffle = sharedPreferences?.getBoolean(Constants.SHUFFLE, false)!!
        firebaseAnalytics = Firebase.analytics
        setSupportActionBar(binding.toolbar)

        song = SongPlayingFragment()
        fragments = SparseArray(4)
        fragments!![0] = MainScreenFragment()
        fragments!![1] = OfflineAlbumsFragment()
        fragments!![2] = FavoriteFragment()
        fragments!![3] = SearchFragment()

        viewModel.init()
        /*This syntax is used to access the objects inside the class*/
        MainActivity.Statified.drawerLayout = findViewById(R.id.drawer_layout)

        /*Adding names of the titles using the add() function of ArrayList*/
        navigationDrawerIconsList.add("All Songs")
        navigationDrawerIconsList.add("Settings")
        navigationDrawerIconsList.add("Help")
        navigationDrawerIconsList.add("Share")
        navigationDrawerIconsList.add("Rate App")



        val toggle = ActionBarDrawerToggle(this@MainActivity, Statified.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        MainActivity.Statified.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        /*Now we create a variable of Navigation Drawer adapter and initialise it with the params required. As you remember that while creating a class for the navigation drawer adapter,
            * we gave it some params which are required for initialising the class. These params are the list, images and the context for the adapter file respectively*/
        val _navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconsList, images_for_navdrawer, this)

        _navigationAdapter.notifyDataSetChanged()

        val drawerRecycler = binding.navRecyclerView

        drawerRecycler.layoutManager = LinearLayoutManager(this)

        drawerRecycler.itemAnimator = DefaultItemAnimator()

/*Now we set the adapter to our recycler view to the adapter we created*/
        drawerRecycler.adapter = _navigationAdapter

        drawerRecycler.setHasFixedSize(true)

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        val mIntentFilter = IntentFilter()
        mIntentFilter.addAction(Constants.ACTION.CLOSE)


        mLocalBroadcastManager?.registerReceiver(
                mBroadcastReceiver,
                mIntentFilter
        )

        bottomNav = binding.mainLayout.bottomNav
        bottomNav!!.selectedItemId = R.id.navigation_main_screen

        bottomNav!!.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_main_screen -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, fragments?.get(0) ?: MainScreenFragment(), MainScreenFragment.TAG)
                            .commit()
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_albums -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, fragments?.get(1) ?: OfflineAlbumsFragment(), OfflineAlbumsFragment.TAG)
                            .commit()
                    return@setOnItemSelectedListener true

                }

                R.id.navigation_favorites -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, fragments?.get(2) ?: FavoriteFragment(), FavoriteFragment.TAG)
                            .commit()
                    return@setOnItemSelectedListener true

                }

                R.id.navigation_search -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.details_fragment, fragments?.get(3) ?:SearchFragment(), SearchFragment.TAG)
                            .commit()
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }

        binding.mainLayout.nowPlayingBottomBar.songTitle.isSelected = true
        binding.mainLayout.nowPlayingBottomBar.songArtist.isSelected = true
        viewModel.isSongPlaying.observeForever(songObserver)

        // register receiver for unplugging earphones
        var filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(mNoisyReceiver, filter)
    }

    override fun onResume() {
        super.onResume()
        try {
            SongPlayingFragment.Staticated.mSensorManager?.registerListener(
                SongPlayingFragment.Staticated.mSensorListener,
                SongPlayingFragment.Staticated.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }catch (e:Exception){
        }

        BottomBarUtils.bottomBarSetup(this, binding.mainLayout.nowPlayingBottomBar)
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            viewModel.isSongPlaying.removeObserver(songObserver)
            mLocalBroadcastManager?.unregisterReceiver(mBroadcastReceiver)
            unregisterReceiver(mNoisyReceiver)
            SongPlayingFragment.Staticated.mSensorManager?.unregisterListener(mSensorListener)
            song!!.unregister()
        } catch (e: Exception) {
        }


    }

    fun moveToHome() {
        bottomNav!!.selectedItemId = R.id.navigation_main_screen
    }

    override fun onBackPressed() {


        if (Statified.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            Statified.drawerLayout!!.closeDrawer(GravityCompat.START)
        }

        var fragment = supportFragmentManager.findFragmentByTag(MainScreenFragment.TAG)

        if (fragment != null && fragment.isVisible) {
            finish()
            return
        }

        fragment = supportFragmentManager.findFragmentByTag(FavoriteFragment.TAG)

        if (fragment != null && fragment.isVisible) {
            findViewById<BottomNavigationView>(R.id.bottom_nav)?.selectedItemId = R.id.navigation_main_screen
            return
        }

        fragment = supportFragmentManager.findFragmentByTag(OfflineAlbumsFragment.TAG)
        if (fragment != null && fragment.isVisible) {
            findViewById<BottomNavigationView>(R.id.bottom_nav)?.selectedItemId = R.id.navigation_main_screen
            OfflineAlbumsFragment.postion=0
            return
        }

        fragment = supportFragmentManager.findFragmentByTag(SearchFragment.TAG)
        if (fragment != null && fragment.isVisible) {
            findViewById<BottomNavigationView>(R.id.bottom_nav)?.selectedItemId = R.id.navigation_main_screen
            return
        }

        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && grantResults[0] == PackageManager.PERMISSION_DENIED)
            Toast.makeText(this, "Please Provide Storage Permission to Continue", Toast.LENGTH_SHORT).show()
    }
}
