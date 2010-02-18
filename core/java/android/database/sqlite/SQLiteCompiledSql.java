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

package android.database.sqlite;

import android.util.Log;

/**
 * This class encapsulates compilation of sql statement and release of the compiled statement obj.
 * Once a sql statement is compiled, it is cached in {@link SQLiteDatabase}
 * and it is released in one of the 2 following ways
 * 1. when {@link SQLiteDatabase} object is closed.
 * 2. dalvikVM wants to reclaim some memory and releases it from the cache in
 * {@link SQLiteDatabase}.
 */
/* package */ class SQLiteCompiledSql {

    /** The database this program is compiled against. */
    /* package */ SQLiteDatabase mDatabase;

    /**
     * Native linkage, do not modify. This comes from the database.
     */
    /* package */ int nHandle = 0;

    /**
     * Native linkage, do not modify. When non-0 this holds a reference to a valid
     * sqlite3_statement object. It is only updated by the native code, but may be
     * checked in this class when the database lock is held to determine if there
     * is a valid native-side program or not.
     */
    /* package */ int nStatement = 0;

    /** when in cache and is in use, this member is set */
    private boolean mInUse = false;

    /* package */ SQLiteCompiledSql(SQLiteDatabase db, String sql) {
        mDatabase = db;
        this.nHandle = db.mNativeHandle;
        compile(sql, true);
    }

    /**
     * Compiles the given SQL into a SQLite byte code program using sqlite3_prepare_v2(). If
     * this method has been called previously without a call to close and forCompilation is set
     * to false the previous compilation will be used. Setting forceCompilation to true will
     * always re-compile the program and should be done if you pass differing SQL strings to this
     * method.
     *
     * <P>Note: this method acquires the database lock.</P>
     *
     * @param sql the SQL string to compile
     * @param forceCompilation forces the SQL to be recompiled in the event that there is an
     *  existing compiled SQL program already around
     */
    private void compile(String sql, boolean forceCompilation) {
        // Only compile if we don't have a valid statement already or the caller has
        // explicitly requested a recompile.
        if (forceCompilation) {
            mDatabase.lock();
            try {
                // Note that the native_compile() takes care of destroying any previously
                // existing programs before it compiles.
                native_compile(sql);
            } finally {
                mDatabase.unlock();
            }
        }
    }

    /* package */ void releaseSqlStatement() {
        // Note that native_finalize() checks to make sure that nStatement is
        // non-null before destroying it.
        if (nStatement != 0) {
            try {
                mDatabase.lock();
                native_finalize();
                nStatement = 0;
            } finally {
                mDatabase.unlock();
            }
        }
    }

    /* package */ synchronized boolean isInUse() {
        return mInUse;
    }

    /* package */ synchronized void acquire() {
        mInUse = true;
    }

    /* package */ synchronized void release() {
        mInUse = false;
    }

    /**
     * Make sure that the native resource is cleaned up.
     */
    @Override
    protected void finalize() {
        releaseSqlStatement();
    }

    /**
     * Compiles SQL into a SQLite program.
     *
     * <P>The database lock must be held when calling this method.
     * @param sql The SQL to compile.
     */
    private final native void native_compile(String sql);
    private final native void native_finalize();
}
