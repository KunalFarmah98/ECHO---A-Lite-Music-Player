package com.apps.kunalfarmah.echo.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.activity.MainActivity

class HelpFragment : Fragment() {

    companion object{
        val TAG = "Help Fragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var v = inflater.inflate(R.layout.fragment_help, container, false)

        setHasOptionsMenu(true)

        activity?.title = "Help"
        MainActivity.Statified.settingsOn=true


        (v.findViewById<TextView>(R.id.link)).setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(v.findViewById<TextView>(R.id.link).text.toString())
            startActivity(intent)
        }
        return  v
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        //removing the sorting adn searching options as they r not needed

        var item = menu.findItem(R.id.action_sort_recent)
        item?.isVisible=false

        item = menu.findItem(R.id.acton_sort_ascending)
        item?.isVisible=false

    }

}