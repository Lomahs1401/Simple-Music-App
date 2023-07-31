package com.example.simplemusicapp.utils

object Constant {
    // Create Notification Channel
    const val CHANNEL_PLAY_MUSIC = "Channel Play Music"
    // Create Media Tag
    const val MEDIA_TAG = "Tag"
    // ACTION_MUSIC
    const val NO_ACTION = 0
    const val ACTION_PAUSE = 1
    const val ACTION_RESUME = 2
    const val ACTION_CLEAR = 3
    const val ACTION_START = 4
    const val ACTION_PLAY_NEXT_SONG = 5
    const val ACTION_PLAY_PREVIOUS_SONG = 6
    // ALBUM ARTWORK
    const val ALBUM_ART = "content://media/external/audio/albumart"
    // SEND DATA
    const val MY_PREFS = "MyPrefs"
    const val OBJECT_SONG_KEY = "object_song"
    const val LIST_SONG_KEY = "list_songs"
    const val SONG_INFO = "song_info"
    const val STATUS_PLAYER_KEY = "status_player"
    const val SONG_POSITION_KEY = "song_position_key"
    const val ACTION_MUSIC_KEY = "action_music"
    const val ACTION_MUSIC_SERVICE_KEY = "action_music_service"
    const val SEND_ACTION_TO_MAIN_SONG_KEY = "send_action_to_main_song"
    const val SEND_ACTION_TO_MUSIC_PLAYER_KEY = "send_action_to_music_player"
    const val CURRENT_SONG_PATH_KEY = "current_song_path"
}