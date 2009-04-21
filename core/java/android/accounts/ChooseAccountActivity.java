/*
 * Copyright (C) 2009 The Android Open Source Project
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
package android.accounts;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;
import android.util.Log;

public class ChooseAccountActivity extends ListActivity {
    private static final String TAG = "AccountManager";
    private Parcelable[] mAccounts = null;
    private AccountManagerResponse mAccountManagerResponse = null;
    private Bundle mResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mAccounts = getIntent().getParcelableArrayExtra(Constants.ACCOUNTS_KEY);
            mAccountManagerResponse =
                    getIntent().getParcelableExtra(Constants.ACCOUNT_MANAGER_RESPONSE_KEY);
        } else {
            mAccounts = savedInstanceState.getParcelableArray(Constants.ACCOUNTS_KEY);
            mAccountManagerResponse =
                    savedInstanceState.getParcelable(Constants.ACCOUNT_MANAGER_RESPONSE_KEY);
        }

        String[] mAccountNames = new String[mAccounts.length];
        for (int i = 0; i < mAccounts.length; i++) {
            mAccountNames[i] = ((Account) mAccounts[i]).mName;
        }

        // Use an existing ListAdapter that will map an array
        // of strings to TextViews
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mAccountNames));
        getListView().setTextFilterEnabled(true);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Account account = (Account) mAccounts[position];
        Log.d(TAG, "selected account " + account);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ACCOUNT_NAME_KEY, account.mName);
        bundle.putString(Constants.ACCOUNT_TYPE_KEY, account.mType);
        mResult = bundle;
        finish();
    }

    public void finish() {
        if (mAccountManagerResponse != null) {
            if (mResult != null) {
                mAccountManagerResponse.onResult(mResult);
            } else {
                mAccountManagerResponse.onError(Constants.ERROR_CODE_CANCELED, "canceled");
            }
        }
        super.finish();
    }
}
