package com.xva.musiclife.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.xva.musiclife.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left,R.anim.exit_to_right)
    }

}
