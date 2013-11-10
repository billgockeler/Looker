package com.kogeto.looker.myvideos;

import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.kogeto.looker.R;
import com.kogeto.looker.db.VideosDataSource;
import com.kogeto.looker.model.Video;
import com.kogeto.looker.widget.MessageDialog;

public class MyVideoListFragment extends ListFragment {
	
	private static final String TAG = "VideoListFragment";
	private MyVideoListAdapter video_list_adapter;
	private ArrayList<Video> video_array;
	private ProgressBar progress_bar;
	private SelectionCountListener count_listener;
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
    	this.video_array = new ArrayList<Video>();
    	this.video_list_adapter = new MyVideoListAdapter(getActivity(), R.layout.video_list_row, video_array);
    	setListAdapter(this.video_list_adapter);
		
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
		
		new MyVideosTask().execute();
	}
	

	
	public void refresh(){
		new MyVideosTask().execute();
	}
	
	
	
	public boolean isEditing(){
		return ((MyVideoListAdapter)this.getListAdapter()).isEditing();
	}
	
	
	public void setEditing(boolean editing){
		((MyVideoListAdapter)this.getListAdapter()).setEditing(editing);
	}
	
	
	
	public void setSelectionCountListener(SelectionCountListener l){
		this.video_list_adapter.setSelectionCountListener(l);
	}
	
	
	
	public void deleteSelectedVideos(){
		final int[] selected_videos = this.video_list_adapter.getSelected();
		
		if(selected_videos.length < 1){
			return;
		}
		
		
		final MessageDialog dialog = new MessageDialog(this.getActivity(), "Delete?", "Are you sure you want to delete " + (selected_videos.length > 1 ? (selected_videos.length + " videos") : " the video?"));

		dialog.show();

		dialog.setPositiveListener(new View.OnClickListener() {
			public void onClick(View v) {
				VideosDataSource datasource = new VideosDataSource(getActivity());
				datasource.open();
				datasource.delete(selected_videos);
				datasource.close();	
				new MyVideosTask().execute();
				dialog.cancel();
			}
		});
		
		dialog.setNegativeListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.cancel();
			}
		});		
	}
	
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Video video = video_array.get(position);
    	Bundle bundle = new Bundle();
    	bundle.putParcelable(VideoDetailsActivity.VIDEO_OBJECT, video);
		Intent video_details_intent = new Intent(getActivity().getApplicationContext(), VideoDetailsActivity.class);
		video_details_intent.putExtras(bundle);
        startActivity(video_details_intent);
	}
	
	
	
	
	//////////////// SELECTION LISTENER INTERFACE //////////////////////////
	public interface SelectionCountListener{
		public void countChanged(int count);
	}
	
	
	
	
	
    ///////////////////////////// MY VIDEOS TASK //////////////////////////// 
	private class MyVideosTask extends AsyncTask<Void, Void, ArrayList<Video>> {

		private static final String TAG = "MyVideosTask";
		
		
		
		protected void onPreExecute(){
	    	video_list_adapter.clear();
	     	
			if(progress_bar != null){
				progress_bar.setVisibility(View.VISIBLE);
			}
		}
		
		
		
		protected ArrayList<Video> doInBackground(Void... voids) {
			
			VideosDataSource datasource = new VideosDataSource(getActivity());
			
			datasource.open();
			ArrayList<Video> videos = datasource.getAllVideos();
			datasource.close();
			
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
