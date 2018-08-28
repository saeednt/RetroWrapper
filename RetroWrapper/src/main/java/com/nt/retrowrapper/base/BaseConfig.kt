package com.nt.retrowrapper.base

import io.reactivex.Scheduler
import retrofit2.CallAdapter
import retrofit2.Converter

interface BaseConfig {
    val baseUrl: String
    val converters: List<Converter.Factory>
    val callAdapters: List<CallAdapter.Factory>
    val observeScheduler: Scheduler
}