package com.example.mytvapp

import android.net.Uri
import android.os.*
import android.widget.Toast
import androidx.core.net.toFile
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow
import androidx.lifecycle.lifecycleScope
import com.frostwire.jlibtorrent.TorrentHandle
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.database.DefaultDatabaseProvider
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.video.VideoListener
import com.masterwok.simpletorrentandroid.TorrentSession
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.contracts.TorrentSessionListener
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

/** Handles video playback with media controls. */
class PlaybackVideoFragment : VideoSupportFragment() {

    private lateinit var mTransportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
    private lateinit var mPlayerGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>

    private lateinit var mVideo: Movie

    private lateinit var mSimplePlayer: SimpleExoPlayer

    @ExperimentalTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)

        try {

            val intent = requireActivity().intent
            mVideo = intent?.getSerializableExtra(DetailsActivity.MOVIE) as Movie

            //setupDefaultPlayer()

            setupExoPlayer()

            runDownloadTestTorrent()

        } catch (ex: Exception) {

            println(":: ERROR ::")
            ex.printStackTrace()
        }
    }

    private fun setupDefaultPlayer() {

        val glueHost = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)
        val playerAdapter = MediaPlayerAdapter(context)
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

        mTransportControlGlue = PlaybackTransportControlGlue(requireActivity(), playerAdapter)
        mTransportControlGlue.apply {
            host = glueHost
            title = mVideo.title
            subtitle = mVideo.description

            isSeekEnabled = true

            playWhenPrepared()

            addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
                override fun onPreparedStateChanged(glue: PlaybackGlue?) {
                    super.onPreparedStateChanged(glue)

                    println(":: VIDEO STATE CHANGE ::")
                    glue?.let {
                        if (isPrepared) {
                            val durationInMS = playerAdapter.duration
                            val durInSeconds = (durationInMS / 1000).toDouble().roundToInt()
                            val durInMinutes = (durInSeconds / 60).toDouble()
                            val minutes = durInMinutes.roundToInt()
                            val seconds = (durInSeconds % 60)
                            Toast.makeText(
                                requireContext(),
                                "Duration $minutes:$seconds",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
            })
        }

        playerAdapter.setDataSource(Uri.parse(mVideo.videoUrl))
    }

    private fun setupExoPlayer() {

        val glueHost = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)

        mSimplePlayer = SimpleExoPlayer.Builder(requireActivity()).build()

        val playerAdapter = LeanbackPlayerAdapter(requireActivity(), mSimplePlayer, UPDATE_DELAY)
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

        mPlayerGlue = PlaybackTransportControlGlue(requireActivity(), playerAdapter)

        mPlayerGlue.apply {

            host = glueHost

            title = mVideo.title
            subtitle = mVideo.description

            isSeekEnabled = true

            playWhenPrepared()
        }


        mVideo.videoUrl?.let { url ->

            mSimplePlayer.addVideoListener(object : VideoListener {
                override fun onVideoSizeChanged(
                    width: Int,
                    height: Int,
                    unappliedRotationDegrees: Int,
                    pixelWidthHeightRatio: Float
                ) {
                    println(":: VIDEO SIZE CHANGED :: ($width x $height)")

                    val rootView = this@PlaybackVideoFragment.view

                    val surfaceView = this@PlaybackVideoFragment.surfaceView
                    val svLayoutParams = surfaceView.layoutParams

                    svLayoutParams.width = rootView?.width!!
                    svLayoutParams.height = rootView.height
                    surfaceView.requestLayout()

                    println(":: ROOT VIEW :: (${rootView.width} x ${rootView.height})")

                }
            })

            mSimplePlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

            mSimplePlayer.addMediaItem(MediaItem.fromUri(url))

            mSimplePlayer.prepare()

            mSimplePlayer.addListener(object : Player.EventListener {

            })
        }

        mSimplePlayer.addListener(object : Player.EventListener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)

                if (state == Player.STATE_READY) {
                    println(":: STATE READY ::")
                }
            }
        })
    }

    private fun runDownloadTestTorrent() {

        val test = Handler(Looper.getMainLooper())

        val torrentUrl = Uri.parse(testUrlTorrent)
        val location = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        val torrentSessionOptions = TorrentSessionOptions(
            downloadLocation = location!!,
            enableLogging = false,
            shouldStream = true,
            onlyDownloadLargestFile = true,
        )

        val torrentSession = TorrentSession(torrentSessionOptions)

        torrentSession.listener = object : TorrentSessionListener {
            override fun onAddTorrent(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: ADD TORRENT ::")
                //EventBus.getDefault().post(torrentSessionStatus)
            }

            override fun onBlockUploaded(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT BLOCK UPLOADED ::")
            }

            override fun onMetadataFailed(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT FAILED METADATA ::")
            }

            override fun onMetadataReceived(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT RECEIVED METADATA ::")
            }

            override fun onPieceFinished(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT PIECE FINISHED ::")

                println("DOWN RATE => ${torrentSessionStatus.downloadRate}")
                println("BYTES DOWNLOADED => ${torrentSessionStatus.bytesDownloaded}")
                println("BYTES WANTED => ${torrentSessionStatus.bytesWanted}")

                //EventBus.getDefault().post(torrentSessionStatus)
            }

            override fun onTorrentDeleteFailed(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT DELETE FAILED ::")
            }

            override fun onTorrentDeleted(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT DELETED ::")
            }

            override fun onTorrentError(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT ERROR ::")
            }

            override fun onTorrentFinished(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT FINISHED ::")
                //EventBus.getDefault().post(torrentSessionStatus)
            }

            override fun onTorrentPaused(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT PAUSED ::")
            }

            override fun onTorrentRemoved(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT REMOVED ::")
            }

            override fun onTorrentResumed(
                torrentHandle: TorrentHandle,
                torrentSessionStatus: TorrentSessionStatus
            ) {
                println(":: TORRENT RESUMED ::")
                EventBus.getDefault().post(torrentSessionStatus)
            }
        }

        //mSimplePlayer.setThrowsWhenUsingWrongThread(false)

        AsyncTask.execute {

            torrentSession.start(requireContext(), torrentUrl)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onAddTorrent(status: TorrentSessionStatus) {

        try {

            println(":: POST - TORRENT EVENT ::")

            println("STATUS => ${status.state.name}")
            println("VIDEO => ${status.videoFileUri}")

            //mSimplePlayer.clearMediaItems()
            //mSimplePlayer.removeMediaItem(0)
            val torrentMediaItem = MediaItem.fromUri(status.videoFileUri)

            val cache = CacheDataSource.Factory().apply {
                
            }

            mSimplePlayer.addMediaItem(torrentMediaItem)

            mSimplePlayer.playWhenReady = true

            mSimplePlayer.next()

        } catch (ex: Exception) {

            println(":: ERROR - TORRENT ADD ::")
            ex.printStackTrace()
        }
    }

    @Suppress("SimpleRedundantLet", "UNNECESSARY_SAFE_CALL")
    override fun onPause() {
        super.onPause()

        try {
            mTransportControlGlue.pause()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        try {
            mPlayerGlue.pause()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {
        private const val UPDATE_DELAY = 16

        private const val testUrlTorrent =
            "https://webtorrent.io/torrents/tears-of-steel.torrent"
    }
}