/*
 * Copyright 2011 Azwan Adli Abdullah
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gh4a.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gh4a.BasePagerActivity;
import com.gh4a.Constants;
import com.gh4a.R;
import com.gh4a.fragment.CommitCompareFragment;
import com.gh4a.fragment.PullRequestFilesFragment;
import com.gh4a.fragment.PullRequestFragment;
import com.gh4a.loader.LoaderCallbacks;
import com.gh4a.loader.LoaderResult;
import com.gh4a.loader.PullRequestLoader;
import com.gh4a.utils.IntentUtils;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;

import java.util.Locale;

public class PullRequestActivity extends BasePagerActivity implements
        View.OnClickListener, PullRequestFragment.StateChangeListener,
        PullRequestFilesFragment.CommentUpdateListener {
    private String mRepoOwner;
    private String mRepoName;
    private int mPullRequestNumber;

    private PullRequest mPullRequest;
    private PullRequestFragment mPullRequestFragment;

    private ViewGroup mHeader;
    private int[] mHeaderColorAttrs;

    private static final int[] TITLES = new int[] {
        R.string.pull_request_conversation, R.string.commits, R.string.pull_request_files
    };

    private LoaderCallbacks<PullRequest> mPullRequestCallback = new LoaderCallbacks<PullRequest>(this) {
        @Override
        protected Loader<LoaderResult<PullRequest>> onCreateLoader() {
            return new PullRequestLoader(PullRequestActivity.this,
                    mRepoOwner, mRepoName, mPullRequestNumber);
        }
        @Override
        protected void onResultReady(PullRequest result) {
            mPullRequest = result;
            fillHeader();
            setContentShown(true);
            invalidateTabs();
            supportInvalidateOptionsMenu();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getLayoutInflater();

        mHeader = (ViewGroup) inflater.inflate(R.layout.issue_header, null);
        mHeader.setClickable(false);
        mHeader.setVisibility(View.GONE);
        addHeaderView(mHeader, true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.pull_request_title) + " #" + mPullRequestNumber);
        actionBar.setSubtitle(mRepoOwner + "/" + mRepoName);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentShown(false);

        getSupportLoaderManager().initLoader(0, null, mPullRequestCallback);
    }

    @Override
    protected void onInitExtras(Bundle extras) {
        super.onInitExtras(extras);
        mRepoOwner = extras.getString(Constants.Repository.OWNER);
        mRepoName = extras.getString(Constants.Repository.NAME);
        mPullRequestNumber = extras.getInt(Constants.PullRequest.NUMBER);
    }

    @Override
    public void onRefresh() {
        mPullRequest = null;
        setContentShown(false);
        mHeader.setVisibility(View.GONE);
        mHeaderColorAttrs = null;
        getSupportLoaderManager().getLoader(0).onContentChanged();
        invalidateTabs();
        super.onRefresh();
    }

    @Override
    protected int[] getTabTitleResIds() {
        return mPullRequest != null ? TITLES : null;
    }

    @Override
    protected int[] getHeaderColors() {
        return mHeaderColorAttrs;
    }

    @Override
    protected Fragment getFragment(int position) {
        if (position == 1) {
            return CommitCompareFragment.newInstance(mRepoOwner, mRepoName,
                    mPullRequest.getBase().getSha(), mPullRequest.getHead().getSha());
        } else if (position == 2) {
            return PullRequestFilesFragment.newInstance(mRepoOwner, mRepoName, mPullRequestNumber);
        } else {
            mPullRequestFragment = PullRequestFragment.newInstance(mPullRequest);
            return mPullRequestFragment;
        }
    }

    @Override
    protected Intent navigateUp() {
        return IntentUtils.getPullRequestListActivityIntent(this, mRepoOwner, mRepoName,
                Constants.Issue.STATE_OPEN);
    }

    @Override
    public void onCommentsUpdated() {
        if (mPullRequestFragment != null) {
            mPullRequestFragment.refreshComments();
        }
    }

    @Override
    public void onPullRequestStateChanged(PullRequest newState) {
        mPullRequest = newState;
        fillHeader();
        transitionHeaderToColor(mHeaderColorAttrs[0], mHeaderColorAttrs[1]);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_gravatar) {
            User user = (User) v.getTag();
            Intent intent = IntentUtils.getUserActivityIntent(this, user);
            if (intent != null) {
                startActivity(intent);
            }
        }
    }

    private void fillHeader() {
        final int stateTextResId;

        if (mPullRequest.isMerged()) {
            stateTextResId = R.string.pull_request_merged;
            mHeaderColorAttrs = new int[] {
                R.attr.colorPullRequestMerged, R.attr.colorPullRequestMergedDark
            };
        } else if (Constants.Issue.STATE_CLOSED.equals(mPullRequest.getState())) {
            stateTextResId = R.string.closed;
            mHeaderColorAttrs = new int[] {
                R.attr.colorIssueClosed, R.attr.colorIssueClosedDark
            };
        } else {
            stateTextResId = R.string.open;
            mHeaderColorAttrs = new int[] {
                R.attr.colorIssueOpen, R.attr.colorIssueOpenDark
            };
        }

        TextView tvState = (TextView) mHeader.findViewById(R.id.tv_state);
        tvState.setText(getString(stateTextResId).toUpperCase(Locale.getDefault()));

        TextView tvTitle = (TextView) mHeader.findViewById(R.id.tv_title);
        tvTitle.setText(mPullRequest.getTitle());

        mHeader.setVisibility(View.VISIBLE);
    }
}
