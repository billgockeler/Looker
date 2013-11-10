package com.kogeto.looker.util;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.util.Log;

public class MediaUtil {

	private static final String TAG = "MediaUtil";
	
	
	private static void listCodecs(){
		int count = MediaCodecList.getCodecCount();
		
		Log.d(TAG, "number of codecs is: " + count);
		
		for(int i = 0; i < count; i++){
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			String name = info.getName();
			String[] types = info.getSupportedTypes();
			for(int ii = 0; ii < types.length; ii++){
				Log.d(TAG, "Codec name: " + name + ", supported type: " + types[ii] + ", is encoder: " + info.isEncoder());
				MediaCodecInfo.CodecCapabilities c = info.getCapabilitiesForType(types[ii]);
				int[] formats = c.colorFormats;
				for(int a = 0; a < types.length; a++){
					Log.d(TAG, "Codec name: " + name + ", supported type: " + types[ii].toString() + ",  supported color format: " + formats[a]);
				}
				
				CodecProfileLevel[] level = c.profileLevels;
				for(int b = 0; b < types.length; b++){
					int l = level[b].level;
					int p = level[b].profile;
					Log.d(TAG, "Codec name: " + name + ", supported type: " + types[ii].toString() + ",  level: " + level[b].level + ", profile: " + level[b].profile);
				}
			}
		}
	}
}
