package com.example.simplemusicapp.screen

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.simplemusicapp.R
import com.example.simplemusicapp.data.model.Song
import com.example.simplemusicapp.data.repo.SongRepo
import com.example.simplemusicapp.data.repo.source.OnResultListener
import com.example.simplemusicapp.data.repo.source.SongDataSource
import com.example.simplemusicapp.data.repo.source.local.SongLocalDataSource
import com.example.simplemusicapp.databinding.ActivityMainBinding
import com.example.simplemusicapp.screen.adapter.SongAdapter
import com.example.simplemusicapp.service.SongService
import com.example.simplemusicapp.utils.Constant
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var isPlaying = false
    private var actionMusic = Constant.NO_ACTION
    private var listSongs = ArrayList<Song>()
    private val localDataSource: SongDataSource.Local = SongLocalDataSource.getInstance()
    private lateinit var songRepo: SongRepo
    private lateinit var song: Song
    private lateinit var sharedPreferences: SharedPreferences

    private val songAdapter: SongAdapter by lazy {
        SongAdapter(this) { songData ->
            startPlayMusic(songData)
        }.apply {
            setData(listSongs)
        }
    }

    private val listSongBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bundle = intent?.extras ?: return
            song = bundle.getParcelable<Song>(Constant.OBJECT_SONG_KEY) as Song
            isPlaying = bundle.getBoolean(Constant.STATUS_PLAYER_KEY)
            actionMusic = bundle.getInt(Constant.ACTION_MUSIC_KEY)

            showMusicPlayer(song)
        }
    }

    private val musicPlayerActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                isPlaying = data?.getBooleanExtra(Constant.STATUS_PLAYER_KEY, false) ?: false
            }
        }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, getString(R.string.request_permission), Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 123)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            listSongBroadcastReceiver, IntentFilter(Constant.SEND_ACTION_TO_MAIN_SONG_KEY)
        )

        songRepo = SongRepo.getInstanceSongLocalRepo(localDataSource)

        sharedPreferences = getSharedPreferences(Constant.MY_PREFS, Context.MODE_PRIVATE)
        isPlaying = sharedPreferences.getBoolean(Constant.STATUS_PLAYER_KEY, false)

        if (savedInstanceState != null && savedInstanceState.containsKey(Constant.SONG_INFO)) {
            song = savedInstanceState.getParcelable<Song>(Constant.SONG_INFO) as Song
        }

        if (!checkPermission()) {
            requestPermission()
            return
        }

        getListSongs()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(listSongBroadcastReceiver)
    }

    // Fetch musics
    private fun getListSongs() {
        songRepo.getSongsLocal(this.contentResolver, object : OnResultListener<MutableList<Song>> {
            override fun onSuccess(data: MutableList<Song>) {
                listSongs.addAll(data)
                if (listSongs.isEmpty()) {
                    binding.tvListSong.visibility = View.GONE
                    binding.tvNoSongs.visibility = View.VISIBLE
                } else {
                    songAdapter.setData(listSongs)
                    binding.recyclerViewListSongs.adapter = songAdapter
                }
            }

            override fun onError(exception: Exception?) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.error_loading_songs),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun startPlayMusic(song: Song) {
        val intent = Intent(this, SongService::class.java)

        val bundle = Bundle().apply {
            putParcelable(Constant.OBJECT_SONG_KEY, song)
            putBoolean(Constant.STATUS_PLAYER_KEY, true)
        }

        intent.putExtras(bundle)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun showMusicPlayer(song: Song) {
        val position = listSongs.indexOf(song)

        val intent = Intent(this, MusicPlayerActivity::class.java)
        val bundle = Bundle().apply {
            putParcelable(Constant.OBJECT_SONG_KEY, song)
            putBoolean(Constant.STATUS_PLAYER_KEY, isPlaying)
            putInt(Constant.SONG_POSITION_KEY, position)
        }
        intent.putParcelableArrayListExtra(Constant.LIST_SONG_KEY, listSongs)
        intent.putExtras(bundle)
        musicPlayerActivityLauncher.launch(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        menu?.let { safeMenu ->
            val nightModeItem = safeMenu.findItem(R.id.night_mode)
            nightModeItem?.setTitle(
                if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    R.string.day_mode
                } else {
                    R.string.night_mode
                }
            )
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.night_mode) {
            val nightMode = AppCompatDelegate.getDefaultNightMode()
            if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.root.setBackgroundResource(R.drawable.default_background)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.root.setBackgroundResource(R.drawable.night_background)
            }
            recreate()
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::song.isInitialized) {
            outState.putParcelable(Constant.SONG_INFO, song)
        }
    }
}