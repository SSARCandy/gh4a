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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.gh4a.Constants;
import com.gh4a.Gh4Application;
import com.gh4a.LoadingFragmentPagerActivity;
import com.gh4a.R;
import com.gh4a.fragment.IssueMilestoneListFragment;

public class IssueMilestoneListActivity extends LoadingFragmentPagerActivity {
    private String mRepoOwner;
    private String mRepoName;

    private static final int[] TITLES = new int[] {
        R.string.open, R.string.closed
    };

    public void onCreate(Bundle savedInstanceState) {
        setTheme(Gh4Application.THEME);
        super.onCreate(savedInstanceState);
        
        mRepoOwner = getIntent().getExtras().getString(Constants.Repository.REPO_OWNER);
        mRepoName = getIntent().getExtras().getString(Constants.Repository.REPO_NAME);
        
        if (!isOnline()) {
            setErrorView();
            return;
        }
        
        setContentView(R.layout.view_pager);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.issue_manage_milestones);
        actionBar.setSubtitle(mRepoOwner + "/" + mRepoName);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setupPager();
    }

    @Override
    protected int[] getTabTitleResIds() {
        return TITLES;
    }

    @Override
    protected Fragment getFragment(int position) {
        return IssueMilestoneListFragment.newInstance(mRepoOwner, mRepoName,
                position == 1 ? Constants.Issue.ISSUE_STATE_CLOSED : Constants.Issue.ISSUE_STATE_OPEN);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Gh4Application.get(this).isAuthorized()) {
            menu.clear();
            MenuInflater inflater = getSupportMenuInflater();
            inflater.inflate(R.menu.create_new, menu);
        }
        return true;
    }

    @Override
    protected void navigateUp() {
        Gh4Application.get(this).openIssueListActivity(this, mRepoOwner, mRepoName,
                Constants.Issue.ISSUE_STATE_OPEN, Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.create_new:
            Intent intent = new Intent().setClass(this, IssueMilestoneCreateActivity.class);
            intent.putExtra(Constants.Repository.REPO_OWNER, mRepoOwner);
            intent.putExtra(Constants.Repository.REPO_NAME, mRepoName);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}