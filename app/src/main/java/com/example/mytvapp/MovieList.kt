package com.example.mytvapp

object MovieList {
    val MOVIE_CATEGORY = arrayOf(
        "Category Zero",
        "Category One",
        "Category Two",
        "Category Three",
        "Category Four",
        "Category Five"
    )

    val list: List<Movie> by lazy {
        setupMovies()
    }
    private var count: Long = 0

    private fun setupMovies(): List<Movie> {
        val title = arrayOf(
            "Zeitgeist 2010_ Year in Review",
            "Google Demo Slam_ 20ft Search",
            "Introducing Gmail Blue",
            "Introducing Google Fiber to the Pole",
            "Introducing Google Nose"
        )

        val description = "Fusce id nisi turpis. Praesent viverra bibendum semper. " +
                "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est " +
                "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit " +
                "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit " +
                "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id " +
                "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat."
        val studio = arrayOf(
            "Studio Zero",
            "Studio One",
            "Studio Two",
            "Studio Three",
            "Studio Four"
        )
        val videoUrl = arrayOf(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        )
        val bgImageUrl = arrayOf(
            "https://source.unsplash.com/cUpp1gAEtiU/1920x1080",
            "https://source.unsplash.com/SXTj90G1f5c/1920x1080",
            "https://source.unsplash.com/mP8nkggCTlA/1920x1080",
            "https://source.unsplash.com/s6aa3O-iyYE/1920x1080",
            "https://source.unsplash.com/3odiQvgDl4s/1920x1080",
        )
        val cardImageUrl = arrayOf(
            "https://source.unsplash.com/cUpp1gAEtiU/400x300",
            "https://source.unsplash.com/SXTj90G1f5c/400x300",
            "https://source.unsplash.com/mP8nkggCTlA/400x300",
            "https://source.unsplash.com/s6aa3O-iyYE/400x300",
            "https://source.unsplash.com/3odiQvgDl4s/400x300",
        )

        val list = title.indices.map {
            buildMovieInfo(
                title[it],
                description,
                studio[it],
                videoUrl[it],
                cardImageUrl[it],
                bgImageUrl[it]
            )
        }

        return list
    }

    private fun buildMovieInfo(
        title: String,
        description: String,
        studio: String,
        videoUrl: String,
        cardImageUrl: String,
        backgroundImageUrl: String
    ): Movie {
        val movie = Movie()
        movie.id = count++
        movie.title = title
        movie.description = description
        movie.studio = studio
        movie.cardImageUrl = cardImageUrl
        movie.backgroundImageUrl = backgroundImageUrl
        movie.videoUrl = videoUrl
        return movie
    }
}