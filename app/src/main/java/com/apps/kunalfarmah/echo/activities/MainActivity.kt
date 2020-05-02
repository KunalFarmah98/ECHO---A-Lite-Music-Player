package com.apps.kunalfarmah.echo.activities

import android.app.Notification
import android.app.NotificationManager

import android.os.Bundle
import com.apps.kunalfarmah.echo.Adapters.NavigationDrawerAdapter

import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.fragments.MainScreenFragment
import com.apps.kunalfarmah.echo.mNotification
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


import com.apps.kunalfarmah.echo.activities.MainActivity.Statified.notify

import com.apps.kunalfarmah.echo.fragments.SongPlayingFragment
import com.apps.kunalfarmah.echo.fragments.SongPlayingFragment.Staticated.mSensorListener


class MainActivity : AppCompatActivity() {

    var song:SongPlayingFragment?=null

    //setting up a broadcast receiver to close the activity when notification is closed

    var mLocalBroadcastManager: LocalBroadcastManager? = null
    var  mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {

       override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.durga.action.close") {

                SongPlayingFragment.Statified.mediaPlayer?.stop()
                SongPlayingFragment.Staticated.mSensorManager?.unregisterListener(mSensorListener)
                song!!.unregister()


                MainActivity.Statified.firstrun=false
               // SongPlayingFragment.Statified.mediaPlayer?.release()
                finishAffinity()


            }
        }
    }


    /*The list for storing the names of the items in list of navigation drawer*/
    var navigationDrawerIconsList: ArrayList<String> = arrayListOf()

    /*Images which will be used inside navigation drawer*/
    var images_for_navdrawer = intArrayOf(R.drawable.navigation_allsongs, R.drawable.navigation_favorites, R.drawable.navigation_settings, R.drawable.navigation_aboutus)

    // to track the notificaiton
    var trackNotificationBuilder: Notification? = null

    var testBoundService: mNotification? = null
    var isBound = false

    /*We made the drawer layout as an object of this class so that this object can also be used as same inside the adapter class without any change in its value*/
    object Statified {
        var drawerLayout: DrawerLayout? = null
        var notify =false
        var notificationManager: NotificationManager? = null
        var settingsOn:Boolean=false
        var AboutOn:Boolean=false
        var MainorFavOn:Boolean=false
        var firstrun=false

    }


    fun setNotify_val(bool:Boolean){
        notify = bool
    }

    fun getnotify_val():Boolean{
        return notify
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Statified.firstrun=true
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        song= SongPlayingFragment()

        /*This syntax is used to access the objects inside the class*/
        MainActivity.Statified.drawerLayout = findViewById(R.id.drawer_layout)

        /*Adding names of the titles using the add() function of ArrayList*/
        navigationDrawerIconsList.add("All Songs")
        navigationDrawerIconsList.add("Favorites")
        navigationDrawerIconsList.add("Settings")
        navigationDrawerIconsList.add("About Developer")

        val toggle = ActionBarDrawerToggle(this@MainActivity, Statified.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        MainActivity.Statified.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager
                .beginTransaction()
                .replace(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
                .commit()

        /*Now we create a variable of Navigation Drawer adapter and initialise it with the params required. As you remember that while creating a class for the navigation drawer adapter,
            * we gave it some params which are required for initialising the class. These params are the list, images and the context for the adapter file respectively*/
        val _navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconsList, images_for_navdrawer, this)

/*Here the function notifyDataSetChanged() tells the adapter that the data you were holding has been changed and thus you should refresh the list*/
        _navigationAdapter.notifyDataSetChanged()

/*Declaring the variable navigation_recycler_view for the list inside the navigation drawer*/
        val navigation_recycler_view = findViewById<RecyclerView>(R.id.navRecyclerView)

/*Here we set a layout manager which aligns the items in a recycler view. As we want to set the items vertically one below the other we use a linear layout manager.*/
        navigation_recycler_view.layoutManager = LinearLayoutManager(this)

/*As the name is suggesting the item animator is used to animate the way the items appear in a recycler view. As we used the default item animator, here we will just see the items
         * appear as they come without any transition */
        navigation_recycler_view.itemAnimator = DefaultItemAnimator()

/*Now we set the adapter to our recycler view to the adapter we created*/
        navigation_recycler_view.adapter = _navigationAdapter

/*As the code setHasFixedSize() suggests, the number of items present in the recycler view are fixed and won't change any time*/
        navigation_recycler_view.setHasFixedSize(true)

//
//        var intent = Intent(this@MainActivity, MainActivity::class.java)
//
//        var pIntent = PendingIntent.getActivity(this@MainActivity, System.currentTimeMillis().toInt(),
//                intent, 0)

//        trackNotificationBuilder = Notification.Builder(this).setContentTitle("A track is playing in the background")
//                .setSmallIcon(R.drawable.echo_logo).setContentIntent(pIntent).setOngoing(true).setAutoCancel(false).build()
//
//        Statified.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//
//        var serviceIntent = Intent(this@MainActivity, mNotification::class.java)
//
//
//        serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION)
//
//        startService(serviceIntent)

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        val mIntentFilter = IntentFilter()
        mIntentFilter.addAction("com.durga.action.close")


        mLocalBroadcastManager?.registerReceiver(
                mBroadcastReceiver,
                mIntentFilter
        )

    }


    override fun onDestroy() {
        try {
            super.onDestroy()
            //Toast.makeText(this,"Destroyed", Toast.LENGTH_SHORT).show()
            mLocalBroadcastManager?.unregisterReceiver(mBroadcastReceiver)
            SongPlayingFragment.Staticated.mSensorManager?.unregisterListener(mSensorListener)
            song!!.unregister()
        }
        catch (e:Exception){}


    }

    override fun onBackPressed() {


        if (Statified.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            Statified.drawerLayout!!.closeDrawer(GravityCompat.START)
        }



        // going to MainScreenFragment when back is pressed in Settings or AboutUs

        if(Statified.settingsOn){

            val mainScreenFragment = MainScreenFragment()
            this.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
                    .commit()

            Statified.settingsOn=false

        }

        else if(Statified.AboutOn){


            val mainScreenFragment = MainScreenFragment()
            this.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
                    .commit()

            Statified.AboutOn=false

        }

        // closing the app if we are on Main or favorite screen

        else if(Statified.MainorFavOn){

            var index = supportFragmentManager.backStackEntryCount-2

            if(index<0){
                super.onBackPressed()
            }

            else if(index>=0){

                var backEntry = supportFragmentManager.getBackStackEntryAt(index)
                var name = backEntry.name


                if (name.equals("SongPlayingFragment", true)) {
                    Statified.MainorFavOn = false
                    finish()
                }

                else{
                    super.onBackPressed()
                }
            }


        }

        else {
            super.onBackPressed()
        }
    }


}
