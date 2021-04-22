package com.example.mytvapp

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * Details activity class that loads [VideoDetailsFragment] class.
 */
class DetailsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
    }

    companion object {
        const val SHARED_ELEMENT_NAME = "hero"
        const val MOVIE = "Movie"
        const val SEARCH_TORRENTS_RESULT = "mytvapp-search-torrents-result"
    }
}