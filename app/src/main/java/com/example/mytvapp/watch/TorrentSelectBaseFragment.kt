package com.example.mytvapp.watch

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.mytvapp.DetailsActivity
import com.example.mytvapp.Movie
import com.example.mytvapp.R
import com.example.mytvapp.extension.Util.fromDpToPx

open class TorrentSelectBaseFragment : GuidedStepSupportFragment() {

    lateinit var mSelectedMovie: Movie
    lateinit var mSearchTorrentsResult: TorrentProviderSearcher.Result

    private lateinit var mGuidanceIconView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {

        arguments?.let {

            mSelectedMovie = it.getSerializable(DetailsActivity.MOVIE) as Movie

            mSearchTorrentsResult = it.getSerializable(
                DetailsActivity.SEARCH_TORRENTS_RESULT
            ) as TorrentProviderSearcher.Result
        }

        super.onCreate(savedInstanceState)
    }

    override fun onProvideTheme(): Int = R.style.Theme_MyTvApp_TorrentSelect


    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {

        return GuidanceStylist.Guidance(
            mSelectedMovie.title,
            mSelectedMovie.description,
            null,
            // Icon
            //null
            ContextCompat.getDrawable(requireContext(), R.drawable.movie)
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mGuidanceIconView = view.findViewById(androidx.leanback.R.id.guidance_icon)

        loadMoviePosterIntoGuidanceIconView()
    }


    private fun loadMoviePosterIntoGuidanceIconView() {

        val width = DETAIL_THUMB_WIDTH.fromDpToPx(requireContext())
        val height = DETAIL_THUMB_HEIGHT.fromDpToPx(requireContext())

        Glide.with(requireContext())
            .load(mSelectedMovie.cardImageUrl)
            .override(width, height)
            .transform(RoundedCorners(36))
            .error(R.drawable.default_background)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {

                    val resource = resource.toBitmap(width, height)

                    mGuidanceIconView.setImageDrawable(
                        resource.toDrawable(resources)
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not yet implemented
                }
            })
    }


    companion object {

        private const val DETAIL_THUMB_WIDTH = 300
        private const val DETAIL_THUMB_HEIGHT = 380
    }
}