package com.xva.musiclife.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xva.musiclife.R

class DiscoverFragment : Fragment() {

    private lateinit var mView: View
    private var isLoaded = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!isLoaded) {
            mView = inflater.inflate(R.layout.fragment_discover, container, false)
            isLoaded = true
        }

        return mView

    }

}