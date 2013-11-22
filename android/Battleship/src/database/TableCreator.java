package database;

import android.database.sqlite.SQLiteDatabase;

public class TableCreator {

	public static void createTables(SQLiteDatabase db) {
		db.execSQL(createConnectionHistoryTable());
	}
	
	private static String createConnectionHistoryTable() {
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
