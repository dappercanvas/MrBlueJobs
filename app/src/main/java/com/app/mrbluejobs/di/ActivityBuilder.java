package com.app.mrbluejobs.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import com.app.mrbluejobs.ui.MainActivity;
import com.app.mrbluejobs.ui.detail.DetailActivity;
import com.app.mrbluejobs.ui.list.ListActivity;

@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract MainActivity mainActivity();
    @ContributesAndroidInjector
    abstract DetailActivity detailActivity();
    @ContributesAndroidInjector
    abstract ListActivity listActivity();
}