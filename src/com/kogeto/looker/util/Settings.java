package com.kogeto.looker.util;

public class Settings {

	
	
	public static class Audio {
		public static final Audio DEFAULT = new Audio(2, 44100, 7000);
		
		public int channels, sample_rate, bit_rate;

		public Audio(int channels, int sample_rate, int bit_rate){
			this.channels = channels;
			this.sample_rate = sample_rate;
			this.bit_rate = bit_rate;
		}
	}
	
	
	
	public static class Camera {
		
		public static final Camera QVGA = new Camera("QVGA", 320, 240, 250000, 15);
		public static final Camera VGA = new Camera("VGA", 640, 480, 500000, 15);
		public static final Camera HD_720 = new Camera("HD_720", 1280, 720, 2000000, 15);
		public static final Camera HD_1080 = new Camera("HD_1920", 1920, 1080, 2000000, 15);
		public static final Camera CURRENT = HD_720;
		
		public String name;
		public int width, height, bit_rate, frame_rate;
		
		public Camera(String name, int width, int height, int bit_rate, int frame_rate){
			this.name = name;
			this.width = width;
			this.height = height;
			this.bit_rate = bit_rate;
			this.frame_rate = frame_rate;
		}
	}
	
	
	public static class Video {
		
		public static final Video HD_720 = new Video("HD_720", 896, 144, 2000000, 15);
		public static final Video HD_1080 = new Video("HD_1080", 1792, 288, 2000000, 15);
		public static final Video CURRENT = HD_1080;
		
		public String name;
		public int width, height, bit_rate, frame_rate;
		
		public Video(String name, int width, int height, int bit_rate, int frame_rate){
			this.name = name;
			this.width = width;
			this.height = height;
			this.bit_rate = bit_rate;
			this.frame_rate = frame_rate;
		}
	}
	
}
