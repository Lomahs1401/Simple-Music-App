package com.example.simplemusicapp.data.repo.source

import android.content.ContentResolver
import com.example.simplemusicapp.data.model.Song

interface SongDataSource {
    /**
     * Local
     */
    interface Local {
        fun getSongsLocal(contentResolver: ContentResolver, listener: OnResultListener<MutableList<Song>>)
    }

    /**
     * Remote
     */
    interface Remote {
        fun getSongsRemote(listener: OnResultListener<MutableList<Song>>)
    }
}