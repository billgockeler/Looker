package com.kogeto.looker.camera;

import java.io.File;

import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.kogeto.looker.util.Settings;

public class AudioRecorder {
	
	private static final String TAG = "AndroidRecorder";

	MediaRecorder recorder = new MediaRecorder();
	
	String file_base;
	File output_file;
	
	public File getOutputFile(){
		return this.output_file;
	}

	
	public void stopRecording(){
		this.recorder.reset();
	}
	

	
	public void startRecording(){
		try {
			this.file_base = "CH" + Settings.Audio.DEFAULT.channels + "BR" + Settings.Audio.DEFAULT.bit_rate + "SR" + Settings.Audio.DEFAULT.sample_rate;
			String filename;

	        Log.d(TAG, "started recording");
	        
	        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

	        if (Build.VERSION.SDK_INT >= 10) {
	            recorder.setAudioSamplingRate(44100);
	            recorder.setAudioEncodingBitRate(192000);//64000, 96000, 192000
	            recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
	            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
	            filename = Environment.getExternalStorageDirectory() + "/" + "kogeto.aac";
	        } else {
	            // older version of Android, use crappy sounding voice codec
	            recorder.setAudioSamplingRate(8000);
	            recorder.setAudioEncodingBitRate(12200);
	            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	            filename = Environment.getExternalStorageDirectory() + "/" + "kogeto.3gp";
	        }

		    this.output_file = new File(filename);
			recorder.setOutputFile(filename);
			recorder.prepare();
			recorder.start(); 
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
}
