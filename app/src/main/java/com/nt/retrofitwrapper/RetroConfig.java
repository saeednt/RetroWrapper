package com.nt.retrofitwrapper;

import com.github.aurae.retrofit2.LoganSquareConverterFactory;
import com.nt.retrowrapper.base.BaseConfig;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

@com.nt.retrowrapper.annots.RetroConfig
public class RetroConfig implements BaseConfig {
    @NotNull
    @Override
    public io.reactivex.Scheduler getObserveScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @NotNull
    @Override
    public String getBaseUrl() {
        return "https://myurl.com";
    }

    @NotNull
    @Override
    public List<retrofit2.CallAdapter.Factory> getCallAdapters() {
        ArrayList<CallAdapter.Factory> factories = new ArrayList<>();
        factories.add(RxJava2CallAdapterFactory.create());
        return factories;
    }

    @NotNull
    @Override
    public List<retrofit2.Converter.Factory> getConverters() {
        ArrayList<Converter.Factory> factories = new ArrayList<>();
        factories.add(LoganSquareConverterFactory.create());
        return factories;
    }
}
