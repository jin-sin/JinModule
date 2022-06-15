package com.appknot.sample.module

import com.appknot.sample.api.SampleApi
import com.appknot.core_rx.coroutine.CoroutinesResponseCallAdapterFactory
import com.appknot.sample.viewmodel.PagingViewModel
import com.appknot.sample.viewmodel.TimerViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 *
 * @author Jin on 2020-02-25
 */

val appContext = module {
    single(named("appContext")) { androidContext() }
}

fun getUrl() = "https://pokeapi.co/api/v2/"

val apiModule = module {
    single {
        val httpLoggingInterceptor = HttpLoggingInterceptor()

        Retrofit.Builder()
            .baseUrl(getUrl())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .addCallAdapterFactory(CoroutinesResponseCallAdapterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(
                        httpLoggingInterceptor.apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                    .build()
            )
            .build()
            .create(SampleApi::class.java)
    }
}

val moshiModule = module {
    single {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }
}

val viewModelModule = module {
    viewModel { TimerViewModel() }
    viewModel { PagingViewModel(get()) }
}