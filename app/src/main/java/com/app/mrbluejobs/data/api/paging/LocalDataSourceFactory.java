package com.app.mrbluejobs.data.api.paging;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

import com.app.mrbluejobs.data.api.storage.GithubJobDao;

public class LocalDataSourceFactory extends DataSource.Factory {

    private LocalPageKeyedDataSource localPageKeyedDataSource;

    public LocalDataSourceFactory(GithubJobDao dao, String keyword) {
        localPageKeyedDataSource = new LocalPageKeyedDataSource(dao, keyword);
    }

    @NonNull
    @Override
    public DataSource create() {
        return localPageKeyedDataSource;
    }
}