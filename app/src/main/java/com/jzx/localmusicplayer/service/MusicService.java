package com.jzx.localmusicplayer.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.jzx.localmusicplayer.MainActivity;
import com.jzx.localmusicplayer.entity.Music;
import com.jzx.localmusicplayer.musicinterface.MusicInterface;
import com.jzx.localmusicplayer.utils.SearchLocalMusicUtils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

public class MusicService extends Service{
	MediaPlayer musicPlayer;
	int current = 0;
	List<Music> musicList;
	private Timer timer;
	@Override
	public IBinder onBind(Intent intent) {
		return new MusicBinder();
	}
	
	class MusicBinder extends Binder implements MusicInterface {

		@Override
		public void play(int current) {
			MusicService.this.play(current);
		}

		@Override
		public void continuePlay() {
			MusicService.this.continuePlay();
		}

		@Override
		public void pause() {
			MusicService.this.pause();
		}

		@Override
		public void seekTo(int progress) {
			MusicService.this.seekTo(progress);
		}

		@Override
		public void next() {
			MusicService.this.next();
		}

		@Override
		public void last() {
			MusicService.this.last();
		}

		@Override
		public int getcurrent() {
			int current = MusicService.this.getCurrent();
			return current;
		}

		@Override
		public boolean getPlaying() {
			return MusicService.this.getPlaying();
		}
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		musicPlayer = new MediaPlayer();
		musicList = SearchLocalMusicUtils.getMusic();
	}	
	
	/**
	 * 播放音乐
	 */
	public void play(int current) {
		//重置mediaplayer
		musicPlayer.reset();
		try {
			this.current = current;
			//设置播放地址
			musicPlayer.setDataSource(musicList.get(current).getMusicPath());
			//准备播放
			musicPlayer.prepare();
			//开始播放
			musicPlayer.start();
			//调用定时器，发送进度
			timer();
			//发送信息设置音乐长度
			Message msg = MainActivity.handler.obtainMessage();
			msg.arg1 = musicPlayer.getDuration();
			msg.obj = musicList.get(current).getMusicName();
			msg.what = 3;
			MainActivity.handler.sendMessage(msg);
			//设置播放完当前歌曲调用的方法
			musicPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					next();
				}
			});
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 继续播放
	 */
	public void continuePlay() {
		musicPlayer.start();
	}
	
	/**
	 * 获取mediaplayer的播放状态
	 * @return
	 */
	public boolean getPlaying() {
		return musicPlayer.isPlaying();
	}
	
	/**
	 * 获取当前播放进度
	 * @return
	 */
	public int getCurrent() {
		return musicPlayer.getCurrentPosition();
	}
	
	/**
	 * 暂停播放
	 */
	public void pause() {
		musicPlayer.pause();
	}
	
	/**
	 * 设置进度
	 * @param progress
	 */
	public void seekTo(int progress) {
		musicPlayer.seekTo(progress);
	}
	
	/**
	 * 下一曲
	 */
	public void next() {
		if(current != musicList.size()-1) {
			play(current+1);
		}else {
			MainActivity.handler.sendEmptyMessage(1);
		}
	}
	
	/**
	 * 上一曲
	 */
	public void last() {
		if(current != 0) {
			play(current-1);
		}else {
			MainActivity.handler.sendEmptyMessage(2);
		}
	}
	
	/**
	 * 计时器,定时向mainActivity中handler传递播放进度
	 */
	public void timer() {
		if(timer == null) {
			//创建定时器
			timer = new Timer();
			//创建定时器的内容
			//第一个参数相当于Thread，开启一个线程，第二个参数是该方法调用后延迟多少毫秒进行发送，第三个参数是以后每多少毫秒调用一次该方法
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					//获取当前播放音乐的总长度和当前进度
					int max = musicPlayer.getDuration();
					int progress = musicPlayer.getCurrentPosition();
					//获取message对象
					Message msg = MainActivity.handler.obtainMessage();
					msg.what = 100;
					//创建bundle对象传递参数
					Bundle data = new Bundle();
					data.putInt("max", max);
					data.putInt("progress", progress);
					//传递内容
					msg.setData(data);
					MainActivity.handler.sendMessage(msg);
				}
			}, 0, 1000);
		}
		
	}
}
