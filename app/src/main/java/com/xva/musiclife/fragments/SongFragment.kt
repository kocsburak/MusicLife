package com.xva.musiclife.fragments


import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
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
import kotlinx.android.synthetic.main.fragment_songs.view.*


class SongFragment() : Fragment(), SongAdapter.onItemClick {


    private var mTAG = "SongFragment"
    private lateinit var mView: View
    private lateinit var songsAdapter: SongAdapter
    private lateinit var recyclerViewSongs: RecyclerView
    private lateinit var sharedPrefencesHelper: SharedPrefencesHelper
    private var songs = ArrayList<Song>()


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

        getSongs()
        log("onCreate", "Çalıştı")
        return mView
    }

    override fun onStart() {
        super.onStart()
        log("onStart", "Çalıştı")
//        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        log("onStop", "Çalıştı")
        //  EventBus.getDefault().unregister(this)
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

    private fun setupAdapter() {
        songsAdapter = SongAdapter(songs, this, activity!!)
        recyclerViewSongs.adapter = songsAdapter
        log("setupAdapter", "Çalıştı")
        if (songs.size > 0) {
            showList()
        }
        swipeDelete()
    }

    private fun swipeDelete() {

        var swipeToDeleteCallback = object : SwipeToDeleteCallback(activity!!) {
            override fun onSwiped(p0: RecyclerView.ViewHolder, p1: Int) {
                var position = p0.adapterPosition
                var item = songsAdapter.data[position]
                songsAdapter.removeItem(position)
                showDeleteAlertDialog(item,position)
            }


        }
        var itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(recyclerViewSongs)

    }

    private fun showDeleteAlertDialog(item:Song,position: Int?){
        val builder = AlertDialog.Builder(activity!!)

        // Set the alert dialog title
        builder.setTitle("ShhSHH")

        // Display a message on alert dialog
        builder.setMessage(getString(R.string.text_remove_item_question))

        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton(getString(R.string.text_yes)){dialog, which ->
            Toast.makeText(activity!!,getString(R.string.text_item_has_removed),Toast.LENGTH_LONG).show()
        }


        // Display a negative button on alert dialog
        builder.setNegativeButton(getString(R.string.text_no)){dialog,which ->
            songsAdapter.restoreItem(item, position!!)
        }

        builder.setCancelable(false)

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
    }

    private fun showList() {
        mView.textViewEmptyList.visibility = View.GONE
        mView.recylerViewSongs.visibility = View.VISIBLE
    }


    // Şarkı Değiştir Yada Song Settingse Git
    override fun onItemClick(view: View?, position: Int?, lastSongPosition: Int?, `object`: String?) {
        if (`object` == "mainView") {
            if (lastSongPosition != -1) {
                songs[lastSongPosition!!].isPlaying = false
                songsAdapter.notifyItemChanged(lastSongPosition)
            }
            songs[position!!].isPlaying = true
            songsAdapter.notifyItemChanged(position!!)
            log("onEventLastSongPosition", "Çalıştı")
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


    private fun startSongsSetting() {
        startActivity(Intent(activity!!, SongSettingsActivity::class.java))
        activity!!.overridePendingTransition(R.anim.enter_from_down, R.anim.exit_to_up)
    }


}