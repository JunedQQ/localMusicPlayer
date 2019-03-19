package com.jzx.localmusicplayer.adapter;

import java.util.List;

import com.jzx.localmusicplayer.R;
import com.jzx.localmusicplayer.entity.Music;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicAdapter extends BaseAdapter{
	List<Music> musicList;
	Context context;
	
	public MusicAdapter(Context context,List<Music> list) {
		this.context = context;
		this.musicList = list;
	}
	@Override
	public int getCount() {
		return musicList.size();
	}

	@Override
	public Object getItem(int position) {
		return musicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = View.inflate(context, R.layout.adapter_musiclistview, null);
		}
		
		TextView text = convertView.findViewById(R.id.adatper_music_tv);
		
		text.setText(musicList.get(position).getMusicName());
		
		return convertView;
	}

}
