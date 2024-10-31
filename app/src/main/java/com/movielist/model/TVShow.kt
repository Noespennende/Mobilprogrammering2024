package com.movielist.model

import com.movielist.R
import java.util.Calendar

data class TVShow(
    override val imdbID: String = "",
    override val title: String = "",
    override val description: String = "",
    override val genre: List<String> = emptyList(),
    override val releaseDate: Calendar = Calendar.getInstance(),
    override val actors: List<String> = emptyList(), // Listen skal ikke endres i appen - data kommer fra API
    override val rating: Int? = null,
    override val reviews: List<String> = emptyList(), // Så bruker kan se anmeldelsen sin *umiddelbart*
    override val posterUrl: String = "",
    override val type: String = "TVShow",

    val episodes: List<String> = emptyList(), // Listen skal ikke endres i appen - data kommer fra API
    val seasons: List<String> = emptyList()
) : Production()
