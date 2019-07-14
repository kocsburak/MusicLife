package com.xva.musiclife.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import com.xva.musiclife.R
import com.xva.musiclife.data.Database
import com.xva.musiclife.models.Song
import com.xva.musiclife.utils.EventBusHelper
import kotlinx.android.synthetic.main.activity_song_settings.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SongSettingsActivity : AppCompatActivity(), View.OnClickListener {


    private var mTAG = "SongSettings"

    private lateinit var song: Song
    private var playingSong: Song? = null
    private lateinit var database: Database
    private var isAddedToFavourite = false
    private var isAddedToQueue = false
    private var songId = -1

    private var EventBusSetup = false

    private fun log(key: String, value: String) {
        Log.e(mTAG + key, value)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_settings)
        log("OnCreate", "Calisti")
        database = Database(this)
        setClicks()
    }

    override fun onStart() {
        super.onStart()
        log("OnStart", "Calisti")
        if (!EventBusSetup) {
            EventBusSetup = true
            EventBus.getDefault().register(this)
        }

    }

    override fun onStop() {
        super.onStop()
        log("OnStop", "Calisti")
        EventBus.getDefault().unregister(this)
    }


    private fun setClicks() {
        textViewCancel.setOnClickListener(this)
        imageViewFavourite.setOnClickListener(this)
        textViewFavourite.setOnClickListener(this)
        imageViewAddPlaylist.setOnClickListener(this)
        textViewAddPlaylist.setOnClickListener(this)
        imageViewQueue.setOnClickListener(this)
        textViewQueue.setOnClickListener(this)
        imageViewChangePhoto.setOnClickListener(this)
        textViewChangePhoto.setOnClickListener(this)
        log("setClicks", "Calisti")
    }

    @Subscribe(sticky = true)
    internal fun onDataEvent(data: EventBusHelper.songInformations) {
        log("EventBusSongInformatios", "Calisti")
        song = data.song
        updateInformations()
        checkSongIsAvailable()
    }


    @Subscribe(sticky = true)
    internal fun onDataEvent(data: EventBusHelper.playingSong) {
        log("EventBusPlayingSong", "Calisti")
        // TODO : Silme İşleminde Playing Song , Song ile Aynı ise Önce Service Durdurulacak, Mini Pl Den Şarkı Temizlenecek, VE sİLİNECEK
        playingSong = data.song
    }


    private fun updateInformations() {
        textViewSongName.text = song.name
        textViewArtist.text = song.artist
    }


    private fun checkSongIsAvailable() {
        songId = database.isSongAvailable(song.path)
        if (songId != -1) {
            log("checkSongIsAvailableSongId!=-1", "Calisti")
            log("SongId", songId.toString())
            getSongPhoto()
            checkIsSongAddedToFavourite()
        } else {
            database.addSong(song.path)
            songId = database.isSongAvailable(song.path)
            log("checkSongIsAvailableSongId==-1", "Calisti")
        }
    }


    private fun getSongPhoto() {
        var image = database.getPhoto(song.path)
        if (image != null) {
            log("getSongPhotoImage!=null", "Calisti")
            setSongPhoto(image)
        } else {
            log("getSongPhotoImage==null", "Calisti")
            checkAlbumCoverIsAvailable()
        }
    }

    private fun checkAlbumCoverIsAvailable() {
        if (song.image != null) {
            setSongPhoto(song.image!!)
        }
    }

    private fun setSongPhoto(image: ByteArray) {
        val imageStream = ByteArrayInputStream(image)
        val theImage = BitmapFactory.decodeStream(imageStream)
        imageViewSongPhoto.setImageBitmap(theImage)
        log("setSongPhoto", "Calisti")
    }

    private fun checkIsSongAddedToFavourite() {
        if (database.isSongAddedToFavourite(songId) != -1) {
            log("checkIsSongAddedToFavourite!=-1", "Calisti")
            addToFavourite()
        }
    }


    private fun addToFavourite() {
        log("addToFavourite", "Calisti")
        imageViewFavourite.setImageResource(R.drawable.ic_favorite_green_24dp)
        textViewFavourite.text = resources.getString(R.string.text_added)
        isAddedToFavourite = true
        checkPlayingSongEqualThisSong(true)
    }

    private fun removeFromFavourite() {
        log("removeFromFavourite", "Calisti")
        imageViewFavourite.setImageResource(R.drawable.ic_favorite_white_24dp)
        textViewFavourite.text = resources.getString(R.string.text_add_favourite)
        isAddedToFavourite = false
        checkPlayingSongEqualThisSong(false)
    }

    private fun showFavouriteAnimation() {
        imageViewFavourite.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale))
        textViewFavourite.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale))
    }

    // Çalan Şarkı İle Seçtigimiz Şarkı Aynı Ve Favourite Degişim İslemi Yapmışsak MiniPl bilgilendirilmeli
    private fun checkPlayingSongEqualThisSong(status: Boolean) {
        if (playingSong != null && playingSong!!.path == song.path) {
            log("checkPlayingSongEquelThisSong", "İçerde Calisti")
            EventBus.getDefault().postSticky(EventBusHelper.favouriteStatus(status))
        }
    }

    private fun actionFavourite() {
        if (isAddedToFavourite) {
            removeFromFavourite()
            showFavouriteAnimation()
            database.removeFromFavourite(songId)
        } else {
            addToFavourite()
            showFavouriteAnimation()
            database.addToFavourite(songId)
        }
    }


    private fun startPlayListActivity() {
        startActivity(Intent(this, PlayListActivity::class.java))
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
    }

    private fun addToQueue() {
        textViewQueue.text = resources.getString(R.string.text_added)
        isAddedToQueue = true
    }


    private fun showQueueAnimation() {
        imageViewQueue.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale))
        textViewQueue.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale))
    }


    private fun actionQueue() {
        if (!isAddedToQueue) {
            addToQueue()
            showQueueAnimation()
            isAddedToQueue = true
            EventBus.getDefault().postSticky(EventBusHelper.songForQueue(song))
            log("actionQueue", "Kuyruga Eklendi")
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, getString(R.string.text_choose_a_photo)), 1)
        log("openGallery", "Calisti")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!(requestCode != 1 || resultCode !== Activity.RESULT_OK || data == null || data.data == null)
        ) {

            log("Fotograf", "aLINDI")
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
            // convert bitmap to byte
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val imageInByte = stream.toByteArray()
            database.updatePhoto(imageInByte, song.path)
            setSongPhoto(imageInByte)
            log("SongSettingsByteArray",imageInByte.toString())


        }
    }


    private fun showChangePhotoAnimation() {
        imageViewChangePhoto.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale))
        textViewChangePhoto.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale))
    }


    override fun onClick(v: View?) {
        when (v!!.id) {

            R.id.textViewCancel -> {
                this.finish()
            }

            R.id.imageViewFavourite -> {
                actionFavourite()
            }

            R.id.textViewFavourite -> {
                actionFavourite()
            }

            R.id.imageViewAddPlaylist -> {
                startPlayListActivity()
            }

            R.id.textViewAddPlaylist -> {
                startPlayListActivity()
            }

            R.id.imageViewQueue -> {
                actionQueue()
            }

            R.id.textViewQueue -> {
                actionQueue()
            }

            R.id.imageViewChangePhoto -> {
                showChangePhotoAnimation()
                openGallery()
            }

            R.id.textViewChangePhoto -> {
                showChangePhotoAnimation()
                openGallery()
            }

            R.id.imageViewBestPart -> {

            }

            R.id.textViewBestPart -> {

            }

            R.id.imageViewGesture -> {

            }

            R.id.textViewGesture -> {

            }

        }
    }


}
