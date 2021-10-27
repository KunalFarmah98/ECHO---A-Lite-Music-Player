package com.apps.kunalfarmah.echo.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.apps.kunalfarmah.echo.BuildConfig
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.HelpActivity
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.apps.kunalfarmah.echo.activity.SettingsActivity
import com.apps.kunalfarmah.echo.online.OnlineActivity
import java.io.*
import java.util.*


class NavigationDrawerAdapter(_contentList: ArrayList<String>, _getImages: Array<Int>, _context: Context)
    : RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>() {


    var contentList: ArrayList<String>? = null
    var getImages: Array<Int>? = null
    var mContext: Context? = null

    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _context
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {

        /*Here we set the icon and the name of that icon with the setBackgroundResource() and the setText() method respectively*/
        holder.icon_GET?.setBackgroundResource(getImages?.get(position) as Int)
        holder.text_GET?.text = contentList?.get(position)

        /*Now since we want to open a new fragment at the click for every item we place the click listener according to the position of the items*/
        holder.contentHolder?.setOnClickListener {

            /*Loading the Main Screen Fragment as the first(remember that the index starts at 0) item is All songs and the fragment corresponding to it is the Main Screen fragment*/
            if (position == 0) {
                (mContext as MainActivity).movToHome()
            } else if (position == 1) {
                mContext?.startActivity(Intent(mContext,SettingsActivity::class.java))
            } else if (position == 2) {
                var intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://kunalfarmah.com")
                mContext!!.startActivity(intent)
            } else if (position == 3) {
                mContext?.startActivity(Intent(mContext,HelpActivity::class.java))
            } else if (position == 4) {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out an awesome offline music player app, ECHO - A LITE MUSIC PLAYER at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                var uri:Uri? = null
                try {
                    uri = getImageUri()
                }catch(e:Exception){
                    uri = null
                }
                if(null!=uri) {
                    sendIntent.putExtra(Intent.EXTRA_STREAM, getImageUri())
                    sendIntent.type = "image/*"
                }
                else
                    sendIntent.type = "text/*"
                mContext!!.startActivity(Intent.createChooser(sendIntent, "Share With"))
            } else if (position == 5) {
                val uri = Uri.parse("market://details?id=" + mContext!!.packageName)
                val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.

                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    mContext!!.startActivity(goToMarket)
                } catch (e: ActivityNotFoundException) {
                    mContext!!.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + mContext!!.getPackageName())))
                }
            } else if (position == 6) {
                var feedback = Intent(Intent.ACTION_SENDTO)
                var to = Array(1) { "kunalfarmah98@gmail.com" }
                feedback.data = Uri.parse("mailto:")
                feedback.putExtra(Intent.EXTRA_EMAIL, to)
                feedback.putExtra(Intent.EXTRA_SUBJECT, "Feedback for ECHO - A Lite Music Player")
                mContext!!.startActivity(feedback)

            } else if (position == 7) {
                var pref: SharedPreferences = mContext!!.getSharedPreferences("Mode", Context.MODE_PRIVATE)
                var editor = pref.edit()
                editor.putString("mode", "online")
                editor.apply()
                val activity: MainActivity = mContext as MainActivity
                activity.finish()
                mContext?.startActivity(Intent(mContext, OnlineActivity::class.java))
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {

        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_custom_navidrawer, parent, false)

        return NavViewHolder(itemView)
    }


    override fun getItemCount(): Int {

        return (contentList as ArrayList).size
    }

    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var icon_GET: ImageView? = null
        var text_GET: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            icon_GET = itemView.findViewById(R.id.icon_navdrawer)
            text_GET = itemView.findViewById(R.id.text_navDrawer)
            contentHolder = itemView.findViewById(R.id.navdrawer_item_skeleton)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getImageUri(): Uri {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val bm = BitmapFactory.decodeResource(mContext?.resources, R.drawable.echo_icon)
        val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
        val file = File(extStorageDirectory, "ECHO.png")
        if (!file.exists()) {
            var outStream: OutputStream? = null
            try {
                outStream = FileOutputStream(file)
                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            try {
                outStream!!.flush()
                outStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return Uri.parse(file.absolutePath)
    }
}
