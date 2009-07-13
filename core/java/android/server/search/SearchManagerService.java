/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.server.search;

import android.app.ISearchManager;
import android.app.ISearchManagerCallback;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.List;

/**
 * The search manager service handles the search UI, and maintains a registry of searchable
 * activities.
 */
public class SearchManagerService extends ISearchManager.Stub {

    // general debugging support
    private static final String TAG = "SearchManagerService";
    private static final boolean DBG = false;

    // Context that the service is running in.
    private final Context mContext;

    // This field is initialized in ensureSearchablesCreated(), and then never modified.
    // Only accessed by ensureSearchablesCreated() and getSearchables()
    private Searchables mSearchables;

    // This field is initialized in ensureSearchDialogCreated(), and then never modified.
    // Only accessed by ensureSearchDialogCreated() and getSearchDialog()
    private SearchDialogWrapper mSearchDialog;

    /**
     * Initializes the Search Manager service in the provided system context.
     * Only one instance of this object should be created!
     *
     * @param context to use for accessing DB, window manager, etc.
     */
    public SearchManagerService(Context context)  {
        mContext = context;
        // call initialize() after all pending actions on the main system thread have finished
        new Handler().post(new Runnable() {
            public void run() {
                initialize();
            }
        });
    }

    /**
     * Initializes the list of searchable activities and the search UI.
     */
    void initialize() {
        ensureSearchablesCreated();
        ensureSearchDialogCreated();
    }

    private synchronized void ensureSearchablesCreated() {
        if (mSearchables != null) return;  // already created

        mSearchables = new Searchables(mContext);
        mSearchables.buildSearchableList();

        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packageFilter.addDataScheme("package");
        mContext.registerReceiver(mPackageChangedReceiver, packageFilter);
    }

    private synchronized void ensureSearchDialogCreated() {
        if (mSearchDialog != null) return;

        mSearchDialog = new SearchDialogWrapper(mContext);
    }

    private synchronized Searchables getSearchables() {
        ensureSearchablesCreated();
        return mSearchables;
    }

    private synchronized SearchDialogWrapper getSearchDialog() {
        ensureSearchDialogCreated();
        return mSearchDialog;
    }

    /**
     * Refreshes the "searchables" list when packages are added/removed.
     */
    private BroadcastReceiver mPackageChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_PACKAGE_ADDED.equals(action) ||
                    Intent.ACTION_PACKAGE_REMOVED.equals(action) ||
                    Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                if (DBG) Log.d(TAG, "Got " + action);
                // Dismiss search dialog, since the search context may no longer be valid
                getSearchDialog().stopSearch();
                // Update list of searchable activities
                getSearchables().buildSearchableList();
                broadcastSearchablesChanged();
            }
        }
    };

    /**
     * Informs all listeners that the list of searchables has been updated.
     */
    void broadcastSearchablesChanged() {
        mContext.sendBroadcast(
                new Intent(SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED));
    }

    //
    // Searchable activities API
    //

    /**
     * Returns the SearchableInfo for a given activity.
     *
     * @param launchActivity The activity from which we're launching this search.
     * @param globalSearch If false, this will only launch the search that has been specifically
     * defined by the application (which is usually defined as a local search).  If no default
     * search is defined in the current application or activity, no search will be launched.
     * If true, this will always launch a platform-global (e.g. web-based) search instead.
     * @return Returns a SearchableInfo record describing the parameters of the search,
     * or null if no searchable metadata was available.
     */
    public SearchableInfo getSearchableInfo(final ComponentName launchActivity,
            final boolean globalSearch) {
        if (globalSearch) {
            return getSearchables().getDefaultSearchable();
        } else {
            if (launchActivity == null) {
                Log.e(TAG, "getSearchableInfo(), activity == null");
                return null;
            }
            return getSearchables().getSearchableInfo(launchActivity);
        }
    }

    /**
     * Returns a list of the searchable activities that can be included in global search.
     */
    public List<SearchableInfo> getSearchablesInGlobalSearch() {
        return getSearchables().getSearchablesInGlobalSearchList();
    }

    /**
     * Returns a list of the searchable activities that handle web searches.
     * Can be called from any thread.
     */
    public List<SearchableInfo> getSearchablesForWebSearch() {
        return getSearchables().getSearchablesForWebSearchList();
    }

    /**
     * Returns the default searchable activity for web searches.
     * Can be called from any thread.
     */
    public SearchableInfo getDefaultSearchableForWebSearch() {
        return getSearchables().getDefaultSearchableForWebSearch();
    }

    /**
     * Sets the default searchable activity for web searches.
     * Can be called from any thread.
     */
    public void setDefaultWebSearch(final ComponentName component) {
        getSearchables().setDefaultWebSearch(component);
        broadcastSearchablesChanged();
    }

    // Search UI API

    /**
     * Launches the search UI. Can be called from any thread.
     *
     * @see SearchManager#startSearch(String, boolean, ComponentName, Bundle, boolean)
     */
    public void startSearch(String initialQuery,
            boolean selectInitialQuery,
            ComponentName launchActivity,
            Bundle appSearchData,
            boolean globalSearch,
            ISearchManagerCallback searchManagerCallback) {
        getSearchDialog().startSearch(initialQuery,
                selectInitialQuery,
                launchActivity,
                appSearchData,
                globalSearch,
                searchManagerCallback);
    }

    /**
     * Cancels the search dialog. Can be called from any thread.
     */
    public void stopSearch() {
        getSearchDialog().stopSearch();
    }

}
