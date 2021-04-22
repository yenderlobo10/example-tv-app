package com.example.mytvapp.watch

import com.example.mytvapp.provider.PelisPandaProvider
import com.example.mytvapp.provider.SitorrentProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.Serializable
import java.util.*

enum class TorrentSite(val url: String) {

    Sitorrent("https://sitorrent.co/buscar/"),

    PelisPanda("https://pelispanda.com/buscar/"),
}

open class TorrentProvider(open val site: TorrentSite) {

    protected lateinit var query: String
    protected var result = mutableListOf<Torrent>()
    protected lateinit var successCallback: (List<Torrent>) -> Unit
    protected lateinit var errorCallback: (Exception) -> Unit


    /**
     * TODO: java doc
     */
    fun launchSearch(query: String): TorrentProvider {
        try {

            this.result.clear()

            this.query = query.trim().toLowerCase(Locale.ROOT)

            startSearchCoroutine()

        } catch (ex: Exception) {
            errorCallback.invoke(ex)
        }

        return this
    }

    /**
     * TODO: java doc
     */
    fun resultSuccess(callback: (List<Torrent>) -> Unit): TorrentProvider {
        successCallback = callback
        return this
    }

    /**
     * TODO: java doc
     */
    fun resultError(callback: (Exception) -> Unit): TorrentProvider {
        errorCallback = callback
        return this
    }

    /**
     * Implement logic to search torrents in [site].
     *
     * -- Note --
     *
     * This method RUN in a COROUTINE inside [startSearchCoroutine] method.
     */
    @Throws(IOException::class)
    protected open fun startSearchTorrentInSite() {
        // implement logic search torrents in site.
    }


    private fun startSearchCoroutine() = GlobalScope.launch(Dispatchers.IO) {

        try {

            startSearchTorrentInSite()

        } catch (ex: Exception) {
            errorCallback.invoke(ex)
        }
    }


    companion object {

        /**
         * Default list of torrent providers.
         * +++ Add more providers if needed +++.
         */
        val listDefaultProviders: List<TorrentProvider> =
            listOf(
                SitorrentProvider(),
                PelisPandaProvider(),
                /// add more providers here ...
            )
    }
}

class TorrentProviderSearcher(
    private val listProviders: List<TorrentProvider>
) {

    private var successCount = 0
    private var errorCount = 0
    private var resultListTorrents = mutableListOf<Torrent>()
    private lateinit var completedCallback: (Result) -> Unit

    private val isSearchCompleted: Boolean
        get() {
            return (successCount + errorCount) == listProviders.size
        }


    /**
     * TODO: java doc
     */
    fun startSearch(query: String): TorrentProviderSearcher {

        listProviders.forEach { provider ->

            provider
                .launchSearch(query)
                .resultSuccess {

                    it.forEach { torrent ->

                        // Check if not has been added another same magnet
                        val isSameMagnetAdded = resultListTorrents.any { x ->
                            x.magnet == torrent.magnet
                        }

                        // Add only unique magnet to list torrents result
                        if (isSameMagnetAdded.not())
                            resultListTorrents.add(torrent)
                    }

                    successCount++
                    emmitSearchCompletedIfRequired()
                }
                .resultError {

                    errorCount++
                    emmitSearchCompletedIfRequired()

                    println(":: ERROR ::")
                    it.printStackTrace()
                }
        }

        return this
    }

    /**
     * TODO: java doc
     */
    fun onSearchCompleted(callback: (Result) -> Unit): TorrentProviderSearcher {
        completedCallback = callback
        return this
    }


    private fun emmitSearchCompletedIfRequired() {

        if (isSearchCompleted) {

            completedCallback.invoke(
                Result(
                    listTorrent = resultListTorrents,
                    hasError = (errorCount > 0)
                )
            )
        }
    }


    companion object {

        fun create() = TorrentProviderSearcher(
            listProviders = TorrentProvider.listDefaultProviders
        )
    }


    data class Result(
        val listTorrent: List<Torrent>,
        val hasError: Boolean,

        val notFound: Boolean = listTorrent.isEmpty()
    ) : Serializable {

        companion object {
            internal const val serialVersionUID = 3393516042021L
        }
    }
}