package database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

public class ConnectionHistoryRepository {

	public static final String TABLE_NAME = "connectionHistory";
	public static final String COL_NAME = "player_name";
	public static final String COL_IP = "ip";
	public static final String COL_LASTPLAYED = "last_played";
	
	/**
	 * Holder class for history item
	 */
	public static class HistoryItem {
		public HistoryItem(String name, String ip) {
			this.name = name;
			this.ip = ip;
		}
		
		public String name;
		public String ip;
	}
	
	public static List<HistoryItem> getSortedHistory() {
		Cursor c = DatabaseManager.getInstance().query(TABLE_NAME,
				new String[]{COL_NAME, COL_IP}, 
				null, null, null, null, COL_LASTPLAYED+" DESC");
		
		ArrayList<HistoryItem> history = new ArrayList<HistoryItem>();
		int nameColInx = c.getColumnIndex(COL_NAME);
		int ipColInx = c.getColumnIndex(COL_IP);
		
		while (c.moveToNext()) {
			history.add(new HistoryItem(c.getString(nameColInx), c.getString(ipColInx)));
		}
		
		return history;
	}
	
	/**
	 * Updates the item last played to now
	 * @param histItem
	 */
	public static void updateLastPlayed(HistoryItem histItem) {
		ContentValues cv = new ContentValues(1);
		cv.put(COL_LASTPLAYED, Calendar.getInstance().getTimeInMillis());
		DatabaseManager.getInstance().update(TABLE_NAME, cv,
				COL_NAME+" = "+histItem.name
				+" AND "+COL_IP+" = "+histItem.ip, null);
	}
	
	/**
	 * Adds the item to the history, with last played as now
	 * If the history exceeds to max, deletes old items
	 */
	public static void addHistoryItem(HistoryItem histItem) {
		ContentValues cv = new ContentValues(3);
		cv.put(COL_NAME, histItem.name);
		cv.put(COL_IP, histItem.ip);
		cv.put(COL_LASTPLAYED, Calendar.getInstance().getTimeInMillis());
		if (DatabaseManager.getInstance().insert(TABLE_NAME, null, cv) != -1) {
			// Get max from prefs
			clearOldItems(5);
		}
	}
	
	/**
	 * Deletes oldest items, if there are more than max
	 */
	public static void clearOldItems(int max) {
		DatabaseManager dbm = DatabaseManager.getInstance();
		Cursor c = dbm.query(TABLE_NAME,
				new String[]{COL_LASTPLAYED}, 
				null, null, null, null, COL_LASTPLAYED+" DESC");
		
		int found = 0;
		while (c.moveToNext()) {
			found++;
			if (found > max) {
				long newestOldTime = c.getLong(0);
				dbm.delete(TABLE_NAME, COL_LASTPLAYED+" <= "+newestOldTime, null);
				break;
			}
		}
	}
}
