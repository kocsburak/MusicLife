package com.xva.musiclife.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.xva.musiclife.R
import com.xva.musiclife.data.SharedPrefencesHelper
import com.xva.musiclife.fragments.DiscoverFragment
import com.xva.musiclife.fragments.MiniPlFragment
import com.xva.musiclife.fragments.MusicFragment
import com.xva.musiclife.services.PlayerServices
import com.xva.musiclife.services.PlayerServicesBinder
import com.xva.musiclife.utils.EventBusHelper
import kotlinx.android.synthetic.main.activity_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class HomeActivity : AppCompatActivity(), View.OnClickListener {


    private var mTAG = "HomeActivity"

    // Animasyonlarda Zamanlama İçin Kullanılacak
    private lateinit var handler: Handler
    //
    private lateinit var sharedPrefencesHelper: SharedPrefencesHelper

    // MiniPl Ekranda Görüntüleniyor mu bilgisi
    private var isMiniPlShowing = false

    // service binder ı
    private var serviceBinder: PlayerServicesBinder? = null

    private val READ_EXTERNAL_STORAGE_CODE = 10

    private fun log(key: String, value: String) {
        Log.e(mTAG + key, value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        log("onCreate", "Çalıştı")
        sharedPrefencesHelper = SharedPrefencesHelper(this)
        handler = Handler()
        readPermission()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        bindAudioService()
        log("onStart", "Çalıtı")
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        unBoundAudioService()
        log("onStop", "Çalıştı")
    }


    override fun onBackPressed() {
        this.finish()
    }


    // Servise Baglanma
    private fun bindAudioService() {
        if (serviceBinder == null) {
            val intent = Intent(this, PlayerServices::class.java)
            log("bindAudioService==null", "Çalıştı")
            // Servise bağlanma
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    // Servisi Koparma
    private fun unBoundAudioService() {
        if (serviceBinder != null) {
            log("unBoundAudioService", "Çalıştı")
            unbindService(serviceConnection)
        }
    }

    // Service Connection
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            log("onServiceDisConnected","Çalıştı")
            // DO Nothing...
            Toast.makeText(this@HomeActivity,getString(R.string.error_went_wrong),Toast.LENGTH_LONG).show()
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            log("onServiceConnected","Çalıştı")
            serviceBinder = service as PlayerServicesBinder
        }

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



    // Verilere Erişim İzni Aldık
    private fun readPermission() {
        log("readPermission", "Çalıştı")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_CODE
            )

        } else {
            loadEveryThing()
        }
    }


    // Song Fragment Datalara Erişim İzni Sonucu ve Song Fragmente Geri Bildirimi
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {

            READ_EXTERNAL_STORAGE_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadEveryThing()
                    log("RequestPermission", "Granted")
                } else {
                    log("RequestPermission", "Denied")
                    Toast.makeText(this,getString(R.string.error_need_permission),Toast.LENGTH_LONG).show()
                }
            }

        }

    }


    private fun loadEveryThing() {
        setClicks()
        showToolBar()
        checkIfWeShouldLoadMiniPl()
        setupMiniPlFragment()
        setupDiscoveryFragment("none")
    }

    private fun setClicks() {
        textViewDiscover.setOnClickListener(this)
        textViewMusic.setOnClickListener(this)
        imageViewSettings.setOnClickListener(this)
    }

    private fun showToolBar() {
        groupToolbar.visibility = View.VISIBLE
    }

    // Son Şarkı Kaydı Girilmişsse Mini Pl yi Göster
    private fun checkIfWeShouldLoadMiniPl(){
        if(sharedPrefencesHelper.getLastSong().path !="-1"){
            showMiniPl()
        }
    }


    //Uygulamada Çalan Şarkıyı Kontrol Etmeye Çalışan Küçük Player
    private fun setupMiniPlFragment() {
        var fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mini_pl, MiniPlFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        log("setupMiniPlFragment", "Çalıştı")
    }


    // @params status -> Fragment Başlatılırken Geçiş Animasyonu İle mi Başlasın Yoksa Direk mi ekrana gelsin
    // Uygulama İlk Açıldığında Direk Ekrana Gelecek
    private fun setupDiscoveryFragment(status: String) {
        var fragmentTransaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag("Discover")

        if (fragment == null) {
            fragmentTransaction.replace(R.id.fragment_container, DiscoverFragment(), "Discover")
        } else {
            fragmentTransaction.replace(R.id.fragment_container, fragment)
        }

        // Geçiş Animasyonu İle Mi Açılsın Kontrolü
        if (status == "animation") {
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_left, 0, 0, R.anim.exit_to_right)
        }
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        log("setupDiscoveryFragment", "Çalıştı")
        // TODO : Geçiş Animasyonu Çalışmıyor Kontrol Et

    }



    // Mini Player Fragmentin Animasyon İle Ekrana Girmesini Sağlıyor
    private fun showMiniPl() {
        mini_pl.visibility = View.VISIBLE
        mini_pl.animation = AnimationUtils.loadAnimation(this, R.anim.enter_from_right)
        isMiniPlShowing = true
        log("showMiniPl", "Çalıştı")
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
        log("setupMusicFragment", "Çalıştı")

    }


    // Şarkı Açıldığında MiniPl Gösterimde Degilse Gösterilmesi İçin Bilgilendirme
    @Subscribe(sticky = true)
    internal fun onDataEvent(data: EventBusHelper.isShowMiniPl) {
        if (data.status == "show" && !(isMiniPlShowing)) {
            showMiniPl()
            log("onEventBusisShowMiniPl", "Çalıştı")
        }
    }


    // Discover ve Music Fragment Butonlarının Tıklanma Animasyonları
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


    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
    }

    private fun openPlayerActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
        overridePendingTransition(R.anim.enter_from_down, R.anim.exit_to_up)
    }


}
