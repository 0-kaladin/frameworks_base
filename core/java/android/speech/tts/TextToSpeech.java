/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package android.speech.tts;

import android.speech.tts.ITts;
import android.speech.tts.ITtsCallback;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 *
 * Synthesizes speech from text for immediate playback or to create a sound file.
 *
 */
//TODO complete javadoc + add links to constants
public class TextToSpeech {

    /**
     * Denotes a successful operation.
     */
    public static final int TTS_SUCCESS                = 0;
    /**
     * Denotes a generic operation failure.
     */
    public static final int TTS_ERROR                  = -1;

    /**
     * Queue mode where all entries in the playback queue (media to be played
     * and text to be synthesized) are dropped and replaced by the new entry.
     */
    public static final int TTS_QUEUE_FLUSH = 0;
    /**
     * Queue mode where the new entry is added at the end of the playback queue.
     */
    public static final int TTS_QUEUE_ADD = 1;


    /**
     * Denotes the language is available exactly as specified by the locale
     */
    public static final int TTS_LANG_COUNTRY_VAR_AVAILABLE = 2;


    /**
     * Denotes the language is available for the language and country specified 
     * by the locale, but not the variant.
     */
    public static final int TTS_LANG_COUNTRY_AVAILABLE = 1;


    /**
     * Denotes the language is available for the language by the locale, 
     * but not the country and variant.
     */
    public static final int TTS_LANG_AVAILABLE = 0;

    /**
     * Denotes the language data is missing.
     */
    public static final int TTS_LANG_MISSING_DATA = -1;

    /**
     * Denotes the language is not supported by the current TTS engine.
     */
    public static final int TTS_LANG_NOT_SUPPORTED = -2;


    /**
     * Called when the TTS has initialized.
     *
     * The InitListener must implement the onInit function. onInit is passed a
     * status code indicating the result of the TTS initialization.
     */
    public interface OnInitListener {
        public void onInit(int status);
    }

    /**
     * Called when the TTS has completed saying something that has an utterance ID set.
     *
     * The OnUtteranceCompletedListener must implement the onUtteranceCompleted function.
     * onUtteranceCompleted is passed a String that is the utteranceId given in
     * the original speak call.
     */
    public interface OnUtteranceCompletedListener {
        public void onUtteranceCompleted(String utteranceId);
    }


    /**
     * Internal constants for the TTS functionality
     *
     * {@hide}
     */
    public class Engine {
        // default values for a TTS engine when settings are not found in the provider
        public static final int FALLBACK_TTS_DEFAULT_RATE = 100; // 1x
        public static final int FALLBACK_TTS_DEFAULT_PITCH = 100;// 1x
        public static final int FALLBACK_TTS_USE_DEFAULTS = 0; // false
        public static final String FALLBACK_TTS_DEFAULT_SYNTH = "com.svox.pico";

        // default values for rendering
        public static final int TTS_DEFAULT_STREAM = AudioManager.STREAM_MUSIC;

        // return codes for a TTS engine's check data activity
        public static final int CHECK_VOICE_DATA_PASS = 1;
        public static final int CHECK_VOICE_DATA_FAIL = 0;
        public static final int CHECK_VOICE_DATA_BAD_DATA = -1;
        public static final int CHECK_VOICE_DATA_MISSING_DATA = -2;
        public static final int CHECK_VOICE_DATA_MISSING_DATA_NO_SDCARD = -3;

        // return codes for a TTS engine's check data activity
        public static final String VOICE_DATA_ROOT_DIRECTORY = "dataRoot";
        public static final String VOICE_DATA_FILES = "dataFiles";
        public static final String VOICE_DATA_FILES_INFO = "dataFilesInfo";

        // keys for the parameters passed with speak commands
        public static final String TTS_KEY_PARAM_RATE = "rate";
        public static final String TTS_KEY_PARAM_LANGUAGE = "language";
        public static final String TTS_KEY_PARAM_COUNTRY = "country";
        public static final String TTS_KEY_PARAM_VARIANT = "variant";
        public static final String TTS_KEY_PARAM_STREAM = "streamType";
        public static final String TTS_KEY_PARAM_UTTERANCE_ID = "utteranceId";
        protected static final int TTS_PARAM_POSITION_RATE = 0;
        protected static final int TTS_PARAM_POSITION_LANGUAGE = 2;
        protected static final int TTS_PARAM_POSITION_COUNTRY = 4;
        protected static final int TTS_PARAM_POSITION_VARIANT = 6;
        protected static final int TTS_PARAM_POSITION_STREAM = 8;
        protected static final int TTS_PARAM_POSITION_UTTERANCE_ID = 10;
        protected static final int TTS_NB_CACHED_PARAMS = 6;
    }

    /**
     * Connection needed for the TTS.
     */
    private ServiceConnection mServiceConnection;

    private ITts mITts = null;
    private ITtsCallback mITtscallback = null;
    private Context mContext = null;
    private String mPackageName = "";
    private OnInitListener mInitListener = null;
    private boolean mStarted = false;
    private final Object mStartLock = new Object();
    /**
     * Used to store the cached parameters sent along with each synthesis request to the
     * TTS service.
     */
    private String[] mCachedParams;

    /**
     * The constructor for the TTS.
     *
     * @param context
     *            The context
     * @param listener
     *            The InitListener that will be called when the TTS has
     *            initialized successfully.
     */
    public TextToSpeech(Context context, OnInitListener listener) {
        mContext = context;
        mPackageName = mContext.getPackageName();
        mInitListener = listener;

        mCachedParams = new String[2*Engine.TTS_NB_CACHED_PARAMS]; // store key and value
        mCachedParams[Engine.TTS_PARAM_POSITION_RATE] = Engine.TTS_KEY_PARAM_RATE;
        mCachedParams[Engine.TTS_PARAM_POSITION_LANGUAGE] = Engine.TTS_KEY_PARAM_LANGUAGE;
        mCachedParams[Engine.TTS_PARAM_POSITION_COUNTRY] = Engine.TTS_KEY_PARAM_COUNTRY;
        mCachedParams[Engine.TTS_PARAM_POSITION_VARIANT] = Engine.TTS_KEY_PARAM_VARIANT;
        mCachedParams[Engine.TTS_PARAM_POSITION_STREAM] = Engine.TTS_KEY_PARAM_STREAM;
        mCachedParams[Engine.TTS_PARAM_POSITION_UTTERANCE_ID] = Engine.TTS_KEY_PARAM_UTTERANCE_ID;

        mCachedParams[Engine.TTS_PARAM_POSITION_RATE + 1] =
                String.valueOf(Engine.FALLBACK_TTS_DEFAULT_RATE);
        // initialize the language cached parameters with the current Locale
        Locale defaultLoc = Locale.getDefault();
        mCachedParams[Engine.TTS_PARAM_POSITION_LANGUAGE + 1] = defaultLoc.getISO3Language();
        mCachedParams[Engine.TTS_PARAM_POSITION_COUNTRY + 1] = defaultLoc.getISO3Country();
        mCachedParams[Engine.TTS_PARAM_POSITION_VARIANT + 1] = defaultLoc.getVariant();

        mCachedParams[Engine.TTS_PARAM_POSITION_STREAM + 1] =
                String.valueOf(Engine.TTS_DEFAULT_STREAM);
        mCachedParams[Engine.TTS_PARAM_POSITION_UTTERANCE_ID + 1] = "";

        initTts();
    }


    private void initTts() {
        mStarted = false;

        // Initialize the TTS, run the callback after the binding is successful
        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                synchronized(mStartLock) {
                    mITts = ITts.Stub.asInterface(service);
                    mStarted = true;
                    if (mInitListener != null) {
                        // TODO manage failures and missing resources
                        mInitListener.onInit(TTS_SUCCESS);
                    }
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                synchronized(mStartLock) {
                    mITts = null;
                    mInitListener = null;
                    mStarted = false;
                }
            }
        };

        Intent intent = new Intent("android.intent.action.START_TTS_SERVICE");
        intent.addCategory("android.intent.category.TTS");
        mContext.bindService(intent, mServiceConnection,
                Context.BIND_AUTO_CREATE);
        // TODO handle case where the binding works (should always work) but
        //      the plugin fails
    }


    /**
     * Shuts down the TTS. It is good practice to call this in the onDestroy
     * method of the Activity that is using the TTS so that the TTS is stopped
     * cleanly.
     */
    public void shutdown() {
        try {
            mContext.unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
            // Do nothing and fail silently since an error here indicates that
            // binding never succeeded in the first place.
        }
    }


    /**
     * Adds a mapping between a string of text and a sound resource in a
     * package.
     *
     * @see #TTS.speak(String text, int queueMode, String[] params)
     *
     * @param text
     *            Example: <b><code>"south_south_east"</code></b><br/>
     *
     * @param packagename
     *            Pass the packagename of the application that contains the
     *            resource. If the resource is in your own application (this is
     *            the most common case), then put the packagename of your
     *            application here.<br/>
     *            Example: <b>"com.google.marvin.compass"</b><br/>
     *            The packagename can be found in the AndroidManifest.xml of
     *            your application.
     *            <p>
     *            <code>&lt;manifest xmlns:android=&quot;...&quot;
     *      package=&quot;<b>com.google.marvin.compass</b>&quot;&gt;</code>
     *            </p>
     *
     * @param resourceId
     *            Example: <b><code>R.raw.south_south_east</code></b>
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int addSpeech(String text, String packagename, int resourceId) {
        synchronized(mStartLock) {
            if (!mStarted) {
                return TTS_ERROR;
            }
            try {
                mITts.addSpeech(mPackageName, text, packagename, resourceId);
                return TTS_SUCCESS;
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - addSpeech", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - addSpeech", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - addSpeech", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return TTS_ERROR;
        }
    }


    /**
     * Adds a mapping between a string of text and a sound file. Using this, it
     * is possible to add custom pronounciations for text.
     *
     * @param text
     *            The string of text
     * @param filename
     *            The full path to the sound file (for example:
     *            "/sdcard/mysounds/hello.wav")
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int addSpeech(String text, String filename) {
        synchronized (mStartLock) {
            if (!mStarted) {
                return TTS_ERROR;
            }
            try {
                mITts.addSpeechFile(mPackageName, text, filename);
                return TTS_SUCCESS;
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - addSpeech", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - addSpeech", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - addSpeech", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return TTS_ERROR;
        }
    }


    /**
     * Speaks the string using the specified queuing strategy and speech
     * parameters. Note that the speech parameters are not universally supported
     * by all engines and will be treated as a hint. The TTS library will try to
     * fulfill these parameters as much as possible, but there is no guarantee
     * that the voice used will have the properties specified.
     *
     * @param text
     *            The string of text to be spoken.
     * @param queueMode
     *            The queuing strategy to use.
     *            See TTS_QUEUE_ADD and TTS_QUEUE_FLUSH.
     * @param params
     *            The hashmap of speech parameters to be used.
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int speak(String text, int queueMode, HashMap<String,String> params)
    {
        synchronized (mStartLock) {
            int result = TTS_ERROR;
            Log.i("TTS received: ", text);
            if (!mStarted) {
                return result;
            }
            try {
                if ((params != null) && (!params.isEmpty())) {
                    String extra = params.get(Engine.TTS_KEY_PARAM_STREAM);
                    if (extra != null) {
                        mCachedParams[Engine.TTS_PARAM_POSITION_STREAM + 1] = extra;
                    }
                    extra = params.get(Engine.TTS_KEY_PARAM_UTTERANCE_ID);
                    if (extra != null) {
                        mCachedParams[Engine.TTS_PARAM_POSITION_UTTERANCE_ID + 1] = extra;
                    }
                }
                result = mITts.speak(mPackageName, text, queueMode, mCachedParams);
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - speak", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - speak", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - speak", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                resetCachedParams();
                return result;
            }
        }
    }


    /**
     * Plays the earcon using the specified queueing mode and parameters.
     *
     * @param earcon
     *            The earcon that should be played
     * @param queueMode
     *            See TTS_QUEUE_ADD and TTS_QUEUE_FLUSH.
     * @param params
     *            The hashmap of parameters to be used.
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int playEarcon(String earcon, int queueMode,
            HashMap<String,String> params) {
        synchronized (mStartLock) {
            int result = TTS_ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if ((params != null) && (!params.isEmpty())) {
                    String extra = params.get(Engine.TTS_KEY_PARAM_STREAM);
                    if (extra != null) {
                        mCachedParams[Engine.TTS_PARAM_POSITION_STREAM + 1] = extra;
                    }
                    extra = params.get(Engine.TTS_KEY_PARAM_UTTERANCE_ID);
                    if (extra != null) {
                        mCachedParams[Engine.TTS_PARAM_POSITION_UTTERANCE_ID + 1] = extra;
                    }
                }
                result = mITts.playEarcon(mPackageName, earcon, queueMode, null);
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - playEarcon", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - playEarcon", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - playEarcon", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                resetCachedParams();
                return result;
            }
        }
    }

    /**
     * Plays silence for the specified amount of time using the specified
     * queue mode.
     *
     * @param durationInMs
     *            A long that indicates how long the silence should last.
     * @param queueMode
     *            See TTS_QUEUE_ADD and TTS_QUEUE_FLUSH.
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int playSilence(long durationInMs, int queueMode, HashMap<String,String> params) {
        synchronized (mStartLock) {
            int result = TTS_ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if ((params != null) && (!params.isEmpty())) {
                    String extra = params.get(Engine.TTS_KEY_PARAM_UTTERANCE_ID);
                    if (extra != null) {
                        mCachedParams[Engine.TTS_PARAM_POSITION_UTTERANCE_ID + 1] = extra;
                    }
                }
                result = mITts.playSilence(mPackageName, durationInMs, queueMode, mCachedParams);
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - playSilence", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - playSilence", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - playSilence", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
              return result;
            }
        }
    }


    /**
     * Returns whether or not the TTS is busy speaking.
     *
     * @return Whether or not the TTS is busy speaking.
     */
    public boolean isSpeaking() {
        synchronized (mStartLock) {
            if (!mStarted) {
                return false;
            }
            try {
                return mITts.isSpeaking();
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - isSpeaking", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - isSpeaking", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - isSpeaking", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return false;
        }
    }


    /**
     * Stops speech from the TTS.
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int stop() {
        synchronized (mStartLock) {
            int result = TTS_ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                result = mITts.stop(mPackageName);
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - stop", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - stop", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - stop", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
              return result;
            }
        }
    }


    /**
     * Sets the speech rate for the TTS engine.
     *
     * Note that the speech rate is not universally supported by all engines and
     * will be treated as a hint. The TTS library will try to use the specified
     * speech rate, but there is no guarantee.
     * This has no effect on any pre-recorded speech.
     *
     * @param speechRate
     *            The speech rate for the TTS engine. 1 is the normal speed,
     *            lower values slow down the speech (0.5 is half the normal speech rate),
     *            greater values accelerate it (2 is twice the normal speech rate).
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int setSpeechRate(float speechRate) {
        synchronized (mStartLock) {
            int result = TTS_ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if (speechRate > 0) {
                    int rate = (int)(speechRate*100);
                    mCachedParams[Engine.TTS_PARAM_POSITION_RATE + 1] = String.valueOf(rate);
                    result = mITts.setSpeechRate(mPackageName, rate);
                }
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - setSpeechRate", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - setSpeechRate", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - setSpeechRate", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
              return result;
            }
        }
    }


    /**
     * Sets the speech pitch for the TTS engine.
     *
     * Note that the pitch is not universally supported by all engines and
     * will be treated as a hint. The TTS library will try to use the specified
     * pitch, but there is no guarantee.
     * This has no effect on any pre-recorded speech.
     *
     * @param pitch
     *            The pitch for the TTS engine. 1 is the normal pitch,
     *            lower values lower the tone of the synthesized voice,
     *            greater values increase it.
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int setPitch(float pitch) {
        synchronized (mStartLock) {
            int result = TTS_ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if (pitch > 0) {
                    result = mITts.setPitch(mPackageName, (int)(pitch*100));
                }
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - setPitch", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - setPitch", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - setPitch", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
              return result;
            }
        }
    }


    /**
     * Sets the language for the TTS engine.
     *
     * Note that the language is not universally supported by all engines and
     * will be treated as a hint. The TTS library will try to use the specified
     * language as represented by the Locale, but there is no guarantee.
     *
     * @param loc
     *            The locale describing the language to be used.
     *
     * @return Code indicating the support status for the locale. See the TTS_LANG_ codes.
     */
    public int setLanguage(Locale loc) {
        synchronized (mStartLock) {
            int result = TTS_LANG_NOT_SUPPORTED;
            if (!mStarted) {
                return result;
            }
            try {
                mCachedParams[Engine.TTS_PARAM_POSITION_LANGUAGE + 1] = loc.getISO3Language();
                mCachedParams[Engine.TTS_PARAM_POSITION_COUNTRY + 1] = loc.getISO3Country();
                mCachedParams[Engine.TTS_PARAM_POSITION_VARIANT + 1] = loc.getVariant();
                result = mITts.setLanguage(mPackageName,
                        mCachedParams[Engine.TTS_PARAM_POSITION_LANGUAGE + 1],
                        mCachedParams[Engine.TTS_PARAM_POSITION_COUNTRY + 1],
                        mCachedParams[Engine.TTS_PARAM_POSITION_VARIANT + 1] );
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - setLanguage", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - setLanguage", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - setLanguage", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
              return result;
            }
        }
    }


    /**
     * Returns a Locale instance describing the language currently being used by the TTS engine.
     * @return language, country (if any) and variant (if any) used by the engine stored in a Locale
     *     instance, or null is the TTS engine has failed.
     */
    public Locale getLanguage() {
        synchronized (mStartLock) {
            if (!mStarted) {
                return null;
            }
            try {
                String[] locStrings =  mITts.getLanguage();
                if (locStrings.length == 3) {
                    return new Locale(locStrings[0], locStrings[1], locStrings[2]);
                } else {
                    return null;
                }
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - getLanguage", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - getLanguage", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - getLanguage", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return null;
        }
    }

    /**
     * Checks if the specified language as represented by the Locale is available.
     *
     * @param loc
     *            The Locale describing the language to be used.
     *
     * @return one of TTS_LANG_NOT_SUPPORTED, TTS_LANG_MISSING_DATA, TTS_LANG_AVAILABLE,
     *         TTS_LANG_COUNTRY_AVAILABLE, TTS_LANG_COUNTRY_VAR_AVAILABLE.
     */
    public int isLanguageAvailable(Locale loc) {
        synchronized (mStartLock) {
            int result = TTS_LANG_NOT_SUPPORTED;
            if (!mStarted) {
                return result;
            }
            try {
                result = mITts.isLanguageAvailable(loc.getISO3Language(),
                        loc.getISO3Country(), loc.getVariant());
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - isLanguageAvailable", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - isLanguageAvailable", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - isLanguageAvailable", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
              return result;
            }
        }
    }


    /**
     * Synthesizes the given text to a file using the specified parameters.
     *
     * @param text
     *            The String of text that should be synthesized
     * @param params
     *            A hashmap of parameters.
     * @param filename
     *            The string that gives the full output filename; it should be
     *            something like "/sdcard/myappsounds/mysound.wav".
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int synthesizeToFile(String text, HashMap<String,String> params,
            String filename) {
        synchronized (mStartLock) {
            int result = TTS_ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if ((params != null) && (!params.isEmpty())) {
                    // no need to read the stream type here
                    String extra = params.get(Engine.TTS_KEY_PARAM_UTTERANCE_ID);
                    if (extra != null) {
                        mCachedParams[Engine.TTS_PARAM_POSITION_UTTERANCE_ID + 1] = extra;
                    }
                }
                if (mITts.synthesizeToFile(mPackageName, text, mCachedParams, filename)){
                    result = TTS_SUCCESS;
                }
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - synthesizeToFile", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - synthesizeToFile", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - synthesizeToFile", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                resetCachedParams();
                return result;
            }
        }
    }


    /**
     * Convenience method to reset the cached parameters to the current default values
     * if they are not persistent between calls to the service.
     */
    private void resetCachedParams() {
        mCachedParams[Engine.TTS_PARAM_POSITION_STREAM + 1] =
                String.valueOf(Engine.TTS_DEFAULT_STREAM);
        mCachedParams[Engine.TTS_PARAM_POSITION_UTTERANCE_ID+ 1] = "";
    }

    /**
     * Sets the OnUtteranceCompletedListener that will fire when an utterance completes.
     *
     * @param listener
     *            The OnUtteranceCompletedListener
     *
     * @return Code indicating success or failure. See TTS_ERROR and TTS_SUCCESS.
     */
    public int setOnUtteranceCompletedListener(
            final OnUtteranceCompletedListener listener) {
        synchronized (mStartLock) {
            int result = TTS_ERROR;
            if (!mStarted) {
                return result;
            }
            mITtscallback = new ITtsCallback.Stub() {
                public void utteranceCompleted(String utteranceId) throws RemoteException {
                    if (listener != null) {
                        listener.onUtteranceCompleted(utteranceId);
                    }
                }
            };
            try {
                result = mITts.registerCallback(mPackageName, mITtscallback);
            } catch (RemoteException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - registerCallback", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - registerCallback", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                // TTS died; restart it.
                Log.e("TextToSpeech.java - registerCallback", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return result;
            }
        }
    }

}
