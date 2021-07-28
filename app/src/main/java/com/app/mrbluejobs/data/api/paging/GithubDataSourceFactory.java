package com.app.mrbluejobs.data.api.paging;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import com.app.mrbluejobs.data.api.ConnectionServer;
import com.app.mrbluejobs.data.api.entity.GithubJob;
import com.app.mrbluejobs.data.api.storage.GithubJobRepository;

import io.reactivex.subjects.ReplaySubject;

public class GithubDataSourceFactory extends DataSource.Factory {

    private MutableLiveData<GithubPageKeyedDataSource> networkStatus;
    private GithubPageKeyedDataSource githubPageKeyedDataSource;
    public GithubDataSourceFactory(ConnectionServer connectionServer, GithubJobRepository repository, String keyword) {
        this.networkStatus = new MutableLiveData<>();
        githubPageKeyedDataSource = new GithubPageKeyedDataSource(connectionServer, repository, keyword);
    }

    @NonNull
    @Override
    public DataSource create() {
        networkStatus.postValue(githubPageKeyedDataSource);
        return githubPageKeyedDataSource;
    }

    public MutableLiveData<GithubPageKeyedDataSource> getNetworkStatus() {
        return networkStatus;
    }

    public ReplaySubject<GithubJob> getData() {
        return githubPageKeyedDataSource.getGithubJobObserve();
    }
}
