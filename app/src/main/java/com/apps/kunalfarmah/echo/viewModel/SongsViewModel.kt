package com.apps.kunalfarmah.echo.viewModel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.kunalfarmah.echo.SongAlbum
import com.apps.kunalfarmah.echo.Songs
import com.apps.kunalfarmah.echo.database.entity.SongAlbumEntity
import com.apps.kunalfarmah.echo.repository.SongsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongsViewModel
@ViewModelInject
constructor(private val songsRepository: SongsRepository) : ViewModel() {

    private val _songsList: MutableLiveData<List<Songs>> = MutableLiveData()
    private val _albumSongsList: MutableLiveData<List<Songs>> = MutableLiveData()
    private val _albumsList: MutableLiveData<List<SongAlbum>> = MutableLiveData()
    private val _isDataReady: MutableLiveData<Boolean> = MutableLiveData()
    private val _isSongPlaying : MutableLiveData<Boolean> = MutableLiveData()
    private val _isFavouritePresent : MutableLiveData<Boolean> = MutableLiveData()

    val songsList: MutableLiveData<List<Songs>>
        get() = _songsList

    val albumSongsList: MutableLiveData<List<Songs>>
        get() = _albumSongsList


    val isDataReady: MutableLiveData<Boolean>
        get() = _isDataReady

    val isSongPlaying: MutableLiveData<Boolean>
        get() = _isSongPlaying

    val isFavoritePresent: MutableLiveData<Boolean>
        get() = _isFavouritePresent

    val albumsList: MutableLiveData<List<SongAlbum>>
        get() = _albumsList

    lateinit var list: List<Songs>
    lateinit var listAlbums: List<SongAlbum>
    lateinit var albumSongs: List<Songs>


    fun init() {
        viewModelScope.launch {
            songsRepository.fetchSongs()
            songsRepository.fetchAlbums()
        }.invokeOnCompletion { isDataReady.value = true }
    }

    fun getAllSongs() {
        viewModelScope.launch {
            list = songsRepository.getAllSongs()
        }.invokeOnCompletion { songsList.value = list }
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

    fun addFavorite(fav:Songs){
        viewModelScope.launch {
            songsRepository.insertFavorite(fav)
        }
    }

    lateinit var fav:Songs
    fun checkIfFavoriteExsits(id:Long){
        viewModelScope.launch {
            fav =  songsRepository.getFavorite(id)
        }.invokeOnCompletion {
            isFavoritePresent.value = null!=fav
        }
    }
}