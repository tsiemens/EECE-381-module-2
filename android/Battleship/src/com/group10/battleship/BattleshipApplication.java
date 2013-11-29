package com.group10.battleship;

import com.group10.battleship.database.DatabaseManager;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class BattleshipApplication extends Application{

	private static String TAG = BattleshipApplication.class.getSimpleName();
	
	private static BattleshipApplication sApplicationInstance;
	
	public static Context getAppContext()
	{
		return sApplicationInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		sApplicationInstance = this;
		// Creates the database
		DatabaseManager.getInstance();
		Log.i(TAG, "Starting application...");
	}

	@Override
	public void onTerminate() {
		Log.i(TAG, "Terminating application...");
		super.onTerminate();
	}
	
}
