package com.apps.kunalfarmah.echo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.apps.kunalfarmah.echo.R
import com.apps.kunalfarmah.echo.databinding.ActivitySongPlayingBinding
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment

class SongPlayingActivity : AppCompatActivity() {
    lateinit var binding : ActivitySongPlayingBinding
    companion object{
        var songPlayingFragment = SongPlayingFragment()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_to_list)

        val args = intent.extras
        songPlayingFragment.arguments = args
        supportFragmentManager.beginTransaction().replace(R.id.container, songPlayingFragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.home || item.itemId == R.id.homeAsUp)
            onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}