package com.app.mrbluejobs.data.api.paging;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.app.mrbluejobs.Utils;
import com.app.mrbluejobs.data.api.ConnectionServer;
import com.app.mrbluejobs.data.api.entity.GithubJob;
import com.app.mrbluejobs.data.api.entity.NetworkState;
import com.app.mrbluejobs.data.api.storage.GithubJobRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.subjects.ReplaySubject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GithubPageKeyedDataSource extends androidx.paging.PageKeyedDataSource<String, GithubJob> {

    private final MutableLiveData networkState;
    private final ReplaySubject<GithubJob> githubJobObserve;
    private final ConnectionServer connectionServer;
    private final GithubJobRepository repository;
    private final String keyword;

    public GithubPageKeyedDataSource(ConnectionServer connectionServer, GithubJobRepository repository, String keyword) {
        this.networkState = new MutableLiveData();
        this.connectionServer = connectionServer;
        this.repository = repository;
        this.githubJobObserve = ReplaySubject.create();
        this.keyword = keyword;
    }

    public MutableLiveData getNetworkState() {
        return networkState;
    }

    public ReplaySubject<GithubJob> getGithubJobObserve() {
        return githubJobObserve;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull LoadInitialCallback<String, GithubJob> callback) {
        networkState.postValue(NetworkState.FIRST_LOADING);
        Call<List<GithubJob>> call = null;
        if (keyword != null) {
            call = connectionServer.searchJobListByPage(1, keyword);
        } else {
            call = connectionServer.getJobListByPage(1);
        }
        call.enqueue(new Callback<List<GithubJob>>() {
            @Override
            public void onResponse(Call<List<GithubJob>> call, Response<List<GithubJob>> response) {
                if (response.isSuccessful()) {
                    networkState.postValue(NetworkState.LOADED);
                    callback.onResult(response.body(), Integer.toString(1), Integer.toString(2));
                    if (response.body().size() > 0) {
                        for (GithubJob item : response.body()) {
                            githubJobObserve.onNext(item);
                        }
                    } else {
                        networkState.postValue(new NetworkState(NetworkState.Status.FIRST_FAILED, "No data found"));
                    }
                } else {
                    networkState.postValue(new NetworkState(NetworkState.Status.FIRST_FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(Call<List<GithubJob>> call, Throwable t) {
                networkState.postValue(new NetworkState(NetworkState.Status.FIRST_FAILED, Utils.errorMessageHandler(call, t)));
                callback.onResult(new ArrayList<>(), Integer.toString(1), Integer.toString(2));
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, GithubJob> callback) {}

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, GithubJob> callback) {
        networkState.postValue(NetworkState.LOADING);
        final AtomicInteger page = new AtomicInteger(0);
        try {
            page.set(Integer.parseInt(params.key));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        Call<List<GithubJob>> call = null;
        if (keyword != null) {
            call = connectionServer.searchJobListByPage(page.get(), keyword);
        } else {
            call = connectionServer.getJobListByPage(page.get());
        }
        call.enqueue(new Callback<List<GithubJob>>() {
            @Override
            public void onResponse(Call<List<GithubJob>> call, Response<List<GithubJob>> response) {
                if (response.isSuccessful()) {
                    networkState.postValue(NetworkState.LOADED);
                    callback.onResult(response.body(), Integer.toString( page.get() + 1));
                    if (response.body().size() > 0) {
                        for (GithubJob item : response.body()) {
                            githubJobObserve.onNext(item);
                        }
                    }
                } else {
                    networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(Call<List<GithubJob>> call, Throwable t) {
                networkState.postValue(new NetworkState(NetworkState.Status.FAILED, Utils.errorMessageHandler(call, t)));
                callback.onResult(new ArrayList<>(), Integer.toString(page.get()));
            }
        });
    }
}