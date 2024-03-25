package com.example.hellomusic

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder

class MusicService: Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var filePath: String? = null
    private var isPaused: Boolean = false
    private var binder = MusicBinder()
    private var appStarted = true

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        filePath = intent?.getStringExtra("music_file_path")
        filePath?.let {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(
                applicationContext, Uri.parse(filePath)
            )
            mediaPlayer?.prepare()
            if(!appStarted) {
                mediaPlayer?.start()
            }
            appStarted = false
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun pause() {
        mediaPlayer?.pause()
        isPaused = true
    }

    fun play() {
        if(isPaused) {
            mediaPlayer?.start()
            isPaused = false
        } else {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(
                applicationContext, Uri.parse(filePath)
            )
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        }
    }

    inner class MusicBinder: Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }
}