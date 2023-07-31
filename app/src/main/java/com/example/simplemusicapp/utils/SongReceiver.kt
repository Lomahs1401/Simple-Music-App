package com.example.simplemusicapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.simplemusicapp.data.model.Song

class SongReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val intentMusicPlayerReceiver = Intent(Constant.SEND_ACTION_TO_MUSIC_PLAYER_KEY)

        val bundle = intent?.extras
        if (bundle != null) {
            val song = bundle.get(Constant.OBJECT_SONG_KEY) as? Song
            val isPlaying = bundle.getBoolean(Constant.STATUS_PLAYER_KEY)
            val actionMusic = intent.getIntExtra(Constant.ACTION_MUSIC_KEY, 0)

            val bundleReceiver = Bundle().apply {
                putParcelable(Constant.OBJECT_SONG_KEY, song)
                putBoolean(Constant.STATUS_PLAYER_KEY, isPlaying)
                putInt(Constant.ACTION_MUSIC_SERVICE_KEY, actionMusic)
            }
            intentMusicPlayerReceiver.putExtras(bundleReceiver)
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(intentMusicPlayerReceiver)
    }
}