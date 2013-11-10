package com.kogeto.looker;

import java.lang.Thread.UncaughtExceptionHandler;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.media.MediaPlayer;
import android.util.Log;

@ReportsCrashes(formUri = "http://www.bugsense.com/api/acra?api_key=9a1278e0", formKey="")
public class LookerApplication extends Application {

	public void onCreate(){
		this.attachUncaughtExceptionHandler();
		ACRA.init(this);
		MediaPlayer media_player = new MediaPlayer();
	}
	
	@SuppressWarnings("static-access")
	private void attachUncaughtExceptionHandler(){
		final UncaughtExceptionHandler handler = Thread.currentThread().getDefaultUncaughtExceptionHandler();
		
		Thread.currentThread().setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		    public void uncaughtException(Thread thread, Throwable ex) {
		    	
				Log.e("LookerApplication", "Uncaught Exception");
			    
				handler.uncaughtException(thread, ex);	    
		    }
		});		
	}

}
