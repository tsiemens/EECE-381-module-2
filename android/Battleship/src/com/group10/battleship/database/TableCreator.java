package com.group10.battleship.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TableCreator {

	private static final String TAG = TableCreator.class.getSimpleName();
	
	public static void createTables(SQLiteDatabase db) {
		Log.d(TAG, "db="+db);
		db.execSQL(createConnectionHistoryTable());
	}
	
	private static String createConnectionHistoryTable() {
		Log.i(TAG, "Creating Connection History table");
		String table = 
				"CREATE TABLE connectionHistory ("
					+" player_name TEXT,"
					+" ip TEXT,"
					+" last_played INTEGER,"
					+" PRIMARY KEY (player_name, ip)"
					+")";
		return table;
	}
}
