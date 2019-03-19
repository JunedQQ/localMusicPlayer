package com.jzx.localmusicplayer.utils;

import android.os.Message;

import com.jzx.localmusicplayer.MainActivity;
import com.jzx.localmusicplayer.entity.Music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SearchLocalMusicUtils {
	private static List<Music> musicList = new ArrayList<Music>();

	public static List<Music> scan(File file) {
		File[] childFile = file.listFiles();
		for (File mFile : childFile) {
			if (mFile.isDirectory()) {
				scan(mFile);
			} else if (mFile.isFile()) {
				String fileName = getFileName(mFile.getAbsolutePath());
				if (fileName.endsWith(".mp3") && mFile.length() > 1024*1024) {
					Music music = new Music();
					music.setMusicName(fileName);
					music.setMusicPath(mFile.getAbsolutePath());
					//获取消息对象
					Message msg = MainActivity.handler.obtainMessage();
					msg.what = 888;
					msg.obj = mFile.getAbsolutePath();
					//发送消息
					MainActivity.handler.sendMessage(msg);
					musicList.add(music);
				}
			}
		}
		return musicList;
	}
	
	public static List<Music> getMusic(){
		return musicList;
	}
	
	public static void clear() {
		musicList.clear();
	}
	
	public static String getFileName(String path) {
		return path.substring(path.lastIndexOf("/") + 1);
	}

}
