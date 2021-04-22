package com.example.mytvapp.provider

import com.example.mytvapp.extension.Util.isHttpUrl
import com.example.mytvapp.extension.Util.isMagnetUrl
import com.example.mytvapp.watch.Torrent
import com.example.mytvapp.watch.TorrentProvider
import com.example.mytvapp.watch.TorrentSite
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*

open class BaseTorrentLatinoProvider(override val site: TorrentSite) : TorrentProvider(site) {


    override fun startSearchTorrentInSite() {

        println(":: SEARCH [$query] ::")

        val document = Jsoup.connect(site.url)
            .data("buscar", query)
            .get()

        document?.let {

            println(":: RESPONSE HTML ::")
            println(it.title())

            it.searchCatalogInDocument()
        }

        successCallback.invoke(result)
    }

    private fun Document.searchCatalogInDocument() {

        val elements = this.select(CssQuery.CatalogItem.query)

        if (elements.isEmpty().not()) {

            println(":: FOUND [${elements?.size}] ITEMS ::")
            elements?.searchItemInCatalogElements()
        }
    }

    private fun Elements.searchItemInCatalogElements() {

        this.forEachIndexed { index, element ->

            println("ITEM ${index.plus(1)}")

            val title = element?.selectFirst(CssQuery.CatalogItemTitle.query)

            val titleLink = title?.selectFirst(CssQuery.CatalogItemTitleLink.query)

            val linkText = titleLink?.text()?.trim()?.toLowerCase(Locale.ROOT)!!

            // TODO: check must be inherit method
            val isMovieMatched = (linkText == query).or(
                query.split(' ').any { word ->
                    linkText.contains(
                        word
                    )
                })

            if (isMovieMatched) {

                val url = titleLink.attr("href")!!

                println(linkText)

                if (url.isHttpUrl())
                    searchTorrentsInItemDocument(url)

                return // break => item found
            }
        }
    }


    private fun searchTorrentsInItemDocument(itemUrl: String) {

        val document = Jsoup.connect(itemUrl).get()

        document?.let document@{ docHtml ->

            val resultTable = docHtml.selectFirst(CssQuery.ItemTorrentsTable.query)

            resultTable?.let table@{

                val resultTableRows = resultTable.select(CssQuery.ItemTorrentsTableRows.query)

                val title = docHtml.selectFirst(CssQuery.ItemDetailsTitle.query)?.text()

                resultTableRows?.searchTorrentsInItemElements(
                    torrentTitle = title!!
                )
            }
        }
    }

    private fun Elements.searchTorrentsInItemElements(torrentTitle: String) {

        val title = torrentTitle.trim().toLowerCase(Locale.ROOT)

        this.forEachIndexed { index, row ->

            println("TORRENT [${index.plus(1)}]")
            println(row?.text())

            val columnLink = row?.selectFirst(CssQuery.ItemTorrentsDownloadLink.query)

            val tokenUrl = columnLink?.attr("href")!!

            // Search | Set torrent magnet
            val magnet = when {
                tokenUrl.isMagnetUrl() -> {
                    //mSelectedMovie?.videoUrl = tokenUrl
                    println("<TORRENT MAGNET> FOUND => $tokenUrl")
                    tokenUrl
                }

                tokenUrl.isHttpUrl() -> {
                    searchMagnetInItemTorrentDocument(
                        tokenUrl = tokenUrl
                    )
                }
                else -> ""
            }

            // Create & Add torrent in result list
            if (magnet.isNotBlank())
                row?.createTorrentItemFromElementInResult(
                    title = title,
                    magnet = magnet
                )
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun searchMagnetInItemTorrentDocument(tokenUrl: String): String {

        val tokenUrl = tokenUrl.replace(
            PATTERN_URL_TOKEN_SEARCH,
            PATTERN_URL_TOKEN_REPLACE
        )

        val document = Jsoup.connect(tokenUrl).get()

        document?.let { it ->

            val resultLink = it.body()?.selectFirst(CssQuery.ItemTokenUrl.query)

            resultLink?.attr("href")?.let { url ->
                println("TORRENT MAGNET => $url")
                return if (url.isMagnetUrl()) url else ""
            }
        }

        return ""
    }


    private fun Element.createTorrentItemFromElementInResult(title: String, magnet: String) {

        val rowColumns = this.select(CssQuery.ItemTorrentsRowColumns.query)

        val tdQuality = rowColumns?.elementAt(0)?.text() ?: ""
        val tdLanguage = rowColumns?.elementAt(1)?.text() ?: ""
        val tdSize = rowColumns?.elementAt(3)?.text() ?: ""
        val tdDownloads = rowColumns?.elementAt(4)?.text() ?: "0"

        val itemTorrent = Torrent(
            site = this@BaseTorrentLatinoProvider.site,
            title = title,
            magnet = magnet,
            quality = tdQuality,
            language = tdLanguage,
            size = tdSize,
            downloads = tdDownloads.toInt()
        )

        this@BaseTorrentLatinoProvider.result.add(itemTorrent)
    }


    protected enum class CssQuery(val query: String) {

        CatalogItem("div.catalog div.card"),
        CatalogItemTitle(".card__title"),
        CatalogItemTitleLink("a"),
        ItemTorrentsTable(".content.torrents > .container table"),
        ItemTorrentsTableRows("tbody > tr"),
        ItemDetailsTitle(".section.details .details__title"),
        ItemTorrentsDownloadLink("td > a.dwnld"),
        ItemTorrentsRowColumns("> td"),
        ItemTokenUrl("div > a"),
    }

    companion object {

        const val PATTERN_URL_TOKEN_SEARCH = "#"
        const val PATTERN_URL_TOKEN_REPLACE = "?k="
    }
}