package com.appknot.sample.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 *
 * @author Jin on 2021/09/27
 */

@JsonClass(generateAdapter = true)
data class PokemonResponse(
    @field:Json(name = "count") val count: Int,
    @field:Json(name = "next") val next: String?,
    @field:Json(name = "previous") val previous: String?,
    @field:Json(name = "results") val results: List<Pokemon>
)