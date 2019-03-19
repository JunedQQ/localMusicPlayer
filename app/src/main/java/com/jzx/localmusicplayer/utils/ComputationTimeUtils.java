package com.jzx.localmusicplayer.utils;

public class ComputationTimeUtils {
	public static String ComputationTime(int millisecond) {
		int minute = millisecond/60000;
		int second =(millisecond/1000) % 60;
		String maxProgress = minute+"";
		String secondProgress = second+"";
		if(minute < 10) {
			maxProgress = "0"+minute;
		}
		if(second < 10) {
			secondProgress = "0"+second;
		}
		return maxProgress+":"+secondProgress;
	}
}
