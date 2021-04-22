package com.example.mytvapp

import android.net.Uri
import android.os.*
import android.view.View
import android.widget.Toast
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.Torrent
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.ui.SubtitleView
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.video.VideoListener
import java.util.*
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

/** Handles video playback with media controls. */
class PlaybackVideoFragment : VideoSupportFragment() {

    private lateinit var mTransportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
    private lateinit var mPlayerGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>

    private lateinit var mVideo: Movie

    private lateinit var mSimplePlayer: SimpleExoPlayer
    private lateinit var mTrackSelector: DefaultTrackSelector
    private lateinit var mSubtitleView: SubtitleView

    private lateinit var mTorrentStream: TorrentStream
    private lateinit var mTorrentListener: TorrentListener
    private var isPlayTorrent = false
    private var torrentProgress = 0.5f

    @ExperimentalTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {

            val intent = requireActivity().intent
            mVideo = intent?.getSerializableExtra(DetailsActivity.MOVIE) as Movie

            //setupDefaultPlayer()

        } catch (ex: Exception) {

            println(":: ERROR ::")
            ex.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {

            setupExoPlayer()

            // runDownloadTestTorrent()

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

        mSubtitleView = requireActivity().findViewById(R.id.playback_subtitles)

        mTrackSelector = DefaultTrackSelector(requireContext())

        mSimplePlayer = SimpleExoPlayer.Builder(requireActivity())
            .setTrackSelector(mTrackSelector)
            .build()

        // TODO: add subtitle view
        mSimplePlayer.textComponent?.let {

            it.addTextOutput(mSubtitleView)
        }

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


        mVideo.videoUrl?.let {

            mSimplePlayer.addVideoListener(object : VideoListener {
                override fun onVideoSizeChanged(
                    width: Int,
                    height: Int,
                    unappliedRotationDegrees: Int,
                    pixelWidthHeightRatio: Float
                ) {
                    println(":: VIDEO SIZE CHANGED :: ($width x $height)")

                    val rootView = this@PlaybackVideoFragment.view
                    val rootViewWidth = rootView?.width!!
                    val rootViewHeight = rootView.height

                    val surfaceView = this@PlaybackVideoFragment.surfaceView
                    val svLayoutParams = surfaceView.layoutParams

                    svLayoutParams.width = rootViewWidth
                    svLayoutParams.height = rootViewHeight
                    surfaceView.requestLayout()

                    // TODO: validate scale
                    var scaleX = 1f
                    var scaleY = 1f

                    val divViewWidthAndVideoWidth = (rootViewWidth / width.toFloat())
                    val divViewHeightAndVideoHeight = (rootViewHeight / height.toFloat())

                    when {
                        (width > rootViewWidth).and(height > rootViewHeight) -> {
                            scaleX = (width.toFloat() / rootViewWidth)
                            scaleY = (height.toFloat() / rootViewHeight)
                        }

                        (width < rootViewWidth).and(height < rootViewHeight) -> {
                            scaleX = divViewWidthAndVideoWidth
                            scaleY = divViewHeightAndVideoHeight
                        }

                        (rootViewWidth > width) -> {
                            scaleX = divViewWidthAndVideoWidth / divViewHeightAndVideoHeight
                        }

                        (rootViewHeight > height) -> {
                            scaleY = divViewHeightAndVideoHeight / divViewWidthAndVideoWidth
                        }
                    }


                    // surfaceView.scaleX = scaleX
                    // surfaceView.scaleY = scaleY - 0.2f
                    // surfaceView.pivotY = (height / 2f)
                    // surfaceView.pivotX = (width / 2f)

                    println(":: ROOT VIEW :: (${rootView.width} x ${rootView.height})")
                    println(":: SCALE :: ($scaleX / $scaleY)")
                }
            })

            // mSimplePlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

//            val subtitle = MediaItem.Subtitle(
//                Uri.parse(testUrlSubtitle),
//                MimeTypes.APPLICATION_SUBRIP,
//                "en",
//                C.SELECTION_FLAG_DEFAULT
//            )

//            val test = MediaItem.Builder()
//                .setUri(url)
//                .setSubtitles(listOf(subtitle))
//                .build()

            //mSimplePlayer.addMediaItem(MediaItem.fromUri(url))
            //mSimplePlayer.addMediaItem(test)

            runDownloadTestTorrent()
        }

        mSimplePlayer.addListener(object : Player.EventListener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)

                if (state == Player.STATE_READY) {
                    println(":: STATE READY ::")

//                    if (isPlayTorrent)
//                        trackSelector(
//                            C.TRACK_TYPE_AUDIO,
//                            trackSelection = 1,
//                            msgOn = 0,
//                            msgOff = 0,
//                            disable = true,
//                            doChange = false
//                        )
                }
            }
        })
    }

    private fun runDownloadTestTorrent() {

        val location = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        val torrentOptions = TorrentOptions.Builder()
            .saveLocation(location)
            .removeFilesAfterStop(true)
            .autoDownload(true)
            .build()

        mTorrentStream = TorrentStream.init(torrentOptions)

        mTorrentListener = object : TorrentListener {
            override fun onStreamPrepared(torrent: Torrent?) {
                println(":: TEST => TORRENT PREPARED ::")
                showToast(":: TORRENT PREPARED ::")
            }

            override fun onStreamStarted(torrent: Torrent?) {
                println(":: TEST => TORRENT STARTED ::")
                showToast(":: TORRENT STARTED ::")
            }

            override fun onStreamError(torrent: Torrent?, e: java.lang.Exception?) {
                println(":: TEST => TORRENT ERROR ::")
                showToast(":: TORRENT ERROR ::")
            }

            override fun onStreamReady(torrent: Torrent?) {
                println(":: TEST => TORRENT READY ::")
            }

            override fun onStreamProgress(torrent: Torrent?, status: StreamStatus?) {
                println(":: TEST => TORRENT PROGRESS ::")
                println(
                    "TEST => " +
                            "speed: ${status?.downloadSpeed} | " +
                            "progress: ${status?.progress} | " +
                            "buffer: ${status?.bufferProgress}"
                )

                if(torrentProgress == 0.5f){
                    showToast(":: TORRENT DOWNLOADING ::")
                }

                val progress = status?.progress!!

                if (progress >= torrentProgress && isPlayTorrent) {
                    torrentProgress = progress + 5f

                    val percentProgress = String.format(Locale.US, "%.1f", progress)

                    showToast("PROGRESS [$percentProgress%]")
                }


                if (progress > 1.5 && isPlayTorrent.not()) {
                    showToast("PLAY TORRENT")

                    val videoPath = torrent?.videoFile?.path!!

                    val torrentMediaItem = MediaItem.fromUri(videoPath)

                    // TODO: add subtitle

                    val subtitle = MediaItem.Subtitle(
                        Uri.parse(testUrlSubtitle),
                        MimeTypes.APPLICATION_SUBRIP,
                        "en",
                        C.SELECTION_FLAG_DEFAULT
                    )

                    val test = MediaItem.Builder()
                        .setUri(videoPath)
                        //.setSubtitles(listOf(subtitle))
                        .build()


                    //mPlayerGlue.pause()
                    //mSimplePlayer.clearMediaItems()
                    mSimplePlayer.addMediaItem(test)

                    mPlayerGlue.playWhenPrepared()
                    mSimplePlayer.prepare()

                    isPlayTorrent = true
                }
            }

            override fun onStreamStopped() {
                println(":: TEST => TORRENT STOPPED ::")
            }

        }

        mTorrentStream.addListener(mTorrentListener)

        mTorrentStream.startStream(mVideo.videoUrl)

        println("TORRENT => ${mVideo.videoUrl}")

        showToast(":: WAIT START TORRENT ::")
    }


    // trackSelection = current selection. -1 = disabled, -2 = leave as is
    // disable = true : Include disabled in the rotation
    // doChange = true : select a new track, false = leave same track
    // Return = new track selection.
    fun trackSelector(
        trackType: Int, trackSelection: Int,
        msgOn: Int, msgOff: Int, disable: Boolean, doChange: Boolean
    ): Int {
        // optionList array - 0 = renderer, 1 = track group, 2 = track
        var trackSelection = trackSelection
        val optionList = ArrayList<IntArray>()
        val renderList = ArrayList<Int>()
        val formatList = ArrayList<Format>()

        val mti = mTrackSelector.currentMappedTrackInfo
        val isPlaying = mPlayerGlue.isPlaying

        if (mti == null) return -1

        for (rendIx in 0 until mti.rendererCount) {

            if (mti.getRendererType(rendIx) == trackType) {

                renderList.add(rendIx)

                val tga = mti.getTrackGroups(rendIx)

                for (tgIx in 0 until tga.length) {

                    val tg = tga[tgIx]

                    for (trkIx in 0 until tg.length) {

                        val selection = IntArray(3)
                        // optionList array - 0 = renderer, 1 = track group, 2 = track
                        selection[0] = rendIx
                        selection[1] = tgIx
                        selection[2] = trkIx

                        optionList.add(selection)
                        formatList.add(tg.getFormat(trkIx))
                    }
                }

                break
            }
        }

        val msg = StringBuilder()

        if (doChange) {

            when {
                (trackSelection == -2) -> trackSelection = -1

                (optionList.size == 0) -> trackSelection = -1

                (++trackSelection >= optionList.size) ->
                    trackSelection = if (disable) -1 else 0
            }
        }

        when {
            (trackSelection >= 0) -> {

                val selection = optionList[trackSelection]
                val override = SelectionOverride(
                    selection[1],
                    selection[2]
                )
                val tga = mti.getTrackGroups(selection[0])

                var params = mTrackSelector
                    .buildUponParameters()
                    .setSelectionOverride(selection[0], tga, override)

                if (disable) params = params.setRendererDisabled(selection[0], false)

                // This line causes playback to pause when enabling subtitle
                mTrackSelector.setParameters(params)

                var language = formatList[trackSelection].language

                language = if (language?.isBlank()!!) String() else {
                    val locale = Locale(language)
                    val langDesc = locale.displayLanguage
                    "($langDesc)"
                }

                if (msgOn > 0) msg.append(
                    activity!!.getString(
                        msgOn,
                        trackSelection + 1, language
                    )
                )
            }

            (trackSelection == -1) -> {

                if (optionList.size > 0) {

                    for (ix in renderList.indices) {
                        mTrackSelector.setParameters(
                            mTrackSelector
                                .buildUponParameters()
                                .setRendererDisabled(renderList[ix], true)
                        )
                    }
                }

                if (msgOff > 0) msg.append(activity!!.getString(msgOff))
            }
        }

        if (msg.isNotEmpty()) {
            showToast(msg.toString())
        }

        // For some reason changing the subtitle pauses playback. This fixes that.
        if (trackType == C.TRACK_TYPE_TEXT && isPlaying) {

            mPlayerGlue.pause()
            mPlayerGlue.play()
        }

        return trackSelection
    }


    fun showToast(message: String) {

        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

            if (mTorrentStream.isStreaming) {

                mTorrentStream.stopStream()
                mTorrentStream.removeListener(mTorrentListener)
            }

            mPlayerGlue.pause()

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {
        private const val UPDATE_DELAY = 16

        // http://145.239.255.77/gtsubtitle/127%20Hours/English.srt
        // https://mkvtoolnix.download/samples/vsshort-en.srt
        private const val testUrlSubtitle =
            "http://145.239.255.77/gtsubtitle/127%20Hours/English.srt"


        private const val testUrlTorrent =
            "magnet:?xt=urn:btih:2BD98D4B35E062C617DF6CD9F08756B019A9373E&dn=Chaos.Walking.2021.lati.mp4&tr=udp%3a%2f%2ftracker.openbittorrent.com%3a80%2fannounce&tr=udp%3a%2f%2ftracker.opentrackr.org%3a1337%2fannounce"
    }
}