package com.xva.musiclife.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import com.xva.musiclife.R
import com.xva.musiclife.data.SharedPrefencesHelper
import com.xva.musiclife.fragments.DiscoverFragment
import com.xva.musiclife.fragments.MiniPlFragment
import com.xva.musiclife.fragments.MusicFragment
import com.xva.musiclife.utils.EventBusHelper
import kotlinx.android.synthetic.main.activity_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var handler: Handler
    private lateinit var sharedPrefencesHelper: SharedPrefencesHelper
    private var isMiniPlShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        sharedPrefencesHelper = SharedPrefencesHelper(this)
        handler = Handler()
        load()
    }


    private fun load() {
        setClicks()
        showToolBar()
        setupDiscoveryFragment("none")
        setupMiniPlFragment()

        // kullanıcının dinlediği son müziği göstermek için
        if (sharedPrefencesHelper.getLastSong().path != "-1") {
            showMiniPl()
        }
    }

    private fun setClicks() {
        textViewDiscover.setOnClickListener(this)
        textViewMusic.setOnClickListener(this)
        imageViewSettings.setOnClickListener(this)

    }

    private fun showToolBar() {
        groupToolbar.visibility = View.VISIBLE
    }


    private fun setupDiscoveryFragment(status: String) {
        var fragmentTransaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag("Discover")

        if (fragment == null) {
            fragmentTransaction.replace(R.id.fragment_container, DiscoverFragment(), "Discover")
        } else {
            fragmentTransaction.replace(R.id.fragment_container, fragment)
        }

        if (status == "animation") {
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_left, 0, 0, R.anim.exit_to_right)
        }
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        // Uygulama Açılırsa Discover Fragment Animasyonlu Degil Direk Yüklensin,Aksi Takdirde Animasyonlu

    }


    private fun setupMiniPlFragment() {
        var fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mini_pl, MiniPlFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    // Bottom Player
    private fun showMiniPl() {
        mini_pl.visibility = View.VISIBLE
        mini_pl.animation = AnimationUtils.loadAnimation(this, R.anim.enter_from_right)
        isMiniPlShowing = true
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.textViewDiscover -> {
                handler.postDelayed(clickDiscoverAnimation, 200)
                setupDiscoveryFragment("animation")
                hideSearchIcon()
            }

            R.id.textViewMusic -> {
                handler.postDelayed(clickMusicAnimation, 200)
                setupMusicFragment()
                showSearchIcon()
            }

            R.id.imageViewSettings -> {
                imageViewSettings.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale))
                openSettings()
            }

        }
    }


    private val clickDiscoverAnimation = Runnable {

        textViewDiscover.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
        textViewMusic.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
        textViewDiscover.alpha = 1f
        textViewMusic.alpha = 0.5f
    }

    private val clickMusicAnimation = Runnable {
        textViewDiscover.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
        textViewMusic.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
        textViewDiscover.alpha = 0.5f
        textViewMusic.alpha = 1f
    }

    private fun showSearchIcon() {
        imageViewSearch.visibility = View.VISIBLE
    }

    private fun hideSearchIcon() {
        imageViewSearch.visibility = View.GONE
    }

    private fun setupMusicFragment() {
        var fragmentTransaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag("Music")

        if (fragment == null) {
            fragmentTransaction.replace(R.id.fragment_container, MusicFragment(), "Music")
        } else {
            fragmentTransaction.replace(R.id.fragment_container, fragment)
        }
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, 0, 0, R.anim.exit_to_left)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
    }

    private fun openPlayerActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
        overridePendingTransition(R.anim.enter_from_down, R.anim.exit_to_up)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true)
    internal fun onDataEvent(data: EventBusHelper.isShowMiniPl) {
        if (data.status == "show" && !(isMiniPlShowing)) {
            showMiniPl()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {

            10 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EventBus.getDefault().postSticky(EventBusHelper.permissionStatus("granted"))
                } else {
                    EventBus.getDefault().postSticky(EventBusHelper.permissionStatus("denied"))
                }
            }

        }

    }

    override fun onBackPressed() {
        this.finish()
    }


}
