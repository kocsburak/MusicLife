package com.xva.musiclife.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xva.musiclife.R
import com.xva.musiclife.adapters.MusicViewPagerFragmentAdapter
import kotlinx.android.synthetic.main.fragment_music.view.*




class MusicFragment : Fragment() {


    private lateinit var mView : View
    private var isLoaded = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(!isLoaded){
            mView = inflater.inflate(R.layout.fragment_music,container,false)
            setupFragments()
            isLoaded = true
        }

        return mView
    }



    private fun setupFragments() {
        Log.e("setup","yes")
        mView.viewPager.adapter = MusicViewPagerFragmentAdapter(activity!!.supportFragmentManager)
        mView.viewPager.currentItem = 0
        mView.tabLayout.setupWithViewPager(mView.viewPager)

        mView.tabLayout.getTabAt(0)!!.text = activity!!.resources.getString(R.string.text_songs)
        mView.tabLayout.getTabAt(1)!!.text = activity!!.resources.getString(R.string.text_playlist)
        mView.tabLayout.getTabAt(2)!!.text = activity!!.resources.getString(R.string.text_artist)

    }





}