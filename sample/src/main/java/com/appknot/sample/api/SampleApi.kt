package com.appknot.sample.api

import com.appknot.core_rx.api.TransApiResponse
import com.appknot.sample.model.PassengerInfo
import com.appknot.sample.model.PokemonResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SampleApi {

    @GET("v1/passenger")
    suspend fun getPassengers(
        @Query("page") page: Int,
        @Query("size") size: Int = 10
    ): TransApiResponse<PassengerInfo>


    @GET("pokemon")
    suspend fun fetchPokemonList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): TransApiResponse<PokemonResponse>
}