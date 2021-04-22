package com.example.mytvapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.mytvapp.extension.Util.fromDpToPx
import com.example.mytvapp.extension.Util.isMagnetUrl
import com.example.mytvapp.watch.LoaderDialogFragment
import com.example.mytvapp.watch.TorrentProviderSearcher
import com.example.mytvapp.watch.TorrentSelectActivity
import org.jsoup.Jsoup
import java.io.IOException
import kotlin.math.roundToInt

/**
 * A wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its metadata plus related videos.
 */
class VideoDetailsFragment : DetailsSupportFragment() {

    private var mSelectedMovie: Movie? = null

    private lateinit var mDetailsBackground: DetailsSupportFragmentBackgroundController
    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate DetailsFragment")
        super.onCreate(savedInstanceState)

        mDetailsBackground = DetailsSupportFragmentBackgroundController(this)

        mSelectedMovie =
            requireActivity().intent.getSerializableExtra(DetailsActivity.MOVIE) as Movie
        if (mSelectedMovie != null) {
            mPresenterSelector = ClassPresenterSelector()
            mAdapter = ArrayObjectAdapter(mPresenterSelector)
            setupDetailsOverviewRow()
            setupDetailsOverviewRowPresenter()
            setupRelatedMovieListRow()
            adapter = mAdapter
            initializeBackground(mSelectedMovie)
            onItemViewClickedListener = ItemViewClickedListener()
        } else {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
        }
    }


    private fun initializeBackground(movie: Movie?) {
        mDetailsBackground.enableParallax()
        Glide.with(requireContext())
            .asBitmap()
            .load(movie?.backgroundImageUrl)
            .centerCrop()
            .error(R.drawable.default_background)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    mDetailsBackground.coverBitmap = resource
                    mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not yet implemented
                }
            })
    }

    private fun setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedMovie?.toString())

        val row = DetailsOverviewRow(mSelectedMovie)

        row.isImageScaleUpAllowed = false
        row.imageDrawable = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.default_background
        )

        val width = DETAIL_THUMB_WIDTH.fromDpToPx(requireContext())
        val height = DETAIL_THUMB_HEIGHT.fromDpToPx(requireContext())

        Glide.with(requireContext())
            .load(mSelectedMovie?.cardImageUrl)
            .override(width, height)
            .transform(RoundedCorners(36))
            .error(R.drawable.default_background)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    Log.d(TAG, "details overview card image url ready: $resource")

                    val resource = resource.toBitmap(width, height)
                    row.imageDrawable = resource.toDrawable(resources)

                    mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not yet implemented
                }
            })

        val actionAdapter = ArrayObjectAdapter()

        actionAdapter.add(
            Action(
                ACTION_WATCH_TRAILER,
                resources.getString(R.string.watch_trailer_1)
            )
        )
        actionAdapter.add(
            Action(
                ACTION_RENT,
                resources.getString(R.string.rent_1),
                resources.getString(R.string.rent_2)
            )
        )
        actionAdapter.add(
            Action(
                ACTION_BUY,
                resources.getString(R.string.buy_1),
                resources.getString(R.string.buy_2)
            )
        )

        row.actionsAdapter = actionAdapter

        mAdapter.add(row)
    }

    private fun setupDetailsOverviewRowPresenter() {
        // Set detail background.
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        detailsPresenter.backgroundColor =
            ContextCompat.getColor(requireContext(), R.color.selected_background)

//        detailsPresenter.actionsBackgroundColor =
//            ContextCompat.getColor(requireContext(), R.color.fastlane_background)

        // Hook up transition element.
        val sharedElementHelper = FullWidthDetailsOverviewSharedElementHelper()
        sharedElementHelper.setSharedElementEnterTransition(
            activity, DetailsActivity.SHARED_ELEMENT_NAME
        )
        detailsPresenter.setListener(sharedElementHelper)
        detailsPresenter.isParticipatingEntranceTransition = true

        detailsPresenter.onActionClickedListener = OnActionClickedListener { action ->

            if (action.id == ACTION_WATCH_TRAILER) {

                println(":: SELECT TORRENT MOVIE ::")
                println(mSelectedMovie.toString())

                //val intent = Intent(context, TorrentSelectActivity::class.java)
                //intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie)
                //startActivity(intent)

                searchTorrents()

            } else {

                Toast.makeText(context, action.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
    }

    private fun setupRelatedMovieListRow() {
        val subcategories = arrayOf(getString(R.string.related_movies))
        val list = MovieList.list.toMutableList()

        list.shuffle()
        val listRowAdapter = ArrayObjectAdapter(CardPresenter())
        for (j in 0 until NUM_COLS) {
            listRowAdapter.add(list[j % 5])
        }

        val header = HeaderItem(0, subcategories[0])
        mAdapter.add(ListRow(header, listRowAdapter))
        mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
    }

    private fun convertDpToPixel(context: Context, dp: Int): Int {
        val density = context.applicationContext.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }


    private fun searchTorrents() {

        try {

            val loaderDialog =
                LoaderDialogFragment.create(activity = requireActivity()).show()

            TorrentProviderSearcher
                .create()
                .startSearch(mSelectedMovie?.title!!)
                .onSearchCompleted { result ->
                    println(":: COMPLETED  CALLBACK ::")
                    println(":: FOUND (${result.listTorrent.size}) TORRENTS ::")

                    loaderDialog.dismiss()

                    if (result.hasError && result.notFound)
                        return@onSearchCompleted


                    val intent = Intent(requireContext(), TorrentSelectActivity::class.java)
                    intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie)
                    intent.putExtra(DetailsActivity.SEARCH_TORRENTS_RESULT, result)

                    startActivity(intent)
                }

        } catch (ex: Exception) {

            println(":: ERROR ::")
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun runWebSearchTorrent() {

        val movieTitle = mSelectedMovie?.title?.toLowerCase()!!

        println(":: SEARCHING [$movieTitle] ::")

        val document = Jsoup.connect(URL_TORRENT_SEARCH)
            .data("buscar", movieTitle)
            .get()

        document?.let {

            println(":: RESPONSE HTML ::")
            println(it.title())

            val result = it.select("div.catalog div.card")

            if (result.isEmpty()) {
                println(":: NOT FOUND ITEMS ::")
                return@let
            }

            println(":: FOUND [${result?.size}] ITEMS ::")

            result?.forEachIndexed { index, element ->

                println("ITEM ${index.plus(1)}")

                val title = element?.selectFirst(".card__title")

                val titleLink = title?.selectFirst("a")

                val linkText = titleLink?.text()?.toLowerCase()!!

                val isMovieSearched = (linkText == movieTitle).or(
                    movieTitle.split(' ').any { x ->
                        linkText.contains(
                            x
                        )
                    })

                if (isMovieSearched) {

                    val url = titleLink.attr("href")!!

                    println(linkText)

                    searchWebItemTorrent(url)

                    return@let
                }
            }
        }
    }

    private fun searchWebItemTorrent(itemUrl: String) {

        val document = Jsoup.connect(itemUrl).get()

        document?.let document@{

            val resultTable = it.selectFirst(".content.torrents > .container table")

            resultTable?.let table@{

                val resultTableRows = resultTable.select("tbody > tr")

                resultTableRows?.forEachIndexed { index, row ->

                    println("TORRENT [${index.plus(1)}]")
                    println(row?.text())

                    val columnLink = row?.selectFirst("td > a.dwnld")

                    val tokenUrl = columnLink?.attr("href")!!

                    when {
                        tokenUrl.isMagnetUrl() -> {
                            mSelectedMovie?.videoUrl = tokenUrl
                            println("TORRENT MAGNET => $tokenUrl")
                        }

                        else -> {
                            searchTokenItemTorrent(tokenUrl)
                        }
                    }

                    // Break - get first torrent in table
                    return@document
                }
            }
        }
    }

    private fun searchTokenItemTorrent(tokenUrl: String) {

        val tokenUrl = tokenUrl.replace("#", "?k=")

        val document = Jsoup.connect(tokenUrl).get()

        document?.let { it ->

            val resultLink = it.body()?.selectFirst("div > a")

            resultLink?.attr("href")?.let { url ->
                mSelectedMovie?.videoUrl = url
                println("TORRENT MAGNET => $url")
            }
        }
    }


    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {
            if (item is Movie) {
                Log.d(TAG, "Item: $item")
                val intent = Intent(requireContext(), DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, item)

                val bundle =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        (itemViewHolder?.view as ImageCardView).mainImageView,
                        DetailsActivity.SHARED_ELEMENT_NAME
                    )
                        .toBundle()
                requireActivity().startActivity(intent, bundle)
            }
        }
    }

    companion object {
        private const val TAG = "VideoDetailsFragment"

        private const val ACTION_WATCH_TRAILER = 1L
        private const val ACTION_RENT = 2L
        private const val ACTION_BUY = 3L

        private const val DETAIL_THUMB_WIDTH = 300
        private const val DETAIL_THUMB_HEIGHT = 380

        private const val NUM_COLS = 10

        private const val URL_TORRENT_SEARCH = "https://sitorrent.co/buscar/"
    }
}