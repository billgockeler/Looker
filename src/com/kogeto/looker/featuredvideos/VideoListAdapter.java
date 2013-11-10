package com.kogeto.looker.featuredvideos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.kogeto.looker.R;
import com.kogeto.looker.model.Video;
import com.kogeto.looker.util.Constants;
import com.kogeto.looker.util.StringUtil;
import com.kogeto.looker.widget.AspectImageView;


public class VideoListAdapter extends ArrayAdapter {

	private final static String TAG = "VideoListAdapter";
	
	private LayoutInflater inflater;
    private AQuery image_manager;


    
    public VideoListAdapter(Activity context, int resource, ArrayList<Video> all_videos) {
		super(context, resource, all_videos);
		this.inflater = LayoutInflater.from(context);
		this.image_manager = new AQuery(context);
	}

	
	
	static class ViewHolder {
		String id;
		TextView video_name_textview;
		TextView video_duration_textview;
		TextView created_textview;
		AspectImageView thumbnail_imageview;
	}

	
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		//Get the current cause object
        final Video video = (Video) getItem(position);

        //Inflate the view
        if(convertView == null) {
            convertView = this.inflater.inflate(R.layout.video_list_row, null);
			holder = new ViewHolder();
			holder.video_name_textview = (TextView) convertView.findViewById(R.id.title_text);
			holder.video_duration_textview = (TextView) convertView.findViewById(R.id.duration_text);
			holder.thumbnail_imageview = (AspectImageView) convertView.findViewById(R.id.thumbnail_image);
			holder.created_textview = (TextView)convertView.findViewById(R.id.created_text);
			convertView.setTag(holder);
        }
        else {
        	holder = (ViewHolder)convertView.getTag();
        }

        holder.id = video.videokey;
        holder.video_name_textview.setText(StringUtil.proper(video.title));
		holder.video_duration_textview.setText("Length " + video.duration);
		holder.created_textview.setText("Posted " + StringUtil.getDateString(video.date_added));
		
		//thumbnail images
		AQuery aq = this.image_manager.recycle(convertView);
		aq.id(holder.thumbnail_imageview).image(video.turl, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 0);     	
     	
		return convertView;
    }
}