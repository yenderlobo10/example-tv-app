package com.example.mytvapp.watch

import com.example.mytvapp.extension.Util
import java.io.Serializable

data class Torrent(
    /**
     * Auto generate GUID.
     */
    val id: Long,
    /**
     * Site of torrent, one of [TorrentSite].
     */
    val site: TorrentSite,
    val title: String,
    val magnet: String,
    val quality: String,
    val language: String,
    val size: String,
    val downloads: Int
) : Serializable {
    constructor(
        site: TorrentSite,
        title: String,
        magnet: String,
        quality: String,
        language: String,
        size: String,
        downloads: Int
    ) : this(id = Util.genRandomId(), site, title, magnet, quality, language, size, downloads)

    constructor(
        site: TorrentSite,
        magnet: String,
        quality: String,
        language: String,
        size: String,
        downloads: Int
    ) : this(
        id = Util.genRandomId(),
        site,
        title = "Torrent",
        magnet,
        quality,
        language,
        size,
        downloads
    )

    constructor(
        site: TorrentSite,
        magnet: String,
        quality: String,
        size: String,
    ) : this(
        id = Util.genRandomId(),
        site,
        title = "Torrent",
        magnet,
        quality,
        language = "Unknown",
        size,
        downloads = 0
    )

    constructor(
        site: TorrentSite,
        magnet: String
    ) : this(
        id = Util.genRandomId(),
        site,
        title = "Torrent",
        magnet,
        quality = "",
        language = "Unknown",
        size = "",
        downloads = 0
    )


    companion object {
        internal const val serialVersionUID = 4040316042021L
    }
}