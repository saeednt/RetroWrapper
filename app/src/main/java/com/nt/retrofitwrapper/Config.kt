package com.nt.retrofitwrapper

import com.github.aurae.retrofit2.LoganSquareConverterFactory
import com.nt.retrowrapper.annots.RetroConfig
import com.nt.retrowrapper.base.BaseConfig
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


@RetroConfig
class Config : BaseConfig {
    override val baseUrl: String
        get() = "http://BaseUrlOfRetro"

    override val converters: List<Converter.Factory> = arrayListOf(LoganSquareConverterFactory.create())

    override val callAdapters: List<CallAdapter.Factory> = arrayListOf(RxJava2CallAdapterFactory.create())

    override val observeScheduler: Scheduler
        get() = AndroidSchedulers.mainThread()

}