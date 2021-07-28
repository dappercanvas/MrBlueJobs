package com.app.mrbluejobs.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import javax.inject.Inject;

import  com.app.mrbluejobs.R;
import  com.app.mrbluejobs.base.BaseActivity;
import com.app.mrbluejobs.base.BaseViewModel;
import  com.app.mrbluejobs.data.api.ConnectionServer;
import com.app.mrbluejobs.data.api.entity.GithubJob;
import com.app.mrbluejobs.data.api.storage.GithubJobRepository;
import  com.app.mrbluejobs.databinding.ActivityMainBinding;
import com.app.mrbluejobs.ui.detail.DetailActivity;
import com.app.mrbluejobs.ui.list.ListActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;


public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> implements  MainViewModel.Navigator {

    @Inject ConnectionServer server;
    @Inject GithubJobRepository repository;
    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    public int getBindingVariable() {
        return 0;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public MainViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = getViewDataBinding();
        viewModel = new ViewModelProvider(this, new MainViewModel.ModelFactory(this, server, repository)).get(MainViewModel.class);
        viewModel.setNavigator(this);
        viewModel.getJobFromServer();
        viewModel.getLiveData().observe(this, githubJobs -> {
            binding.recyclerView.setAdapter(new MainAdapter(githubJobs, viewModel));
        });
        viewModel.getLiveDataMarked().observe(this, githubJobs -> {
            if (githubJobs.size() > 0) {
                binding.markedTitle.setVisibility(View.VISIBLE);
                binding.recyclerViewMarked.setVisibility(View.VISIBLE);
                binding.recyclerViewMarked.setAdapter(new MainMarkedAdapter(githubJobs, viewModel));
            } else {
                binding.markedTitle.setVisibility(View.GONE);
                binding.recyclerViewMarked.setVisibility(View.GONE);
            }
        });
        binding.swipeRefresh.setOnRefreshListener(()->viewModel.getJobFromServer());
        binding.cardSearch.setOnClickListener(v->{
            showDialogSearch();
        });
        binding.showAllMarked.setOnClickListener(v-> {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("marked", "marked");
            startActivity(intent);
        });
        binding.showAllRecommended.setOnClickListener(v-> {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("recommended", "recommended");
            startActivity(intent);
        });
    }


    private void showDialogSearch() {
        View view = getLayoutInflater().inflate(R.layout.dialog_info, null);
        TextInputEditText m = view.findViewById(R.id.message);
        RelativeLayout btnSearch = view.findViewById(R.id.card_search);

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogStyle);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("search", m.getText().toString());
            startActivity(intent);
        });
        m.requestFocus();

        dialog.setContentView(view);
        dialog.show();
    }

    @Override
    public void showProgress() {
        binding.swipeRefresh.setRefreshing(true);
        binding.emptyView.setVisibility(View.GONE);
        binding.shimmer.setVisibility(View.VISIBLE);
        binding.shimmer.startShimmer();
        binding.contentLayout.setVisibility(View.GONE);
    }

    @Override
    public void hideProgress() {
        binding.swipeRefresh.setRefreshing(false);
        binding.emptyView.setVisibility(View.GONE);
        binding.shimmer.setVisibility(View.GONE);
        binding.shimmer.stopShimmer();
        binding.contentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onGetResult(boolean status, String message) {
        if (!status) { //<-- status result is FALSE
            binding.textEmptyErr.setText(message);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMark(int mark, String title) {
        Snackbar.make(binding.getRoot(), mark == 0 ? "\uD83D\uDE13 Unmark " + title : "\uD83D\uDE0D Marked " + title, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(GithubJob githubJob) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("item", githubJob);
        startActivity(intent);
    }
}