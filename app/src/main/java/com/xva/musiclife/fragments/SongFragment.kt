package com.xva.musiclife.fragments


import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.xva.musiclife.R
import com.xva.musiclife.activities.SongSettingsActivity
import com.xva.musiclife.adapters.SongAdapter
import com.xva.musiclife.data.SharedPrefencesHelper
import com.xva.musiclife.models.Song
import com.xva.musiclife.utils.EventBusHelper
import kotlinx.android.synthetic.main.fragment_songs.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.ByteArrayOutputStream


class SongFragment() : Fragment(), SongAdapter.onItemClick {


    private var mTAG = "SongFragment"
    private lateinit var mView: View
    private lateinit var songsAdapter: SongAdapter
    private lateinit var recyclerViewSongs: RecyclerView
    private lateinit var sharedPrefencesHelper: SharedPrefencesHelper
    private var songs = ArrayList<Song>()


    // İzin Geri Bildirim Kodu
    private val READ_EXTERNAL_STORAGE_CODE = 10

    // Son Çalan Şarkının Listedeki Poziyon Bilgisi
    private var lastPlayedSongPosition = -1
    // Recycler View Kaydırma ki En Son Konumu
    private var oldState = -10000
    private var isShuffleButtonShowed = false


    private fun log(key: String, value: String) {
        Log.e(mTAG + key, value)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_songs, container, false)
        sharedPrefencesHelper = SharedPrefencesHelper(activity!!)
        recyclerViewSongs = mView.findViewById(R.id.recylerViewSongs) as RecyclerView

        // Shuffle Butonunu Listeyi Kaydırınca Gösterme İşlemi
        recyclerViewSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.e("dx", dy.toString())
                if (dy > 0) {
                    if (!isShuffleButtonShowed) {
                        isShuffleButtonShowed = true
                        showShuffleButton()
                    }
                } else {
                    hideShuffleButton()
                    isShuffleButtonShowed = false

                }
                oldState = dy

            }
        })
        // Şarkı Karıştır Butonu
        mView.buttonFirstShuffle.setOnClickListener {

            mView.buttonFirstShuffle.startAnimation(AnimationUtils.loadAnimation(activity!!, R.anim.scale))
        }

        log("onCreate", "Çalıştı")
        return mView
    }

    override fun onStart() {
        super.onStart()
        log("onStart", "Çalıştı")
        EventBus.getDefault().register(this)
        readPermission()
    }

    override fun onStop() {
        super.onStop()
        log("onStop", "Çalıştı")
        EventBus.getDefault().unregister(this)
    }

    // Datalara Erişim İzni
    private fun readPermission() {
        if (checkSelfPermission(
                activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_CODE
            )

        } else {
            loadEverything()
        }
    }

    private fun loadEverything() {
        getSongs()
    }

    // MP3 Dosyalarını Çekme İşlemi
    private fun getSongs() {
        if (songs.size == 0) {

            var contentResolver = activity!!.contentResolver
            var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            var cursor = contentResolver.query(uri, null, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    var song = Song(
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                        null,
                        false
                    )


                    songs.add(song)
                } while (cursor.moveToNext())
            }
        }
        log("getSongs", "Çalıştı")
/*
        var song = Song("ss", "ss", "ss", "ss", null, false)
        for (i in 0..20) {
            songs.add(song)
        }
*/
        setupAdapter()
    }

    private fun getAlbumart(album_id: Long?): ByteArray? {
        var bm: Bitmap? = null
        try {
            val sArtworkUri = Uri
                .parse("content://media/external/audio/albumart")

            val uri = ContentUris.withAppendedId(sArtworkUri, album_id!!)

            val pfd = context!!.contentResolver
                .openFileDescriptor(uri, "r")

            if (pfd != null) {
                val fd = pfd.fileDescriptor
                bm = BitmapFactory.decodeFileDescriptor(fd)
                var stream = ByteArrayOutputStream()
                bm.compress(Bitmap.CompressFormat.PNG, 100, stream)
                var byteArray = stream.toByteArray()
                bm.recycle()
                Log.e("songPhoto", byteArray.toString())
                return byteArray
            }
        } catch (e: Exception) {
        }
        Log.e("songPhoto", "null")
        return null
    }

    private fun setupAdapter() {
        songsAdapter = SongAdapter(songs, this, activity!!)
        recyclerViewSongs.adapter = songsAdapter
        log("setupAdapter", "Çalıştı")
        if (songs.size > 0) {
            showList()
        }
    }

    // İzin Geri Bildirimi
    @Subscribe(sticky = true)
    internal fun onDataEvent(data: EventBusHelper.permissionStatus) {

        if (data.status == "granted") {
            loadEverything()
            log("onEventPermissionStatus","Granted")
        } else {
            // TODO : Dialog ile İzin Lazım De Ve Ayarlara Götürecek bir button koy
            Toast.makeText(activity!!, "We Need Permission", Toast.LENGTH_LONG).show()
            log("onEventPermissionStatus","Denied")
        }

    }

    // Son Çalan Şarkının Position Degeri -> Textin  Yeşil Olan Rengini  Beyaz Yapmak İçin
    @Subscribe(sticky = true)
    internal fun onDataEvent(data: EventBusHelper.lastSongPosition) {
        // en son çalınan şarkının view ını aldık on item clickte rengini degiştirmek için
        this.lastPlayedSongPosition = data.position
        log("onEventLastSongPosition","Çalıştı")
    }

    // Çalan Şarkı Bilgisi -> Çalan Şarkının Text tini Yeşil Yapmak İçin
    @Subscribe(sticky = true)
    internal fun onDataEvent(data: EventBusHelper.playingSong) {
        findNewSongPosition(data.song)
        log("onEventPlayinSong","Çalıştı")
    }

    // Çalan Şarkının Listede Hangi Poziyonda Olduğunu Bulmak
    private fun findNewSongPosition(song: Song) {

        for (i in 0..songs.size) {
            if (song.path == songs[i].path) {
                changeSongTextColor(i)
                break
            }
        }
        log("findNewSongPosition","Çalıştı")
    }

    // En Son Çalan Şarkının Rengini Beyaz , Çalan Şarkının Rengini Yeşil Yapma
    private fun changeSongTextColor(index: Int) {
        if (lastPlayedSongPosition != -1) {
            songs[lastPlayedSongPosition].isPlaying = false
            songsAdapter.notifyItemChanged(lastPlayedSongPosition)
        }

        songs[index].isPlaying = true
        songsAdapter.notifyItemChanged(index)
        lastPlayedSongPosition = index
        log("changeSongTextColor","Çalıştı")
    }

    // Şarkı Değiştir Yada Song Settingse Git
    override fun onItemClick(view: View?, position: Int?, `object`: String?) {
        if (`object` == "mainView") {
            changeSongTextColor(position!!)
        } else {
            // Song Settings
            startSongsSetting()
        }
    }

    private fun showShuffleButton() {
        mView.buttonFirstShuffle.visibility = View.VISIBLE
    }

    private fun hideShuffleButton() {
        mView.buttonFirstShuffle.visibility = View.GONE
    }

    private fun showList() {
        mView.textViewEmptyList.visibility = View.GONE
        mView.recylerViewSongs.visibility = View.VISIBLE
    }

    private fun startSongsSetting() {
        startActivity(Intent(activity!!, SongSettingsActivity::class.java))
        activity!!.overridePendingTransition(R.anim.enter_from_down, R.anim.exit_to_up)
    }


}