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
	
	public static final String PREF_KEY_USE_NIOS = "pref_use_nios";
	public static final String PREF_KEY_MM_IP = "pref_middleman_ip";
	public static final String PREF_KEY_MM_PORT = "pref_middleman_port";
	
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
	
	public boolean putInt(String key, int val)
	{
		mEditor.putInt(key, val);
		return mEditor.commit();
	}
	
	public int getInt(String key, int defVal)
	{
		return mPrefs.getInt(key, defVal);
	}
	
	public boolean putString(String key, String val)
	{
		mEditor.putString(key, val);
		return mEditor.commit();
	}
	
	public String getString(String key, String defVal)
	{
		return mPrefs.getString(key, defVal);
	}
}
