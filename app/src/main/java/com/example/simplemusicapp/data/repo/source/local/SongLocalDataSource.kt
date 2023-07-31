package com.example.simplemusicapp.data.repo.source.local

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.example.simplemusicapp.data.model.Song
import com.example.simplemusicapp.data.repo.source.OnResultListener
import com.example.simplemusicapp.data.repo.source.SongDataSource
import com.example.simplemusicapp.utils.Constant
import java.util.concurrent.Executors

class SongLocalDataSource : SongDataSource.Local {

    private var executor = Executors.newSingleThreadExecutor()

    override fun getSongsLocal(
        contentResolver: ContentResolver,
        listener: OnResultListener<MutableList<Song>>
    ) {
        executor.execute {
            val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val cursor = contentResolver.query(songUri, null, null, null, null)
            val listSongs = mutableListOf<Song>()

            if (cursor != null && cursor.moveToFirst()) {
                val columnId = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val columnTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val columnArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val columnData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val columnDate = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val columnDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val columnAlbumId = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val songId = cursor.getLong(columnId)
                    val songTitle = cursor.getString(columnTitle)
                    val songArtist = cursor.getString(columnArtist)
                    val songData = cursor.getString(columnData)
                    val songDate = cursor.getLong(columnDate)
                    val songDuration = cursor.getInt(columnDuration)
                    val songAlbumId = cursor.getLong(columnAlbumId)

                    val imageUri = Uri.parse(Constant.ALBUM_ART)
                    val albumUri = ContentUris.withAppendedId(imageUri, songAlbumId)

                    val song = Song(
                        id = songId,
                        path = songData,
                        title = songTitle,
                        artist = songArtist,
                        duration = songDuration,
                        date = songDate,
                        image = albumUri
                    )

                    listSongs.add(song)
                }
                cursor.close()
            }
            listener.onSuccess(listSongs)
        }
    }

    companion object {
        private var instance: SongLocalDataSource? = null

        fun getInstance() = synchronized(this) {
            instance ?: SongLocalDataSource().also { instance = it }
        }
    }
}