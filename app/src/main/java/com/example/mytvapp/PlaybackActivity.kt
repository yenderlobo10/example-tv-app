package com.example.mytvapp

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import org.greenrobot.eventbus.EventBus

/** Loads [PlaybackVideoFragment]. */
class PlaybackActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, PlaybackVideoFragment())
                    .commit()
        }

        //EventBus.getDefault().register(this)
    }
}