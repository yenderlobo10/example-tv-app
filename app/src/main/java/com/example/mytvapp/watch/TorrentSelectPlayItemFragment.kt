package com.example.mytvapp.watch

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.leanback.widget.GuidedAction
import androidx.leanback.widget.GuidedActionsStylist
import com.airbnb.lottie.LottieAnimationView
import com.example.mytvapp.DetailsActivity
import com.example.mytvapp.PlaybackActivity
import com.example.mytvapp.R

class TorrentSelectPlayItemFragment : TorrentSelectBaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println(":: PLAY TORRENTS ITEMS ::")
        println("ITEMS => ${mSearchTorrentsResult?.listTorrent.size}")
    }


    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {

        mSearchTorrentsResult.listTorrent.forEachIndexed { index, torrent ->

            actions.add(
                buildItemAction(torrent, index)
            )
        }
    }

    override fun onGuidedActionClicked(action: GuidedAction?) {

        val torrent = mSearchTorrentsResult.listTorrent.find { x -> x.id == action?.id }

        val intent = Intent(requireContext(), PlaybackActivity::class.java)

        mSelectedMovie.videoUrl = torrent?.magnet
        intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie)

        startActivity(intent)

        //requireActivity().finish()
    }


    private fun buildItemAction(torrent: Torrent, index: Int): GuidedAction {

        // Title
        val title = if (torrent.language.isNotBlank())
            torrent.language
        else
            "Opción $index"

        // Description
        val description =
            "${torrent.site.name}  •  ${torrent.quality}  •  ${torrent.size}  •  ${torrent.downloads}"


        return GuidedAction.Builder(requireActivity())
            .id(torrent.id)
            .editable(false)
            .title(title)
            .description(description)
            .build()
    }
}