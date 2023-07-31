package com.example.simplemusicapp.screen

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.simplemusicapp.R
import com.example.simplemusicapp.data.model.Song
import com.example.simplemusicapp.databinding.ActivityMusicPlayerBinding
import com.example.simplemusicapp.service.SongService
import com.example.simplemusicapp.utils.Constant
import java.util.concurrent.TimeUnit
import androidx.palette.graphics.Palette

class MusicPlayerActivity : AppCompatActivity() {
    private val binding: ActivityMusicPlayerBinding by lazy {
        ActivityMusicPlayerBinding.inflate(layoutInflater)
    }

    private var handler = Handler()
    private var mediaPlayer = MediaPlayer()

    private var song = Song()
    private var listSongs = ArrayList<Song>()
    private var isPlaying = false
    private var isDominantColorWhite = true
    private var isClearSong = false
    private var currentSongPosition = 0
    private var currentPlayingSongPath: String? = ""

    private val updateCurrentTime = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                binding.seekBar.progress = currentPosition
                binding.tvCurrentTime.text = convertMillisecondsToMMSS(currentPosition)
            }
            handler.postDelayed(this, 1000)
        }
    }

    private val updateSeekBarTime = object : Runnable {
        override fun run() {
            if (mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                binding.seekBar.progress = currentPosition
                binding.tvCurrentTime.text = convertMillisecondsToMMSS(currentPosition)
            }
            handler.postDelayed(this, 1000)
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent?.extras ?: return
            song = bundle.getParcelable<Song>(Constant.OBJECT_SONG_KEY) as Song
            isPlaying = bundle.getBoolean(Constant.STATUS_PLAYER_KEY)

            when (bundle.getInt(Constant.ACTION_MUSIC_SERVICE_KEY)) {
                Constant.ACTION_PAUSE -> handlePlayMusic()
                Constant.ACTION_RESUME -> handlePlayMusic()
                Constant.ACTION_PLAY_NEXT_SONG -> playNextSong()
                Constant.ACTION_PLAY_PREVIOUS_SONG -> playPreviousSong()
                Constant.ACTION_CLEAR -> clearSong()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver, IntentFilter(Constant.SEND_ACTION_TO_MUSIC_PLAYER_KEY)
        )

        if (savedInstanceState != null) {
            currentPlayingSongPath = savedInstanceState.getString(Constant.CURRENT_SONG_PATH_KEY)
            isPlaying = savedInstanceState.getBoolean(Constant.STATUS_PLAYER_KEY)
            currentSongPosition = savedInstanceState.getInt(Constant.SONG_POSITION_KEY, 0)
        }

        val bundle = intent?.extras
        if (bundle != null) {
            song = bundle.get(Constant.OBJECT_SONG_KEY) as Song
            isPlaying = bundle.getBoolean(Constant.STATUS_PLAYER_KEY)
            currentSongPosition = bundle.getInt(Constant.SONG_POSITION_KEY, 0)
        }

        listSongs = intent.getParcelableArrayListExtra(Constant.LIST_SONG_KEY) ?: ArrayList()

        startRotateAnimation()
        showMediaPlayerUI(song)

        if (mediaPlayer.isPlaying) {
            if (song.path != currentPlayingSongPath) {
                mediaPlayer.reset()
            }
        }

        mediaPlayer.apply {
            setDataSource(song.path)
            prepareAsync()

            setOnPreparedListener { song ->
                // Get the total duration of the music
                val totalDurationMillis = song.duration
                binding.tvTotalTime.text = convertMillisecondsToMMSS(totalDurationMillis)

                // Set initial progress to 0
                binding.seekBar.progress = 0
                binding.seekBar.max = totalDurationMillis

                // Update current time value
                handler.post(updateCurrentTime)
                // Update seekbar and current time value
                handler.post(updateSeekBarTime)

                song.start()
            }

            setOnCompletionListener {
                // When the song finishes playing, update the UI and reset MediaPlayer
                this@MusicPlayerActivity.isPlaying = false
                setStatusButtonPlayOrPause(isDominantColorWhite)

                binding.seekBar.progress = 0
                binding.tvCurrentTime.text = convertMillisecondsToMMSS(0)
                mediaPlayer.seekTo(0)

                // Stop updating current time after completion
                handler.removeCallbacks(updateCurrentTime)

                // Clear rotating animation
                setRotateToDefault()
            }
        }

        binding.btnPlayOrPause.setOnClickListener {
            handlePlayMusic()
        }

        binding.btnNext.setOnClickListener {
            playNextSong()
        }

        binding.btnPrevious.setOnClickListener {
            playPreviousSong()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    binding.tvCurrentTime.text = convertMillisecondsToMMSS(progress)
                }
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {
                handler.removeCallbacks(updateSeekBarTime)
            }

            override fun onStopTrackingTouch(seekbar: SeekBar?) {
                seekbar?.let {
                    mediaPlayer.seekTo(it.progress)
                    handler.post(updateSeekBarTime)
                }
            }
        })
    }

    private fun startRotateAnimation() {
        val runnable = object : Runnable {
            override fun run() {
                binding.imgSong.animate()
                    .rotationBy(360f)
                    .withEndAction(this)
                    .setDuration(20000)
                    .setInterpolator(LinearInterpolator())
                    .start()
            }
        }

        binding.imgSong.animate()
            .rotationBy(360f)
            .withEndAction(runnable)
            .setDuration(20000)
            .setInterpolator(LinearInterpolator())
            .start()
    }

    private fun stopRotateAnimation() {
        binding.imgSong.animate().cancel()
    }

    private fun setRotateToDefault() {
        binding.imgSong.animate().cancel()
        binding.imgSong.rotation = 0f
    }

    private fun isColorDark(color: Int): Boolean {
        // Calculate the luminance of the color
        val luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        // Return true if the luminance is less than or equal to 0.5 (considered dark), false otherwise (considered light)
        return luminance <= 0.5
    }

    private fun showMediaPlayerUI(song: Song) {
        // Check current Night Mode and set background color for RelativeLayout
        val nightMode = AppCompatDelegate.getDefaultNightMode()

        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            val textColor = Color.WHITE

            binding.tvNameSong.text = song.title
            binding.tvNameSong.setTextColor(textColor)
            binding.tvCurrentTime.setTextColor(textColor)
            binding.tvTotalTime.setTextColor(textColor)

            binding.btnNext.setImageResource(R.drawable.white_skip_next_24)
            binding.btnPrevious.setImageResource(R.drawable.white_skip_previous_24)

            binding.imgBackground.setImageResource(R.drawable.night_background)
            Glide.with(this)
                .load(song.image)
                .into(binding.imgSong)

            setStatusButtonPlayOrPause(false)
        } else {
            // Load the album artwork from the Uri provided in the song.image property
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.default_background)
            var textColor = Color.BLACK

            Palette.from(bitmap).generate { palette ->
                val dominantColor = palette?.dominantSwatch?.rgb ?: Color.TRANSPARENT
                binding.root.setBackgroundColor(dominantColor)

                val isColorDark = isColorDark(dominantColor)
                if (isColorDark) {
                    textColor = Color.WHITE
                    isDominantColorWhite = false
                }

                val opacityColor = Color.argb(
                    (255 * 0.6).toInt(),
                    Color.red(dominantColor),
                    Color.green(dominantColor),
                    Color.blue(dominantColor)
                )
                binding.imgBackground.setBackgroundColor(opacityColor)
                binding.imgBackground.alpha = 0.5F

                binding.tvNameSong.text = song.title
                binding.tvNameSong.setTextColor(textColor)
                binding.tvCurrentTime.setTextColor(textColor)
                binding.tvTotalTime.setTextColor(textColor)

                if (textColor == Color.WHITE) {
                    binding.btnNext.setImageResource(R.drawable.white_skip_next_24)
                    binding.btnPrevious.setImageResource(R.drawable.white_skip_previous_24)
                } else {
                    binding.btnNext.setImageResource(R.drawable.black_skip_next_24)
                    binding.btnPrevious.setImageResource(R.drawable.black_skip_previous_24)
                }

                Glide.with(this)
                    .load(song.image) // Use the album artwork URI from the Song object
                    .into(binding.imgBackground)

                Glide.with(this)
                    .load(song.image)
                    .into(binding.imgSong)

                setStatusButtonPlayOrPause(isDominantColorWhite)
            }
        }
    }

    private fun setStatusButtonPlayOrPause(isDominantColorWhite: Boolean) {
        if (isPlaying) {
            if (isDominantColorWhite) {
                binding.btnPlayOrPause.setImageResource(R.drawable.black_pause_circle_24px)
            } else {
                binding.btnPlayOrPause.setImageResource(R.drawable.white_pause_circle_24)
            }
        } else {
            if (isDominantColorWhite) {
                binding.btnPlayOrPause.setImageResource(R.drawable.black_play_circle_24px)
            } else {
                binding.btnPlayOrPause.setImageResource(R.drawable.white_play_circle_24)
            }
        }
    }

    private fun sendNewNotificationOfSong(song: Song, isPlaying: Boolean, action: Int) {
        val intent = Intent(this, SongService::class.java)
        val bundle = Bundle().apply {
            putParcelable(Constant.OBJECT_SONG_KEY, song)
            putBoolean(Constant.STATUS_PLAYER_KEY, isPlaying)
        }
        intent.putExtra(Constant.ACTION_MUSIC_SERVICE_KEY, action)
        intent.putExtras(bundle)

        startService(intent)
    }

    private fun handlePlayMusic() {
        if (isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
            sendNewNotificationOfSong(song, false, Constant.ACTION_PAUSE)

            // Remove current time update if music is paused
            handler.removeCallbacks(updateCurrentTime)

            setStatusButtonPlayOrPause(isDominantColorWhite)
            stopRotateAnimation()
        } else {
            if (isClearSong) {
                startRotateAnimation()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(song.path)
                    prepareAsync()

                    setOnPreparedListener { song ->
                        // Get the total duration of the music
                        val totalDurationMillis = song.duration
                        binding.tvTotalTime.text = convertMillisecondsToMMSS(totalDurationMillis)

                        // Set initial progress to 0
                        binding.seekBar.progress = 0
                        binding.seekBar.max = totalDurationMillis

                        // Update current time value
                        handler.post(updateCurrentTime)
                        // Update seekbar and current time value
                        handler.post(updateSeekBarTime)

                        song.start()
                    }

                    setOnCompletionListener {
                        // When the song finishes playing, update the UI and reset MediaPlayer
                        this@MusicPlayerActivity.isPlaying = false
                        setStatusButtonPlayOrPause(isDominantColorWhite)

                        binding.seekBar.progress = 0
                        binding.tvCurrentTime.text = convertMillisecondsToMMSS(0)
                        mediaPlayer.seekTo(0)

                        // Stop updating current time after completion
                        handler.removeCallbacks(updateCurrentTime)

                        // Clear rotating animation
                        setRotateToDefault()
                    }
                }
                isClearSong = false
                isPlaying = true

                sendNewNotificationOfSong(song, true, Constant.ACTION_RESUME)
                setStatusButtonPlayOrPause(isDominantColorWhite)
            } else {
                mediaPlayer.start()
                isPlaying = true
                sendNewNotificationOfSong(song, true, Constant.ACTION_RESUME)

                // Start updating current time when music starts playing
                handler.post(updateCurrentTime)

                setStatusButtonPlayOrPause(isDominantColorWhite)
                startRotateAnimation()
            }
        }
    }

    private fun playNextSong() {
        currentSongPosition = (currentSongPosition + 1) % listSongs.size
        song = listSongs[currentSongPosition]

        mediaPlayer.apply {
            stop()
            release()
        }

        setRotateToDefault()
        startRotateAnimation()
        showMediaPlayerUI(song)

        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.path)
            prepareAsync()

            setOnPreparedListener { song ->
                // Get the total duration of the music
                val totalDurationMillis = song.duration
                binding.tvTotalTime.text = convertMillisecondsToMMSS(totalDurationMillis)

                // Set initial progress to 0
                binding.seekBar.progress = 0
                binding.seekBar.max = totalDurationMillis

                // Update current time value
                handler.post(updateCurrentTime)
                // Update seekbar and current time value
                handler.post(updateSeekBarTime)

                song.start()

                this@MusicPlayerActivity.isPlaying = true
                setStatusButtonPlayOrPause(isDominantColorWhite)
            }

            setOnCompletionListener {
                // When the song finishes playing, update the UI and reset MediaPlayer
                this@MusicPlayerActivity.isPlaying = false
                setStatusButtonPlayOrPause(isDominantColorWhite)

                binding.seekBar.progress = 0
                binding.tvCurrentTime.text = convertMillisecondsToMMSS(0)
                mediaPlayer.seekTo(0)

                // Stop updating current time after completion
                handler.removeCallbacks(updateCurrentTime)

                // Clear rotating animation
                setRotateToDefault()
            }
        }

        sendNewNotificationOfSong(song, true, Constant.ACTION_PLAY_NEXT_SONG)
    }

    private fun playPreviousSong() {
        currentSongPosition = if (currentSongPosition > 0) currentSongPosition - 1 else listSongs.size - 1
        song = listSongs[currentSongPosition]

        mediaPlayer.apply {
            stop()
            release()
        }

        setRotateToDefault()
        startRotateAnimation()
        showMediaPlayerUI(song)

        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.path)
            prepareAsync()

            setOnPreparedListener { song ->
                // Get the total duration of the music
                val totalDurationMillis = song.duration
                binding.tvTotalTime.text = convertMillisecondsToMMSS(totalDurationMillis)

                // Set initial progress to 0
                binding.seekBar.progress = 0
                binding.seekBar.max = totalDurationMillis

                // Update current time value
                handler.post(updateCurrentTime)
                // Update seekbar and current time value
                handler.post(updateSeekBarTime)

                song.start()

                this@MusicPlayerActivity.isPlaying = true
                setStatusButtonPlayOrPause(isDominantColorWhite)
            }

            setOnCompletionListener {
                // When the song finishes playing, update the UI and reset MediaPlayer
                this@MusicPlayerActivity.isPlaying = false
                setStatusButtonPlayOrPause(isDominantColorWhite)

                binding.seekBar.progress = 0
                binding.tvCurrentTime.text = convertMillisecondsToMMSS(0)
                mediaPlayer.seekTo(0)

                // Stop updating current time after completion
                handler.removeCallbacks(updateCurrentTime)

                // Clear rotating animation
                setRotateToDefault()
            }
        }

        sendNewNotificationOfSong(song, true, Constant.ACTION_PLAY_PREVIOUS_SONG)
    }

    private fun clearSong() {
        val intent = Intent(this, SongService::class.java)
        stopService(intent)

        mediaPlayer.apply {
            reset()
            seekTo(0)
        }
        binding.seekBar.progress = 0
        binding.tvCurrentTime.text = convertMillisecondsToMMSS(0)

        // Stop updating current time after completion
        handler.removeCallbacks(updateCurrentTime)

        isPlaying = false
        isClearSong = true

        setRotateToDefault()
        setStatusButtonPlayOrPause(isDominantColorWhite)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra(Constant.STATUS_PLAYER_KEY, isPlaying)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRotateAnimation()
        handler.removeCallbacksAndMessages(null)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the state of the MediaPlayerActivity
        outState.putString(Constant.CURRENT_SONG_PATH_KEY, song.path)
        outState.putBoolean(Constant.STATUS_PLAYER_KEY, isPlaying)
        outState.putInt(Constant.SONG_POSITION_KEY, currentSongPosition)
    }

    companion object {
        fun convertMillisecondsToMMSS(durationMillis: Int): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis.toLong())
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis.toLong()) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}
