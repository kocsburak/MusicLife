package com.xva.musiclife

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.xva.musiclife.activities.HomeActivity
import com.xva.musiclife.data.SharedPrefencesHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPrefencesHelper: SharedPrefencesHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefencesHelper = SharedPrefencesHelper(this)

        if (sharedPrefencesHelper.isShowSplashScreen()) {
            var handler = Handler()
            handler.postDelayed(showIcon, 1000)
            handler.postDelayed(changeActivity, 2000)
        } else {
            changeActivity.run()
        }

    }


    private val showIcon = Runnable {
        imageViewBestPart.visibility = View.VISIBLE
    }

    private val changeActivity = Runnable {
        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
        this@MainActivity.finish()
        Animatoo.animateFade(this)
    }


}
