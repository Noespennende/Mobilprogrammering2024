package com.movielist.data

import java.util.Calendar

data class Movie(
    override val imdbID: String = "",
    override val title: String = "",
    override val description: String = "",
    override val genre: List<String> = emptyList(), //endret fra String
    override val releaseDate: Calendar = Calendar.getInstance(),
    override val actors: List<String> = emptyList(), // Listen skal ikke endres i appen - data kommer fra API
    override val rating: Int? = null,
    override val reviews: List<String> = emptyList(), // Så bruker kan se anmeldelsen sin *umiddelbart*
    override val posterUrl: Int = 0,
    override val type: String = "Movie",

    val lengthMinutes: Int? = null,
    val trailerUrl: String = ""
) : Production()
