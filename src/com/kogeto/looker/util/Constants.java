package com.kogeto.looker.util;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.facebook.android.Facebook;


public class Constants {
	
	
	public static final class TWITTER {
		public static final String CONSUMER_KEY  	= "xtRg5VZX96OCBrlANiXvg";
		public static final String CONSUMER_SECRET 	= "xAnfDtoiHcupivFXmlC2c2faWXCial9XHZCcGBsAE";
		
		public static final String REQUEST_URL 		= "http://api.twitter.com/oauth/request_token";
		public static final String ACCESS_URL 		= "http://api.twitter.com/oauth/access_token";
		public static final String AUTHORIZE_URL 	= "http://api.twitter.com/oauth/authorize";
	
		public static final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-twitter";
		public static final String OAUTH_CALLBACK_HOST   = "kogeto";
		public static final String OAUTH_CALLBACK_URL	 = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
		
	}
	
	
	public static final class FACEBOOK {
		public static final String FACEBOOK_ID 	 = "286137561524400";
		public static final String[] PERMISSIONS = new String[] {"publish_stream", "read_friendlists", "user_about_me", "user_birthday", "email", };
		public static final Facebook facebook 	 = new Facebook(FACEBOOK_ID);
	}

		
	public static final class USER 	{
	}


	public static final class LOOKER {
		public static final String VERSION = "1.0";
		public static final String APP_VERSION = VERSION + ".android";
		public static final SimpleDateFormat SERVER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

		
	public static final class PREFERENCES {
		public static final String CALIBRATED 		= "calibrated";
		public static final String CALIBRATION_X 	= "calibration_center_x";
		public static final String CALIBRATION_Y 	= "calibration_center_y";
		public static final String CALIBRATION_OUTER_DIAMETER = "calibration_outer_diameter";
		public static final String CALIBRATION_INNER_DIAMETER = "calibration_inner_diameter";
		
		public static final String FOCUS_TOP 		= "focus_top";
		public static final String FOCUS_LEFT 		= "focus_left";
		public static final String FOCUS_BOTTOM 	= "focus_bottom";
		public static final String FOCUS_RIGHT 		= "focus_right";
		
		public static final String TWITTER_TOKEN  	= "twitter_access_token";
		public static final String TWITTER_SECRET 	= "twitter_secret";
	    public static final String TWITTER_KEY 	  	= "twitter-session";
		
	    public static final String FACEBOOK_TOKEN 	= "facebook_access_token";
	    public static final String FACEBOOK_EXPIRES = "expires_in";
	    public static final String FACEBOOK_KEY 	= "facebook-session";
		
	    public static final String LOOKER_TOKEN 	= "looker_token";
	}

		
	public static final class WEB_SERVICES {
		public static String HOST = "http://api.kogeto.com/rest/v1/?method=";
		public static String DOTSPOT_HOST = "http://www.kogeto.com/dotspots/";
		
	    public static final String SEARCH_VIDEOS = "looker.video.search&keyword={keyword}";
	    public static final String FEATURED_VIDEOS = "looker.video.getFeatured";
	    public static final String REGISTER = "looker.user.create&username={username}&email={email}&password={password}&cpassword={cpassword}";
	    public static final String SIGNIN = "looker.user.signin&email={email}&password={password}";
	    public static final String ADD_FACEBOOK = "looker.user.addFacebook&looker_token={looker_token}&facebookid={facebookid}&access_token={access_token}";
	    public static final String ADD_TWITTER = "looker.user.addTwitter&looker_token={looker_token}&twitterid={twitterid}&twitter_token={twitter_token}&twitter_secret={twitter_secret}";
	    public static final String CREATE_ASSIGNMENT = "looker.video.createAssignment&looker_token={looker_token}";
	    public static final String CHECK_ASSIGNMENT = "looker.video.checkAssignment&looker_token={looker_token}&assignmentid={assignment_id}";
	    public static final String END_ASSIGNMENT = "looker.video.endAssignment&looker_token={looker_token}&assignmentid={assignment_id}";
	    public static final String UPLOAD_VIDEO = "{bucket}?assignmentid={assignment_id}&looker_token={looker_token}";
	    public static final String SHARE_FACEBOOK = "ooker.video.shareVideo?video_key={video_key}&looker_token={looker_token}&fb=1&fbmsg={facebook_message}";
	    public static final String SHARE_TWITTER = "ooker.video.shareVideo?video_key={video_key}&looker_token={looker_token}&tw=1&twmsg={twitter_message}";
	}
	
}
