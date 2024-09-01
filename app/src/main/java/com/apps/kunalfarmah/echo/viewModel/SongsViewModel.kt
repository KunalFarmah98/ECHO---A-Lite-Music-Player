package com.apps.kunalfarmah.echo.viewModel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(true)
    private val _isSongPlaying : MutableLiveData<Boolean> = MutableLiveData()

    val songsList: MutableLiveData<List<Songs>>
        get() = _songsList

    val albumSongsList: MutableLiveData<List<Songs>>
        get() = _albumSongsList


    val isLoading: MutableLiveData<Boolean>
        get() = _isLoading

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
        }
    }

    fun getAllSongs() {
        viewModelScope.launch {
            isLoading.value = true
            list = songsRepository.getSongsFromPhone()
        }.invokeOnCompletion {
            songsList.value = list?:ArrayList()
            MediaUtils.allSongsList = (list ?: ArrayList()) as ArrayList<Songs>
            MediaUtils.setMediaItems()
            isLoading.value = false
        }
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
}