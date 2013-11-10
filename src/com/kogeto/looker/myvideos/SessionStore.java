package com.kogeto.looker.myvideos;

import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.facebook.android.Facebook;
import com.kogeto.looker.util.Constants;

public class SessionStore {
	    
    
    public static boolean save(Facebook session, Context context) {
        Editor editor =  context.getSharedPreferences(Constants.PREFERENCES.FACEBOOK_KEY, Context.MODE_PRIVATE).edit();
        editor.putString(Constants.PREFERENCES.FACEBOOK_TOKEN, session.getAccessToken());
        editor.putLong(Constants.PREFERENCES.FACEBOOK_EXPIRES, session.getAccessExpires());
        return editor.commit();
    }

    
	public static boolean restore(Facebook session, Context context) {
		SharedPreferences savedSession = context.getSharedPreferences(Constants.PREFERENCES.FACEBOOK_KEY, Context.MODE_PRIVATE);
		session.setAccessToken(savedSession.getString(Constants.PREFERENCES.FACEBOOK_TOKEN, null));
		session.setAccessExpires(savedSession.getLong(Constants.PREFERENCES.FACEBOOK_EXPIRES, 0));
		return session.isSessionValid();
	}
	
	    
//    public static boolean save(AccessToken token, Context context) {
//        Editor editor =  context.getSharedPreferences(Constants.PREFERENCES.TWITTER_KEY, Context.MODE_PRIVATE).edit();
//        editor.putString(Constants.PREFERENCES.TWITTER_TOKEN, token.getToken());
//        editor.putString(Constants.PREFERENCES.TWITTER_SECRET, token.getTokenSecret());
//        return editor.commit();
//    }
//
//    
//	public static AccessToken restore(Context context) {
//		SharedPreferences savedSession = context.getSharedPreferences(Constants.PREFERENCES.TWITTER_KEY, Context.MODE_PRIVATE);
//		String token = savedSession.getString(Constants.PREFERENCES.TWITTER_TOKEN, null);
//		String secret = savedSession.getString(Constants.PREFERENCES.TWITTER_SECRET, null);
//   	    return new AccessToken(token, secret);
//	}
	
	    	
    public static void clearFacebook(Context context) {
        Editor editor = context.getSharedPreferences(Constants.PREFERENCES.FACEBOOK_KEY, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }
    
}