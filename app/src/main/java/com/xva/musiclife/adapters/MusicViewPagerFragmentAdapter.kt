package com.xva.musiclife.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import com.xva.musiclife.fragments.PlayListFragment
import com.xva.musiclife.fragments.SongFragment

class MusicViewPagerFragmentAdapter(var manager: FragmentManager) : FragmentPagerAdapter(manager) {


    private val numColumn = 3

    override fun getItem(p0: Int): Fragment? {
        when (p0!!) {
            0 -> {
                return SongFragment()
            }
            1 -> {
                return PlayListFragment()
            }
            2 -> {
                return PlayListFragment()
            }
        }
        return null
    }

    override fun getCount(): Int {
        return numColumn
    }


}