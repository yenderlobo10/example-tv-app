package com.example.mytvapp

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.Torrent
import com.github.se_bastiaan.torrentstream.TorrentOptions
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CustomPlaybackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CustomPlaybackFragment : Fragment() {

    lateinit var playerView: StyledPlayerView
    lateinit var mPlayer: SimpleExoPlayer

    private var isPlayTorrent = false
    private var torrentProgress = 0.5f


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {

            playerView = requireActivity().findViewById(R.id.playerView)

            setupPlayer()

            runDownloadTestTorrent()

        } catch (ex: Exception) {

            println(":: ERROR ::")
            ex.printStackTrace()
        }
    }


    private fun setupPlayer() {

        mPlayer = SimpleExoPlayer.Builder(requireContext()).build()

        playerView.player = mPlayer

        val mediaItem =
            MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4")
        mPlayer.addMediaItem(mediaItem)

        mPlayer.playWhenReady = true
        mPlayer.prepare()
    }


    private fun runDownloadTestTorrent() {

        val location = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

        val torrentOptions = TorrentOptions.Builder()
            .saveLocation(location)
            .removeFilesAfterStop(true)
            .autoDownload(true)
            .build()

        val torrentStream = TorrentStream.init(torrentOptions)

        torrentStream.addListener(object : TorrentListener {
            override fun onStreamPrepared(torrent: Torrent?) {
                println(":: TEST => TORRENT PREPARED ::")
            }

            override fun onStreamStarted(torrent: Torrent?) {
                println(":: TEST => TORRENT STARTED ::")
            }

            override fun onStreamError(torrent: Torrent?, e: java.lang.Exception?) {
                println(":: TEST => TORRENT ERROR ::")
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

                val progress = status?.progress!!

                if (progress >= torrentProgress) {
                    torrentProgress = progress + 0.5f

                    val percentProgress = String.format(Locale.US, "%.1f", progress)

                    showToast("PROGRESS [$percentProgress%]")
                }


                if (progress > 3 && isPlayTorrent.not()) {
                    showToast("PLAY TORRENT")

                    val videoPath = torrent?.videoFile?.path!!

                    val torrentMediaItem = MediaItem.fromUri(videoPath)

                    // TODO: add subtitle


                    //mSimplePlayer.pause()
                    //mSimplePlayer.clearMediaItems()
                    mPlayer.addMediaItem(torrentMediaItem)

                    mPlayer.next()

                    isPlayTorrent = true
                }
            }

            override fun onStreamStopped() {
                println(":: TEST => TORRENT STOPPED ::")
            }

        })

        torrentStream.startStream(testUrlTorrent)
    }


    fun showToast(message: String) {

        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    override fun onPause() {
        super.onPause()

        try {

            mPlayer.pause()

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {

            mPlayer.release()

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    companion object {

        // http://145.239.255.77/gtsubtitle/127%20Hours/English.srt
        // https://mkvtoolnix.download/samples/vsshort-en.srt
        private const val testUrlSubtitle =
            "http://145.239.255.77/gtsubtitle/127%20Hours/English.srt"

        private const val testUrlTorrent =
            "magnet:?xt=urn:btih:6FDD39F084ED5B940EB200C782984359DAA4CA4C&dn=Justice.League.The.Snyder.Cut.2021.lati.mp4&tr=udp%3a%2f%2ftracker.leechers-paradise.org%3a6969%2fannounce&tr=udp%3a%2f%2ftracker.coppersurfer.tk%3a6969%2fannounce&tr=http%3a%2f%2fshare.camoe.cn%3a8080%2fannounce&tr=udp%3a%2f%2ftracker.pirateparty.gr%3a6969"
    }
}