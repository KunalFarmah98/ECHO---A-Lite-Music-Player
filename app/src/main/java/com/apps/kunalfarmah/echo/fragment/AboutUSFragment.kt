package com.apps.kunalfarmah.echo.fragment


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class AboutUSFragment : Fragment() {


     var email: TextView?=null
     var fb: TextView?=null
//     var number: TextView?=null




    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        //removing the sorting adn searching options as they r not needed

        val item = menu.findItem(R.id.action_sort)
        item?.isVisible=false

        val item1 = menu.findItem(R.id.action_search)
        item1?.isVisible=false
    }




    fun AboutFragment() {
        // Required empty public constructor
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        //        Toast.makeText(getContext(), "onCreateview", Toast.LENGTH_SHORT).show();


        // Inflate the layout for this fragment


        var view = inflater.inflate(R.layout.fragment_about, container, false)


        setHasOptionsMenu(true)

        email = view.findViewById(R.id.email) as TextView
        fb = view.findViewById(R.id.fb) as TextView
//        number = view.findViewById(R.id.extra1)

        email!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("kunalfarmah98@gmail.com"))
            try {
                startActivity(intent)
                activity!!.supportFragmentManager.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(context, "No Email Apps found on the device", Toast.LENGTH_SHORT).show()
            }
        }

        fb!!.setOnClickListener {
            val url = "https://www.facebook.com/kunal.farmah"
            val fb = Intent(Intent.ACTION_VIEW)
            fb.data = Uri.parse(url)
            try {
                startActivity(fb)
                activity!!.supportFragmentManager.popBackStack()
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No Web Browsers found on the device", Toast.LENGTH_SHORT).show()
            }
        }

//        number!!.setOnClickListener {
//            val intent = Intent(Intent.ACTION_DIAL)
//            intent.data = Uri.parse("tel:" + "+919654211634")
//
//            startActivity(intent)
//            activity!!.supportFragmentManager.popBackStack()
//        }

        MainActivity.Statified.AboutOn=true

        return view
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        activity!!.title = "ABOUT ME"

    }


    @SuppressLint("UseRequireInsteadOfGet", "SourceLockedOrientationActivity")
    override fun onStart() {
        super.onStart()
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT


    }

}

