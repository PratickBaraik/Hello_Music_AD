package com.example.hellomusic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.AssetFileDescriptor
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hellomusic.adapter.MusicAdapter
import com.example.hellomusic.data.MusicModel
import com.example.hellomusic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), MusicAdapter.SongClick {
    private lateinit var binding: ActivityMainBinding
    private lateinit var musicAdapter: MusicAdapter
    var musicService: MusicService? = null
    private var running = 0

    private val connection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as MusicService.MusicBinder
            musicService = binder.getService()
            musicService?.initializeSeekBar(binding.seekBar)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
        }
    }

    private var isMusicPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val list = getSongList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        musicAdapter = MusicAdapter(this, list, this)
        binding.recyclerView.adapter = musicAdapter

        binding.play.setOnClickListener {
            if(isMusicPlaying) {
                pause()
            } else {
                play()
            }
        }

        binding.previous.setOnClickListener {
            playPrevious()
        }

        binding.next.setOnClickListener {
            playNext()
        }
    }

    fun playPrevious() {
        val list = getSongList()
        if(running - 1 >= 0) {
            running = running - 1
        } else {
            running = list.size - 1
        }
        onSongClick(list.get(running), running)
    }

    fun playNext() {
        val list = getSongList()
        if(running + 1 <= list.size) {
            running = running + 1
        } else {
            running = 0
        }
        onSongClick(list.get(running), running)
    }

    fun play() {
        if(!isMusicPlaying) {
            musicService?.play()
            isMusicPlaying = true
            binding.play.setImageResource(R.drawable.ic_pause)
            musicAdapter?.notifyDataSetChanged()
        }
    }

    fun pause() {
        if(isMusicPlaying) {
            musicService?.pause()
            isMusicPlaying = false
            binding.play.setImageResource(R.drawable.ic_play)
            musicAdapter?.notifyDataSetChanged()
        }
    }

    private fun getSongList(): List<MusicModel> {
        return listOf(
            MusicModel(R.raw.lutt_putt_gaya, getSongTitle(R.raw.lutt_putt_gaya), getMp3FileLength(R.raw.lutt_putt_gaya)),
            MusicModel(R.raw.arjan_vailly, getSongTitle(R.raw.arjan_vailly), getMp3FileLength(R.raw.arjan_vailly)),
            MusicModel(R.raw.ittar, getSongTitle(R.raw.ittar), getSongTitle(R.raw.ittar)),
            MusicModel(R.raw.jamal_kudu, getSongTitle(R.raw.jamal_kudu), getMp3FileLength(R.raw.jamal_kudu)),
            MusicModel(R.raw.lashkare, getSongTitle(R.raw.lashkare), getMp3FileLength(R.raw.lashkare)),
            MusicModel(R.raw.million, getSongTitle(R.raw.million), getMp3FileLength(R.raw.million)),
            MusicModel(R.raw.pyaar_ban_gaye, getSongTitle(R.raw.pyaar_ban_gaye), getMp3FileLength(R.raw.pyaar_ban_gaye))
        )
    }

    override fun onSongClick(music: MusicModel, position: Int) {
        val musicIntent = Intent(this, MusicService::class.java)
        musicIntent.putExtra(
            "music_file_path",
            "android.resource://" + this?.packageName + "/" + music.resourceId
        )

        this?.startService(musicIntent)
        this.bindService(musicIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun getSongTitle(mp3ResourceId: Int): String {
        val retriever = MediaMetadataRetriever()
        val fileDes : AssetFileDescriptor = this.resources.openRawResourceFd(mp3ResourceId)
        retriever.setDataSource(
            fileDes.fileDescriptor, fileDes.startOffset, fileDes.length
        )
        val title: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        retriever.release()
        return title?: ""
    }

    fun prepare() {
        val musicIntent = Intent(this, MusicService::class.java)
        musicIntent.putExtra(
            "music_file_path",
            "android.resource://" + this?.packageName + "/" + R.raw.lutt_putt_gaya
        )

        this?.startService(musicIntent)
        this.bindService(musicIntent, connection, Context.BIND_AUTO_CREATE)
    }

    fun getMp3FileLength(mp3ResourceId: Int): String {
        val retriever = MediaMetadataRetriever()
        val fileDes : AssetFileDescriptor = this.resources.openRawResourceFd(mp3ResourceId)
        retriever.setDataSource(
            fileDes.fileDescriptor, fileDes.startOffset, fileDes.length
        )
        val duration: Long = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()?:0
        retriever.release()
        val min = (duration/1000)/60
        val sec = (duration/1000)%60

        return "$min: ${String.format("%02d", sec)}"
    }
}