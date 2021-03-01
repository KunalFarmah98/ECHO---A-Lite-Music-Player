package com.apps.kunalfarmah.echo.fragment


import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
/*The settings fragment class is used for handling the events inside the settings fragment*/

class SettingsFragment : Fragment() {

    var myActivity: Activity? = null

    /*Declaring the switch used*/
    var shakeSwitch: Switch? = null


    var aditional:TextView? =null

    var separator:View?=null



    companion object{
        val TAG = "SettingsFragment"
    }

    /*Here the change in switch will lead to turning on and off of a setting so we need to persist the changes
    * This will be done with the help of Shared preferences*/
    object Statified {
        var MY_PREFS_NAME = "ShakeFeature"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        setHasOptionsMenu(true)

        activity?.title = "Settings"
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav).visibility = View.GONE


        /*Linking switch to its view*/
        shakeSwitch = view?.findViewById(R.id.switchShake)
        aditional=view?.findViewById(R.id.additional)
        separator=view?.findViewById(R.id.v2)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            aditional?.visibility=View.VISIBLE
            separator?.visibility=View.VISIBLE
        }

        MainActivity.Statified.settingsOn=true


        return view
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val prefs = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)

        var isAllowed = prefs?.getBoolean("feature", false)

        /*Checking the value of the feature as to whether it is ON or OFF*/
        shakeSwitch?.isChecked = isAllowed as Boolean

        /*Now we handle the change events i.e. when the switched is turned ON or OFF*/
        shakeSwitch?.setOnCheckedChangeListener({ compoundButton, b ->
            if (b) {

                /*If the switch is turned on we then make the feature to be true*/
                val editor = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", true)
                editor?.apply()
            } else {

                /*Else the feature remains false*/
                val editor = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", false)
                editor?.apply()
            }
        })


        aditional?.setOnClickListener({


            val setup = NotificationSetup()

            (context as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, setup)
//                    .addToBackStack("Additional")
                    .commit()


        })


    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        //removing the sorting adn searching options as they r not needed

        val item = menu.findItem(R.id.action_sort)
        item?.isVisible=false

        val item1 = menu.findItem(R.id.action_search)
        item1?.isVisible=false
    }






}