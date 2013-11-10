package com.kogeto.looker.featuredvideos;

import java.lang.reflect.Type;
import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.kogeto.looker.R;
import com.kogeto.looker.model.Video;
import com.kogeto.looker.player.VideoPlayerActivity;
import com.kogeto.looker.util.ArrayAdapterFactory;
import com.kogeto.looker.util.Constants;
import com.kogeto.looker.util.WebServices;

public class VideoListFragment extends ListFragment {
	
	private static final String TAG = "VideoListFragment";
	private VideoListAdapter video_list_adapter;
	private ArrayList<Video> video_array;
	private ProgressBar progress_bar;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
    	this.video_array = new ArrayList<Video>();
    	this.video_list_adapter = new VideoListAdapter(getActivity(), R.layout.video_list_row, video_array);
    	setListAdapter(this.video_list_adapter);
		
		new FeaturedVideosTask().execute();		
	}
	

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_video_list, container, false);
		this.progress_bar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
		return rootView;
	}


	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Video video = video_array.get(position);
    	Bundle bundle = new Bundle();
    	bundle.putString(VideoPlayerActivity.DATASOURCE, video.vurl);
		Intent video_player_intent = new Intent(getActivity().getApplicationContext(), VideoPlayerActivity.class);
		video_player_intent.putExtras(bundle);
        startActivity(video_player_intent);
	}
	
	
	
	public void search(String keyword){
		new SearchVideosTask().execute(keyword);
	}
	
	
	
    ///////////////////////////// SEARCH VIDEOS TASK //////////////////////////// 
	private class SearchVideosTask extends AsyncTask<String, Void, ArrayList<Video>> {

		private static final String TAG = "SearchVideosTask";
		
		
		
		protected void onPreExecute(){
	    	video_list_adapter.clear();
	     	
			if(progress_bar != null){
				progress_bar.setVisibility(View.VISIBLE);
			}
		}
		
		
		
		protected ArrayList<Video> doInBackground(String... keywords) {
			
    		String host = Constants.WEB_SERVICES.HOST;
    		String endpoint = Constants.WEB_SERVICES.SEARCH_VIDEOS;
    		String keyword = null;
    		
    		if(keywords != null && keywords[0] != null){
    			keyword = keywords[0].trim().replace(" ", "%20");
        		endpoint = endpoint.replace("{keyword}", keyword);
    		}

     		String url = host + endpoint;

    		ArrayList<Video> videos = null;
	    	String json_string = new WebServices().get(getActivity(), url);
	    		    	
		    if(json_string != null){
		    	try{
		    		
		    		JsonParser parser = new JsonParser();
		    		JsonElement root = parser.parse(json_string);
		    		JsonObject object = root.getAsJsonObject();
		    		JsonArray array = object.getAsJsonArray("videos");
		    		
		    		
		    		Gson gson = new GsonBuilder()
		    			.registerTypeAdapterFactory(new ArrayAdapterFactory())
		    			.setDateFormat(Constants.LOOKER.SERVER_DATE_FORMAT.toPattern()).create();
		    		
	    			Type list_type = new TypeToken<ArrayList<Video>>(){}.getType();
	    			
	    			videos = gson.fromJson(array, list_type);
		         }
		    	catch(Exception e) {
		         	 Log.e(TAG, "Error parsing data: " + e.toString());
		        }    
		    }
		    		    
		    return videos;
		}

		
	    protected void onPostExecute(ArrayList<Video> videos) {

	        if(videos != null && videos.size() != 0){
		    	for(Video video : videos) {
					video_array.add(video);
		        }

		    	video_list_adapter.notifyDataSetChanged();
	        }
	        
			if(progress_bar != null){
				progress_bar.setVisibility(View.INVISIBLE);
			}

	    }
	    
	}

	
    ///////////////////////////// FEATURED VIDEOS TASK //////////////////////////// 
	private class FeaturedVideosTask extends AsyncTask<Void, Void, ArrayList<Video>> {

		private static final String TAG = "FeaturedVideosTask";
		
		
		
		protected void onPreExecute(){
	    	video_list_adapter.clear();
	     	
			if(progress_bar != null){
				progress_bar.setVisibility(View.VISIBLE);
			}
		}
		
		
		
		protected ArrayList<Video> doInBackground(Void... voids) {
			
    		String host = Constants.WEB_SERVICES.HOST;
    		String endpoint = Constants.WEB_SERVICES.FEATURED_VIDEOS;
     		String url = host + endpoint;

    		ArrayList<Video> videos = null;
	    	String json_string = new WebServices().get(getActivity(), url);
	    		    	
		    if(json_string != null){
		    	try{
		    		
		    		JsonParser parser = new JsonParser();
		    		JsonElement root = parser.parse(json_string);
		    		JsonObject object = root.getAsJsonObject();
		    		JsonArray array = object.getAsJsonArray("videos");
		    		
		    		
		    		Gson gson = new GsonBuilder()
		    			.registerTypeAdapterFactory(new ArrayAdapterFactory())
		    			.setDateFormat(Constants.LOOKER.SERVER_DATE_FORMAT.toPattern()).create();
		    		
	    			Type list_type = new TypeToken<ArrayList<Video>>(){}.getType();
	    			
	    			videos = gson.fromJson(array, list_type);
		         }
		    	catch(Exception e) {
		         	 Log.e(TAG, "Error parsing data: " + e.toString());
		        }    
		    }
		    		    
		    return videos;
		}

		
	    protected void onPostExecute(ArrayList<Video> videos) {

	        if(videos != null && videos.size() != 0){
		    	for(Video video : videos) {
					video_array.add(video);
		        }

		    	video_list_adapter.notifyDataSetChanged();
	        }
	        
			if(progress_bar != null){
				progress_bar.setVisibility(View.INVISIBLE);
			}

	    }
	    
	}

	

}
