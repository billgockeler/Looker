package com.kogeto.looker;

import java.util.Hashtable;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kogeto.looker.camera.CameraActivity;
import com.kogeto.looker.featuredvideos.VideoListFragment;
import com.kogeto.looker.myvideos.MyVideoListFragment;
import com.kogeto.looker.myvideos.MyVideoListFragment.SelectionCountListener;
import com.kogeto.looker.widget.KogetoViewPager;


public class MainActivity extends FragmentActivity implements SelectionCountListener {

	private static final String TAG = "MainActivity";
	
	SectionsPagerAdapter sections_pager_adapter;
	KogetoViewPager view_pager;
	Button shoot_button;
	ToggleButton activity_toggle;
	ToggleButton share_toggle;
	TextView edit_button;
	TextView cancel_button;
	TextView delete_button;
	View footer;
	Hashtable page_map = new Hashtable();
	
	CameraActivity camera_fragment;
	
	boolean editing = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		shoot_button = (Button)findViewById(R.id.shoot_toggle);
		activity_toggle = (ToggleButton)findViewById(R.id.activity_toggle);
		share_toggle = (ToggleButton)findViewById(R.id.share_toggle);
		
		activity_toggle.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				setSelectedButton(0);
				view_pager.setCurrentItem(0);
			}
			
		});
		
		shoot_button.setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, CameraActivity.class);
				startActivity(intent);
			}
			
		});
		
		share_toggle.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				setSelectedButton(1);
				view_pager.setCurrentItem(1);
			}
			
		});
		
		// Create the adapter that will return a fragment for each of the three primary sections of the app.
		sections_pager_adapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		view_pager = (KogetoViewPager) findViewById(R.id.pager);
		view_pager.setAdapter(sections_pager_adapter);
		

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		view_pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			public void onPageSelected(int position) {
				setSelectedButton(position);
			}
		});
		
		edit_button = (TextView)findViewById(R.id.right_button);
		edit_button.setText("Edit");
		edit_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				edit();
			}
		});
		
		cancel_button = (TextView)findViewById(R.id.left_button);
		cancel_button.setText("Cancel");
		cancel_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				cancel();
			}
		});
		
		delete_button = (TextView)findViewById(R.id.delete_button);
		delete_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				delete();
			}
		});
		
		footer = findViewById(R.id.footer);
		
		this.setSelectedButton(0);
	}
	
	
	
	public void setSelectedButton(int position){
		
		switch(position){
			case 0:
				activity_toggle.setChecked(true);
				share_toggle.setChecked(false);
				edit_button.setVisibility(View.GONE);
				break;
			case 1:
				activity_toggle.setChecked(false);
				share_toggle.setChecked(true);
				edit_button.setVisibility(View.VISIBLE);
				break;
		}
	}
	
	
	
	
	@Override
	public void onBackPressed(){
		MyVideoListFragment video_list = (MyVideoListFragment) page_map.get(1);
		if(video_list != null && video_list.isEditing()){
			cancel();
		}
		else{
			super.onBackPressed();
		}
	}
	
	
	
	private void cancel(){
		view_pager.setPagingEnabled(true);
		
		MyVideoListFragment video_list = (MyVideoListFragment) page_map.get(1);
		video_list.setEditing(false);
		
		edit_button.setVisibility(View.VISIBLE);
		footer.setVisibility(View.VISIBLE);
		cancel_button.setVisibility(View.GONE);
		delete_button.setVisibility(View.GONE);
	}
	
	
	
	private void edit(){
		view_pager.setPagingEnabled(false);
		
		MyVideoListFragment video_list = (MyVideoListFragment) page_map.get(1);
		if(video_list != null){
			video_list.setEditing(true);
			video_list.setSelectionCountListener(MainActivity.this);
		}
		
		edit_button.setVisibility(View.GONE);
		footer.setVisibility(View.GONE);
		cancel_button.setVisibility(View.VISIBLE);
		delete_button.setVisibility(View.VISIBLE);
	}
	
	
	
	private void delete(){
		MyVideoListFragment video_list = (MyVideoListFragment) page_map.get(1);
		video_list.deleteSelectedVideos();
	}
	
	
	
	public void countChanged(int count){
		String text = (count == 0 ? "Delete" : (count == 1 ? "Delete 1 Video" : ("Delete " + count + " Videos")));
		delete_button.setText(text);
	}
	
	
	
	/////////////////// PAGER ADAPTER ////////////////////////
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}


		public Fragment getItem(int position) {
			Fragment fragment = null;
			
			switch(position){
				case 0:
					fragment = new VideoListFragment();
					break;
				case 1:
					fragment = new MyVideoListFragment();
					break;
			}
			
			page_map.put(position, fragment);
			
			return fragment;
		}


		
		public void destroyItem(ViewGroup container, int position, Object object) {
		    super.destroyItem(container, position, object);
		    page_map.remove(position);
		}

		
		
		public int getCount() {
			return 2;
		}
	}
	
}
