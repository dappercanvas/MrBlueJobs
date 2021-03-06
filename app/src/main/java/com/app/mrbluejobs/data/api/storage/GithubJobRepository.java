package com.app.mrbluejobs.data.api.storage;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.app.mrbluejobs.Utils;
import com.app.mrbluejobs.data.api.ConnectionServer;
import com.app.mrbluejobs.data.api.entity.GithubJob;
import com.app.mrbluejobs.data.api.entity.NetworkState;
import com.app.mrbluejobs.data.api.paging.GithubDataSourceFactory;
import com.app.mrbluejobs.data.api.paging.GithubNetwork;
import com.app.mrbluejobs.data.api.paging.LocalDataSourceFactory;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.schedulers.Schedulers;

public class GithubJobRepository {

    private static GithubJobDatabase database;
    private static GithubJobRepository repository;

    private static GithubNetwork network;
    private MediatorLiveData githubMediatorLiveData;
    private LiveData<PagedList<GithubJob>> pagedListLiveData;

    public GithubJobRepository(Context context) {
        database = GithubJobDatabase.getDatabase(context);
    }

    public static GithubJobRepository getInstance(Context context) {
        if (repository == null) {
            repository = new GithubJobRepository(context);
        }
        return repository;
    }

    public void updateMarkJob(GithubJob githubJob) {
        githubJob.is_mark = githubJob.is_mark == 1 ? 0 : 1;
        database.githubJobDao().insert(githubJob);
    }

    public void insert(GithubJob githubJob) {
        if (database.githubJobDao().getDataById(githubJob.id) == null) {
            database.githubJobDao().insert(githubJob);
        }
    }

    public GithubJob getById(String id) {
        return database.githubJobDao().getDataById(id);
    }

    public LiveData<List<GithubJob>> getLiveData() {
        return database.githubJobDao().getLiveData();
    }

    public LiveData<List<GithubJob>> searchLiveData(String keyword) {
        keyword = "%" + keyword + "%";
        return database.githubJobDao().searchLiveData(keyword);
    }

    public LiveData<List<GithubJob>> getLiveDataMarked() {
        return database.githubJobDao().getLiveDataMarked();
    }


    /*PAGING*/
    public LiveData<PagedList<GithubJob>> getPagedListLiveData() {
        return pagedListLiveData;
    }

    private PagedList.BoundaryCallback<GithubJob> boundaryCallback = new PagedList.BoundaryCallback<GithubJob>() {
        @Override
        public void onZeroItemsLoaded() {
            super.onZeroItemsLoaded();
            githubMediatorLiveData.addSource(getPagedListLiveData(), value -> {
                githubMediatorLiveData.setValue(value);
                githubMediatorLiveData.removeSource(getPagedListLiveData());
            });
        }
    };

    public void initPageDao(String keyword) {
        PagedList.Config config = (new PagedList.Config.Builder()).setEnablePlaceholders(false)
                .setInitialLoadSizeHint(Integer.MAX_VALUE).setPageSize(Integer.MAX_VALUE).build();
        Executor executor = Executors.newFixedThreadPool(3);
        LocalDataSourceFactory localDataSourceFactory = new LocalDataSourceFactory(database.githubJobDao(), keyword);
        LivePagedListBuilder livePagedListBuilder = new LivePagedListBuilder(localDataSourceFactory, config);
        pagedListLiveData = livePagedListBuilder.setFetchExecutor(executor).build();
    }

    public LiveData<PagedList<GithubJob>> getDataByPage(ConnectionServer connectionServer, GithubJobRepository repository, String keyword) {
        initPageDao(keyword);
        GithubDataSourceFactory githubDataSourceFactory = new GithubDataSourceFactory(connectionServer, repository, keyword);
        network = new GithubNetwork(githubDataSourceFactory, boundaryCallback);
        githubMediatorLiveData = new MediatorLiveData<>();
        githubMediatorLiveData.addSource(network.getPagedListLiveData(), value ->  {
            githubMediatorLiveData.setValue(value);
        });
        githubDataSourceFactory.getData()
                .observeOn(Schedulers.io())
                .subscribe(item -> {
                    item.createdAt = Utils.dateFormatter(item.createdAt);
                    database.githubJobDao().insert(item);
                });
        return githubMediatorLiveData;
    }

    public LiveData<NetworkState> getNetworkState() {
        return network.getNetworkStateLiveData();
    }
}