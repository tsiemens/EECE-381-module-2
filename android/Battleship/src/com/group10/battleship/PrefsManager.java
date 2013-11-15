package com.group10.battleship;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * A singleton class, which allows fast access to shared preferences from anywhere
 * @author Trevor Siemens
 *
 */
public class PrefsManager {
	
	public static final String PREF_KEY_LOCAL_DEBUG = "pref_local_debug";
	public static final String PREF_KEY_USE_NIOS = "pref_use_nios";
	
	private static PrefsManager sInstance;
	
	private SharedPreferences mPrefs;
	private Editor mEditor;
	
	public static PrefsManager getInstance()
	{
		if (sInstance == null) {
			sInstance = new PrefsManager();
		}
		
		return sInstance;
	}
	
	private PrefsManager()
	{
		mPrefs = PreferenceManager.getDefaultSharedPreferences(BattleshipApplication.getAppContext());
		mEditor = mPrefs.edit();
	}
	
	// These methods are being added as we go, since we will likely not need them all
	
	public boolean putBoolean(String key, boolean val)
	{
		mEditor.putBoolean(key, val);
		return mEditor.commit();
	}
	
	public boolean getBoolean(String key, boolean defVal)
	{
		return mPrefs.getBoolean(key, defVal);
	}
}
