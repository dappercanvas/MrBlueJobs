package com.app.mrbluejobs.data.api.paging;

import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;

import com.app.mrbluejobs.data.api.entity.GithubJob;
import com.app.mrbluejobs.data.api.storage.GithubJobDao;

import java.util.List;

public class LocalPageKeyedDataSource extends PageKeyedDataSource<String, GithubJob> {

    private final GithubJobDao dao;
    private String keyword;

    public LocalPageKeyedDataSource(GithubJobDao dao, String keyword) {
        this.dao = dao;
        this.keyword = keyword;
    }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull LoadInitialCallback<String, GithubJob> callback) {
        List<GithubJob> data = null;
        if (keyword == null) {
            data = dao.getList();
        } else {
            data = dao.searchList(keyword);
        }

        if (data.size() != 0) {
            callback.onResult(data, Integer.toString(0), Integer.toString(1));
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, GithubJob> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, GithubJob> callback) {

    }
}