package com.app.mrbluejobs.data.api.paging;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.app.mrbluejobs.data.api.entity.GithubJob;
import com.app.mrbluejobs.data.api.entity.NetworkState;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GithubNetwork {

    final private LiveData<PagedList<GithubJob>> pagedListLiveData;
    final private LiveData<NetworkState> networkStateLiveData;

    public GithubNetwork(GithubDataSourceFactory githubDataSourceFactory, PagedList.BoundaryCallback<GithubJob> boundaryCallback) {
        PagedList.Config config = (new PagedList.Config.Builder())
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(1)
                .setPageSize(50)
                .build();

        this.networkStateLiveData = Transformations.switchMap(
                githubDataSourceFactory.getNetworkStatus(),
                (Function<GithubPageKeyedDataSource, LiveData<NetworkState>>) GithubPageKeyedDataSource::getNetworkState);

        Executor executor = Executors.newFixedThreadPool(3);
        LivePagedListBuilder livePagedListBuilder = new LivePagedListBuilder(githubDataSourceFactory, config);
        this.pagedListLiveData = livePagedListBuilder.setFetchExecutor(executor).setBoundaryCallback(boundaryCallback).build();
    }

    public LiveData<NetworkState> getNetworkStateLiveData() {
        return networkStateLiveData;
    }

    public LiveData<PagedList<GithubJob>> getPagedListLiveData() {
        return pagedListLiveData;
    }
}