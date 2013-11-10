package com.kogeto.looker.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.kogeto.looker.model.Video;

public class VideosDataSource {

	private SQLiteDatabase database;
	private DatabaseHelper helper;
	
	private String[] all_columns = { 
			DatabaseHelper.ID, 
			DatabaseHelper.TITLE, 
			DatabaseHelper.DESCRIPTION, 
			DatabaseHelper.DURATION, 
			DatabaseHelper.THUMBNAIL_URL, 
			DatabaseHelper.VIDEO_URL, 
			DatabaseHelper.DATE_ADDED, 
			DatabaseHelper.VIDEO_KEY};

	
	
	public VideosDataSource(Context context) {
		helper = new DatabaseHelper(context);
	}

  
  
	public void open() throws SQLException {
		database = helper.getWritableDatabase();
	}

  
  
	public void close() {
		helper.close();
	}

	
	
	public long insert(Video video) {
	    ContentValues values = new ContentValues();
	    
	    values.put(DatabaseHelper.TITLE, video.title);
	    values.put(DatabaseHelper.DESCRIPTION, video.description);
	    values.put(DatabaseHelper.DURATION, video.duration);
	    values.put(DatabaseHelper.THUMBNAIL_URL, video.turl);
	    values.put(DatabaseHelper.VIDEO_URL, video.vurl);
	    values.put(DatabaseHelper.DATE_ADDED, video.date_added);
	    values.put(DatabaseHelper.VIDEO_KEY, video.videokey);
	    
	    long id = database.insert(DatabaseHelper.TABLE_NAME, null, values);
	    
	    return id;
	}
	


	public boolean update(Video video){
	    ContentValues values = new ContentValues();
	    values.put(DatabaseHelper.VIDEO_KEY, video.videokey);
	    values.put(DatabaseHelper.DESCRIPTION, video.description);
	    values.put(DatabaseHelper.TITLE, video.title);
	    
	    String[] args = {Integer.toString(video.id)};

	    int rows = database.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.ID + "=?", args);
	    
	    return rows > 0;
	}
	
	
	
	public void delete(Video video) {
	    database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.ID + " = " + video.id, null);
	}

	

	public void delete(int[] ids) {
		String[] video_ids = new String[ids.length];
		
		for(int i=0; i < ids.length; i++){
			video_ids[i] = Integer.toString(ids[i]);
		}
		
		String args = TextUtils.join(", ", video_ids);
		
		database.execSQL(String.format("DELETE FROM " + DatabaseHelper.TABLE_NAME + " WHERE " + DatabaseHelper.ID + " IN (%s);", args));
	}
	
	
	
	
	public ArrayList<Video> getAllVideos() {
	    ArrayList<Video> videos = new ArrayList<Video>();
	
	    Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, all_columns, null, null, null, null, null);
	
	    cursor.moveToFirst();
	    
	    while (!cursor.isAfterLast()) {
	      Video video = new Video();
	      video.id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ID));
	      video.title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TITLE));
	      video.description = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DESCRIPTION));
	      video.duration = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DURATION));
	      video.turl = cursor.getString(cursor.getColumnIndex(DatabaseHelper.THUMBNAIL_URL));
	      video.vurl = cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_URL));
	      video.date_added = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DATE_ADDED));
	      video.videokey = cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_KEY));
	      
	      videos.add(video);
	      cursor.moveToNext();
	    }

	    cursor.close();
	    return videos;
	}
} 