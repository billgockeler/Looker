package com.kogeto.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.H264TrackImpl;
import com.kogeto.looker.db.VideosDataSource;
import com.kogeto.looker.model.Video;
import com.kogeto.looker.util.Constants;

public class CreateMovieTask extends AsyncTask<Void, Void, Video> {
	
	private static final String TAG = "CreateMovieTask";
	private ProgressDialog progress_dialog;		
	private Context m_context;
	private CreateMovieTaskListener m_listener;
	
	public CreateMovieTask(Context context, CreateMovieTaskListener listener){
		m_context = context;
		m_listener = listener;
	}
	
	
	
	protected void onPreExecute(){
		progress_dialog = new ProgressDialog(m_context, ProgressDialog.THEME_HOLO_LIGHT);
    	progress_dialog.setMessage("Saving...");
    	progress_dialog.setInverseBackgroundForced(true);
    	progress_dialog.show();			
	}

	
	
	protected Video doInBackground(Void... voids) {
	
		try{
			//mux the audio and video into an mp4
			
			File video_file = new File(Environment.getExternalStorageDirectory() + "/kogeto.h264");
	        H264TrackImpl h264Track = new H264TrackImpl(new FileInputStream(video_file).getChannel(), "eng", 15000, 1001);
	        long stop = System.currentTimeMillis();
	        AACTrackImpl aacTrack = new AACTrackImpl(new FileInputStream(Environment.getExternalStorageDirectory() + "/kogeto.aac").getChannel());
            Movie movie = new Movie();
            movie.addTrack(h264Track);
            movie.addTrack(aacTrack);
            
            long time = System.currentTimeMillis();
            
            String video_filename = Environment.getExternalStorageDirectory() + "/kogeto-" + time + ".mp4";
    
            {
                DefaultMp4Builder mp4_builder = new DefaultMp4Builder();
                Container container = mp4_builder.build(movie);
                FileOutputStream fos = new FileOutputStream(new File(video_filename));
                container.writeContainer(fos.getChannel());
	            fos.close();
            }
                           
            //create a thumbnail from the video
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(video_filename, MediaStore.Images.Thumbnails.MINI_KIND);
            
            //the thumbnail is basically just the first frame from the video, which is not the right size
            //so we need to get a cropped thumbnail of the correct size 
            int target_width = 137;
            int target_height = 55;
            
            int center_x = (thumbnail.getWidth()/2)-(target_width/2);
            int center_y = (thumbnail.getHeight()/2)-(target_height/2);
            		
            thumbnail = Bitmap.createBitmap(thumbnail, center_x, center_y, target_width, target_height);
            
            String thumbnail_filename = Environment.getExternalStorageDirectory() + "/kogeto-" + time + ".jpg";
            
            try {
                FileOutputStream out = new FileOutputStream(thumbnail_filename);
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
            } 
            catch (Exception e) {
                e.printStackTrace();
            }

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(':');
            DecimalFormat duration_format = new DecimalFormat("00.00", symbols);
           
    		SimpleDateFormat time_format = new SimpleDateFormat("yyyy-MM-dd, HH:mm");	

    		Date now = new Date();
    		
    		Video video = new Video();
            video.title = time_format.format(now);
            video.description = "Blank description that will serve as a placeholder until the user enters a description";
            video.date_added = Constants.LOOKER.SERVER_DATE_FORMAT.format(now);
            video.duration = duration_format.format(((float)movie.getTimescale())/100000);
            video.vurl = video_filename;
            video.turl = thumbnail_filename;
            
            VideosDataSource datasource = new VideosDataSource(m_context);
            datasource.open();
            datasource.insert(video);
            datasource.close();
            
	        return video;
		}
		catch(Exception e){
			Log.e(TAG, "error saving: " + e.getMessage());
			progress_dialog.dismiss();
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	protected void onPostExecute(Video video) {
		progress_dialog.dismiss();

//		if(video != null){
//			Toast.makeText(m_context, "Video Saved", Toast.LENGTH_SHORT).show();
//		}
//		else{
//			Toast.makeText(m_context, "Video Not Saved", Toast.LENGTH_LONG).show();
//		}
//		
		if(m_listener != null){
			m_listener.finished(video);
		}
	}
	
	
	
	public interface CreateMovieTaskListener {
		public void finished(Video video);
	}
	
}

