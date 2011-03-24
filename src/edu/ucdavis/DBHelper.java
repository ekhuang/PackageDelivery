package edu.ucdavis;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String TAG = "DBHelper";
	// DB constants
	private static final String DB_NAME = "friends.db";
	private static final int DB_VERSION = 1;	// Schema version
	
	// Schema constants
	public static final String TABLE_NAME 	 = "Friends";
	public static final String COL_NAME 	 = "name";
	public static final String COL_PHONE  = "phone";
	
	// Constructor
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	/** This is where we create the DB first time around */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table "+TABLE_NAME +
				" (_id integer not null primary key autoincrement, " +
				COL_NAME + " integer, " +
				COL_PHONE + " integer)";
		Log.d(TAG, "onCreate(): sql="+sql);
		db.execSQL(sql);
	}

	/** This is where we upgrade the database based on version */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Typically you would have ALTER TABLE sql code here
	}
}
