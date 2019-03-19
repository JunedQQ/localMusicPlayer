package com.jzx.localmusicplayer.musicinterface;

public interface MusicInterface {
	void play(int current);
	void continuePlay();
	void pause();
	void seekTo(int progress);
	void next();
	void last();
	int getcurrent();
	boolean getPlaying();
}
