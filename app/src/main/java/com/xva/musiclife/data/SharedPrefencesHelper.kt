package com.xva.musiclife.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import com.xva.musiclife.models.Song

class SharedPrefencesHelper {

    private var PREFS_FILENAME = "MusicFile_Shared_Preferences"
    private var SPLASH_SCREEN_KEY = "SplashScreen"

    private var context: Context
    private var sharedPreferences: SharedPreferences
    private var editor: SharedPreferences.Editor

    constructor(context: Context) {
        this.context = context
        sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    private fun commit() {
        editor.commit()
    }


    // Settings

    fun setIsSplashScreen(boolean: Boolean) {
        editor.putBoolean(SPLASH_SCREEN_KEY, boolean)
        commit()
    }

    fun isShowSplashScreen(): Boolean {
        return sharedPreferences.getBoolean(SPLASH_SCREEN_KEY, true)
    }

    fun saveLastSong(song: Song) {

        editor.putString("LastSongName", song.name)
        editor.putString("LastSongPath", song.path)
        editor.putString("LastSongArtist", song.artist)
        editor.putString("LastSongDuration", song.duration)
        editor.putString("LastSongImage",song.image.toString())
        commit()
    }

    fun getLastSong(): Song {

        return Song(
            sharedPreferences.getString("LastSongPath", "-1"),
            sharedPreferences.getString("LastSongName", "-1"),
            sharedPreferences.getString("LastSongArtist", "-1"),
            sharedPreferences.getString("LastSongDuration", "-1"),
            sharedPreferences.getString("LastSongImage","-1").toByteArray()
        )
    }


}