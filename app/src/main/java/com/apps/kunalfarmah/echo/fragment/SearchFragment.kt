package com.apps.kunalfarmah.echo.fragment

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.apps.kunalfarmah.echo.adapter.SearchAdapter
import com.apps.kunalfarmah.echo.databinding.FragmentSearchBinding
import com.apps.kunalfarmah.echo.util.MediaUtils


/**
 * A fragment representing a list of Items.
 */
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private var searchAdapter : SearchAdapter ? = null

    companion object{
        val TAG = "SearchFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater)
        activity?.title = "Search"
        binding.recyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.VISIBLE

        searchAdapter = SearchAdapter(MediaUtils.allSongsList, context)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = searchAdapter

        binding.searchEt.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                searchAdapter?.setList(MediaUtils.allSongsList)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                search()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        binding.iconSearch.setOnClickListener {
            search()
            hideKeyboard(activity)
        }

        binding.iconClear.setOnClickListener {
            binding.searchEt.text = null
            searchAdapter?.setList(MediaUtils.allSongsList)
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
            hideKeyboard(activity)
        }

        return binding.root
    }

    fun hideKeyboard(activity: Activity?) {
        if(activity == null)
            return
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun search(){
        binding.searchEt.text.let {
            if(it.isNullOrEmpty()){
                binding.recyclerView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
            else{
                searchAdapter?.filter(binding.searchEt.text.toString())
                if(searchAdapter?.songs?.isEmpty()==true){
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                }
                else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }
            }
        }
    }

}