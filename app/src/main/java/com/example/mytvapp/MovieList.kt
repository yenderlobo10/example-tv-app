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
            "Godzilla vs Kong",
            "La Liga de la Justicia de Zack Snyder",
            "Caos: El Inicio",
            "Raya y el último dragón",
            "Thor: Ragnarok"
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
            "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jMWkd0fuwbG39eJpzycJzPWMCww.jpg",
            "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/pcDc2WJAYGJTTvRSEIpRZwM3Ola.jpg",
            "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/xUTzd0s4oOQz9MuxZMkfkOHRTkP.jpg",
            "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/7prYzufdIOy1KCTZKVWpjBFqqNr.jpg",
            "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/kaIfm5ryEOwYg8mLbq8HkPuM1Fo.jpg",
        )
        val cardImageUrl = arrayOf(
            "https://www.themoviedb.org/t/p/w600_and_h900_bestv2/bnuC6hu7AB5dYW26A3o6NNLlIlE.jpg",
            "https://www.themoviedb.org/t/p/w220_and_h330_face/rkuvJnamPl3xW9wKJsIS6qkmOCW.jpg",
            "https://www.themoviedb.org/t/p/w220_and_h330_face/idQDWn8Yhl4zXLwpyHKlr3NXYO9.jpg",
            "https://www.themoviedb.org/t/p/w220_and_h330_face/yHpNgjEXzZ557YiZ2r3VrKid788.jpg",
            "https://www.themoviedb.org/t/p/w220_and_h330_face/6VFzRo4lKsEy5jlcRREctOWR2IC.jpg",
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