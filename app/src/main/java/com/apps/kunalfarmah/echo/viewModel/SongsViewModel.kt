package com.apps.kunalfarmah.echo.viewModel

import android.os.Build
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.kunalfarmah.echo.fragment.SongPlayingFragment
import com.apps.kunalfarmah.echo.model.SongAlbum
import com.apps.kunalfarmah.echo.model.Songs
import com.apps.kunalfarmah.echo.repository.SongsRepository
import com.apps.kunalfarmah.echo.util.MediaUtils
import kotlinx.coroutines.launch

class SongsViewModel
@ViewModelInject
constructor(private val songsRepository: SongsRepository) : ViewModel() {

    private val _songsList: MutableLiveData<List<Songs>> = MutableLiveData()
    private val _albumSongsList: MutableLiveData<List<Songs>> = MutableLiveData()
    private val _albumsList: MutableLiveData<List<SongAlbum>> = MutableLiveData()
    private val _isDataReady: MutableLiveData<Boolean> = MutableLiveData()
    private val _isSongPlaying : MutableLiveData<Boolean> = MutableLiveData()

    val songsList: MutableLiveData<List<Songs>>
        get() = _songsList

    val albumSongsList: MutableLiveData<List<Songs>>
        get() = _albumSongsList


    val isDataReady: MutableLiveData<Boolean>
        get() = _isDataReady

    val isSongPlaying: MutableLiveData<Boolean>
        get() = _isSongPlaying

    val albumsList: MutableLiveData<List<SongAlbum>>
        get() = _albumsList

    private var list: List<Songs>?=null
    private var listAlbums: List<SongAlbum>?=null
    private var albumSongs: List<Songs>?=null


    fun init() {
        viewModelScope.launch {
            songsRepository.fetchSongs()
            songsRepository.fetchAlbums()
        }.invokeOnCompletion { isDataReady.value = true }
    }

    fun getAllSongs() {
        viewModelScope.launch {
            list = songsRepository.getAllSongs()
        }.invokeOnCompletion { songsList.value = list?:ArrayList() }
    }

    fun getAllAlbums(){
        viewModelScope.launch {
            listAlbums = songsRepository.getAlbums()
        }.invokeOnCompletion { albumsList.value = listAlbums }
    }

    fun getAlbumSongs(id:Long?){
        viewModelScope.launch {
            albumSongs = songsRepository.getSongsByAlbum(id)
        }.invokeOnCompletion { albumSongsList.value = albumSongs }
    }

    fun setPlayStatus(play:Boolean){
        isSongPlaying.value = play
    }

    override fun onCleared() {
        super.onCleared()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (MediaUtils.mediaPlayer != null) {
                    MediaUtils.mediaPlayer.stop()
                    MediaUtils.mediaPlayer.release()
                }
            } catch (e: Exception) {
            }
            SongPlayingFragment.Staticated.mSensorManager?.unregisterListener(SongPlayingFragment.Staticated.mSensorListener)
        }
    }
}