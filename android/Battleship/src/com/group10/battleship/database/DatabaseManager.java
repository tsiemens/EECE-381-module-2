package com.group10.battleship.database;

import com.group10.battleship.BattleshipApplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager extends SQLiteOpenHelper{

	private static final String DB_NAME = "BattleshipDatabase";
	private static final int DB_VERSION = 1;
	
	private static DatabaseManager sInstance;
	
	private SQLiteDatabase mDatabase;
	
	public static DatabaseManager getInstance() {
		if (sInstance == null) {
			Log.d("test", "creating db instance");
			sInstance = new DatabaseManager();
		}
		return sInstance;
	}
	
	private DatabaseManager() {
		super(BattleshipApplication.getAppContext(), DB_NAME, null, DB_VERSION);
		mDatabase = getWritableDatabase();
	}
	
	private DatabaseManager(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		TableCreator.createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public Cursor query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having, String orderBy) {
		return mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	}
	
	public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		return mDatabase.update(table, values, whereClause, whereArgs);
	}
	
	public int delete(String table, String whereClause, String[] whereArgs) {
		return mDatabase.delete(table, whereClause, whereArgs);
	}
	
	public long insert(String table, String nullColumnHack, ContentValues values) {
		return mDatabase.insert(table, nullColumnHack, values);
	}
	
}
