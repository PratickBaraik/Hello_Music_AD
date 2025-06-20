package com.example.hellomusic

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.widget.SeekBar

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

    fun initializeSeekBar(seekBar: SeekBar) {
       seekBar.max = mediaPlayer?.duration?: 0
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        val handler = Handler()
        handler.postDelayed(object: Runnable {
            override fun run() {
                if(mediaPlayer != null) {
                    try {
                        seekBar.progress = mediaPlayer?.currentPosition?: 0
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    handler.removeCallbacks(this)
                }
                handler.postDelayed(this, 1000)
            }
        }, 0)
    }
}