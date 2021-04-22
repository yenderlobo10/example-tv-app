package com.example.mytvapp.watch

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.GuidedStepSupportFragment

class TorrentSelectActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firstFragment = TorrentSelectPlayItemFragment().also {
            it.arguments = intent?.extras!!
        }

        GuidedStepSupportFragment.addAsRoot(
            this,
            firstFragment,
            android.R.id.content
        )
    }
}