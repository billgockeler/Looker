package com.kogeto.looker.myvideos;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.androidquery.AQuery;
import com.kogeto.looker.R;
import com.kogeto.looker.model.Video;
import com.kogeto.looker.myvideos.MyVideoListFragment.SelectionCountListener;
import com.kogeto.looker.player.VideoPlayerActivity;
import com.kogeto.looker.util.StringUtil;
import com.kogeto.looker.widget.AspectImageView;


public class MyVideoListAdapter extends ArrayAdapter {

	private final static String TAG = "MyVideoListAdapter";
	
	private LayoutInflater m_inflater;
    private AQuery m_image_manager;
    private Context m_context;
	private ArrayList<Integer> m_selected_videos = new ArrayList<Integer>();
	private SelectionCountListener m_count_listener;

    private boolean m_editing = false;

	public MyVideoListAdapter(Activity context, int resource, ArrayList<Video> all_videos) {
		super(context, resource, all_videos);
		m_context  = context;
		m_inflater = LayoutInflater.from(context);
		m_image_manager = new AQuery(context);
	}

	
	
	static class ViewHolder {
		String id;
		TextView video_name_textview;
		TextView video_duration_textview;
		TextView created_textview;
		ImageView play_button;
		ToggleButton delete_toggle;
		AspectImageView thumbnail_imageview;
	}

	
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		//Get the current cause object
        final Video video = (Video) getItem(position);

        //Inflate the view
        if(convertView == null) {
            convertView = m_inflater.inflate(R.layout.video_list_row, null);
			holder = new ViewHolder();
			holder.video_name_textview = (TextView) convertView.findViewById(R.id.title_text);
			holder.video_duration_textview = (TextView) convertView.findViewById(R.id.duration_text);
			holder.thumbnail_imageview = (AspectImageView) convertView.findViewById(R.id.thumbnail_image);
			holder.created_textview = (TextView)convertView.findViewById(R.id.created_text);
			holder.delete_toggle = (ToggleButton)convertView.findViewById(R.id.delete_toggle);
			holder.play_button = (ImageView)convertView.findViewById(R.id.play_image);
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
		AQuery aq = m_image_manager.recycle(convertView);
		aq.id(holder.thumbnail_imageview).image(video.turl, true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 0);     	
     	
		if(!m_editing){
			holder.delete_toggle.setVisibility(View.INVISIBLE);
			holder.play_button.setVisibility(View.VISIBLE);
			holder.play_button.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
			    	Bundle bundle = new Bundle();
			    	bundle.putString(VideoPlayerActivity.DATASOURCE, video.vurl);
					Intent video_player_intent = new Intent(m_context, VideoPlayerActivity.class);
					video_player_intent.putExtras(bundle);
			        m_context.startActivity(video_player_intent);					
				}
			});
		}
		else{
			holder.play_button.setVisibility(View.INVISIBLE);
			holder.delete_toggle.setVisibility(View.VISIBLE);
			holder.delete_toggle.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					selected(Integer.valueOf(video.id), isChecked);				
				}
			});
			
	    	if(m_selected_videos.contains(Integer.valueOf(video.id))){
	    		holder.delete_toggle.setChecked(true);
	    	}
	    	else{
	    		holder.delete_toggle.setChecked(false);
	    	}
		}
		
		
		return convertView;
    }
	
	
	
	public void setEditing(boolean editing){
		m_editing = editing;
		notifyDataSetChanged();
	}
	
	
	
	public boolean isEditing(){
		return m_editing;
	}
	
	
	public void notifyDataSetChanged(){
		super.notifyDataSetChanged();
		m_selected_videos.clear();
		
		if(m_count_listener != null){
			m_count_listener.countChanged(m_selected_videos.size());
		}
	}
	

	
	private void selected(Integer video_id, boolean is_checked){
		if(is_checked){
			m_selected_videos.add(video_id);
		}
		else {
			m_selected_videos.remove(video_id);
		}
		
		if(m_count_listener != null){
			m_count_listener.countChanged(m_selected_videos.size());
		}
	}
	
  		
	public int[] getSelected(){
	     int count = m_selected_videos.size();
	     int[] selected_videos = new int[count];
	     
	     for(int i = 0; i < count; i++){
	    	 selected_videos[i] = m_selected_videos.get(i);
	     }
	     
	     return selected_videos;
	}
	
	
	public void setSelectionCountListener(SelectionCountListener l){
		m_count_listener = l;
	}
	

}