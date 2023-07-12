package com.apps.kunalfarmah.echo.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.apps.kunalfarmah.echo.R

class Wizard : Fragment() {

    var myActivity: Activity? = null
    var nxt: Button? = null
    var instr: TextView? = null
    var imagev: ImageView? = null
    var pressed = 1


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var view = inflater.inflate(R.layout.fragment_wizard, container, false)

        setHasOptionsMenu(true)
        nxt = view?.findViewById(R.id.nextstep)
        instr = view?.findViewById(R.id.instruction)
        imagev = view?.findViewById(R.id.visual)



        return view

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        //removing the sorting adn searching options as they r not needed

        var item = menu.findItem(R.id.action_sort_recent)
        item?.isVisible=false

        item = menu.findItem(R.id.acton_sort_ascending)
        item?.isVisible=false

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        nxt?.setOnClickListener({

            ++pressed

            if (pressed == 4)
                nxt?.visibility = View.GONE


            when (pressed) {
                2 -> {
                    instr?.setText(R.string.inst2)
                    imagev?.setImageDrawable(resources.getDrawable(R.drawable.s2))
                }
                3 -> {
                    instr?.setText(R.string.inst3)
                    imagev?.setImageDrawable(resources.getDrawable(R.drawable.s3))
                }
                4 -> {
                    instr?.setText(R.string.inst4)
                    imagev?.setImageDrawable(resources.getDrawable(R.drawable.s4))
                }

            }

        })
    }


}
