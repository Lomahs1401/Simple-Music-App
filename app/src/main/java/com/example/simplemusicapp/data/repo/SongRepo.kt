package com.example.simplemusicapp.data.repo

import android.content.ContentResolver
import com.example.simplemusicapp.data.model.Song
import com.example.simplemusicapp.data.repo.source.OnResultListener
import com.example.simplemusicapp.data.repo.source.SongDataSource

class SongRepo private constructor(
    private val local: SongDataSource.Local?,
    private val remote: SongDataSource.Remote?,
) : SongDataSource.Local, SongDataSource.Remote {
    override fun getSongsLocal(
        contentResolver: ContentResolver,
        listener: OnResultListener<MutableList<Song>>
    ) {
        local!!.getSongsLocal(contentResolver, listener)
    }

    override fun getSongsRemote(listener: OnResultListener<MutableList<Song>>) {
        TODO("Not yet implemented")
    }

    companion object {
        private var instance: SongRepo? = null

        fun getInstanceSongLocalRepo(local: SongDataSource.Local): SongRepo {
            return synchronized(this) {
                instance ?: SongRepo(local, null).also {
                    instance = it
                }
            }
        }

        fun getInstanceSongRemoteRepo(remote: SongDataSource.Remote): SongRepo {
            return synchronized(this) {
                instance ?: SongRepo(null, remote).also {
                    instance = it
                }
            }
        }
    }
}