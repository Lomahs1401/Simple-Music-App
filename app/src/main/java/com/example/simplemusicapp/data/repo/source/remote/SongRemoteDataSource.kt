package com.example.simplemusicapp.data.repo.source.remote

import com.example.simplemusicapp.data.model.Song
import com.example.simplemusicapp.data.repo.source.OnResultListener
import com.example.simplemusicapp.data.repo.source.SongDataSource

class SongRemoteDataSource : SongDataSource.Remote {
    override fun getSongsRemote(listener: OnResultListener<MutableList<Song>>) {
        TODO("Not yet implemented")
    }

    companion object {
        private var instance: SongRemoteDataSource? = null

        fun getInstance() = synchronized(this) {
            instance ?: SongRemoteDataSource().also { instance = it }
        }
    }
}