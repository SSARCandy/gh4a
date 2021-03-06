package com.gh4a.loader;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.egit.github.core.client.PageIterator;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.gh4a.Constants;

public class PageIteratorLoader<T> extends AsyncTaskLoader<LoaderResult<PageIteratorLoader<T>.LoadedPage<T>>> {
    private PageIterator<T> mPageIterator;
    private ArrayList<T> mPreviouslyLoadedData;

    public class LoadedPage<T> {
        public final Collection<T> results;
        public final boolean hasMoreData;
        private LoadedPage(Collection<T> r, boolean hmd) {
            results = r;
            hasMoreData = hmd;
        }
    }

    public PageIteratorLoader(Context context, PageIterator<T> pageIterator) {
        super(context);
        mPageIterator = pageIterator;
        mPreviouslyLoadedData = new ArrayList<>();
        onContentChanged();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mPageIterator.reset();
        mPreviouslyLoadedData.clear();
    }

    @Override
    protected void onReset() {
        super.onReset();
        mPageIterator.reset();
        mPreviouslyLoadedData.clear();
    }

    @Override
    public LoaderResult<LoadedPage<T>> loadInBackground() {
        if (mPageIterator.hasNext()) {
            try {
                Collection<T> newData = mPageIterator.next();
                mPreviouslyLoadedData = new ArrayList<>(mPreviouslyLoadedData);
                mPreviouslyLoadedData.addAll(newData);
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, e.getMessage(), e);
                return new LoaderResult<>(e);
            }
        }

        return new LoaderResult<>(new LoadedPage<>(mPreviouslyLoadedData, mPageIterator.hasNext()));
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged()) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
