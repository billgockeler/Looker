package com.kogeto.looker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

	static final int    DB_VERSION 		= 1;
	static final String DB_NAME 		= "VIDEOSDB";
	static final String TABLE_NAME 		= "VIDEOS";

	static final String ID 				= "_id"; 
	static final String TITLE 			= "title";
	static final String DESCRIPTION 	= "description";
	static final String DURATION 		= "duration";
	static final String THUMBNAIL_URL 	= "thumbnail_url";
	static final String VIDEO_URL 		= "video_url";
	static final String DATE_ADDED 		= "date_added";
	static final String VIDEO_KEY 		= "video_key";
	
	static final String CREATE_DB = 
			"CREATE TABLE " + TABLE_NAME + "(" + 
					ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
					TITLE + " TEXT, " +
					DESCRIPTION + " TEXT, " +
					DURATION + " TEXT, " +
					THUMBNAIL_URL + " TEXT, " +
					VIDEO_URL + " TEXT, " +
					DATE_ADDED + " TEXT, " +
					VIDEO_KEY + " TEXT);";
	
	
	
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, 1); 
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_DB);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}