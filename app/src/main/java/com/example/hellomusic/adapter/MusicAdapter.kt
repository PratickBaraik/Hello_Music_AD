package com.example.hellomusic.adapter

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hellomusic.MainActivity
import com.example.hellomusic.R
import com.example.hellomusic.data.MusicModel

class MusicAdapter(
    private val context: Context,
    private val musicList: List<MusicModel>,
    private val listener: SongClick
): RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return MusicViewHolder(view)
    }

    override fun getItemCount(): Int {
        return musicList.size;
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val currentMusic = musicList[position]
        holder.bind(currentMusic, listener, position, getmp3Thumbnail(currentMusic.resourceId), context)
    }

    inner class MusicViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(mp3: MusicModel, listener: SongClick, position: Int, bm: Bitmap?, context: Context) {
            val songTitle: TextView = itemView.findViewById(R.id.title)
            val songImage: ImageView = itemView.findViewById(R.id.img1)

            songTitle.text = mp3.title
            if(bm != null) {
                songImage.setImageBitmap(bm)
            } else {
                songImage.setImageResource(R.drawable.ic_music)
            }

            songTitle.setOnClickListener {
                listener.onSongClick(mp3, position)
            }
        }
    }

    interface SongClick {
        fun onSongClick(music:MusicModel, position: Int)
    }

    private fun getmp3Thumbnail(mp3ResouceId: Int): Bitmap? {
        val retriever = MediaMetadataRetriever()
        val fileDes: AssetFileDescriptor = context.resources.openRawResourceFd(mp3ResouceId)
        retriever.setDataSource(
            fileDes.fileDescriptor, fileDes.startOffset, fileDes.length
        )

        val songPic: ByteArray? = retriever.embeddedPicture
        if(songPic != null) {
            return BitmapFactory.decodeByteArray(songPic, 0, songPic.size)
        }

        retriever.release()
        return null
    }
}