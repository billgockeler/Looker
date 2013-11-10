package com.kogeto.looker.util;

import java.util.Calendar;
import java.util.Date;

public class StringUtil {

	public static String proper(String s){
		StringBuilder b = new StringBuilder(s);
		int i = 0;
		do {
		  b.replace(i, i + 1, b.substring(i,i + 1).toUpperCase());
		  i =  b.indexOf(" ", i) + 1;
		} 
		while (i > 0 && i < b.length());
		
		return b.toString();
	}
	
	
	
	public static String getDateString(String created_date_string){
	    Calendar now_calendar = Calendar.getInstance();

		if(created_date_string != null){
			try{
				Date date_created = Constants.LOOKER.SERVER_DATE_FORMAT.parse(created_date_string);
				Calendar created_calendar = Calendar.getInstance();
				created_calendar.setTime(date_created);
				
				int years, months, weeks, days, hours, minutes, seconds;
				
				years = now_calendar.get(Calendar.YEAR) - created_calendar.get(Calendar.YEAR);
				
				if(years == 1){
					return "1 year ago";
				}
				else if(years > 1){
					return years + " years ago";
				}
				
				months = now_calendar.get(Calendar.MONTH) - created_calendar.get(Calendar.MONTH);
				
				if(months == 1){
					return "1 month ago";
				}
				else if(months > 1){
					return months + " months ago";
				}
				
				weeks = now_calendar.get(Calendar.WEEK_OF_YEAR) - created_calendar.get(Calendar.WEEK_OF_YEAR);
				
				if(weeks == 1){
					return "1 week ago";
				}
				else if(months > 1){
					return months + " weeks ago";
				}
				
				days = now_calendar.get(Calendar.DAY_OF_MONTH) - created_calendar.get(Calendar.DAY_OF_MONTH);
				
				if(days == 1){
					return "1 day ago";
				}
				else if(days > 1){
					return days + " days ago";
				}
				
				hours = now_calendar.get(Calendar.HOUR_OF_DAY) - created_calendar.get(Calendar.HOUR_OF_DAY);
				
				if(hours == 1){
					return "1 hour ago";
				}
				else if(hours > 1){
					return hours + " hours ago";
				}
				
				minutes = now_calendar.get(Calendar.MINUTE) - created_calendar.get(Calendar.MINUTE);
				
				if(minutes == 1){
					return "1 minute ago";
				}
				else if(minutes > 1){
					return minutes + " minutes ago";
				}

				seconds = now_calendar.get(Calendar.SECOND) - created_calendar.get(Calendar.SECOND);
				
				if(seconds == 1){
					return "1 second ago";
				}
				else if(seconds > 1){
					return seconds + " seconds ago";
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return "unknown"; 
	}
	
}
