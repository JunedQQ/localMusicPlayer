package com.jzx.localmusicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jzx.localmusicplayer.adapter.MusicAdapter;
import com.jzx.localmusicplayer.application.MyApplication;
import com.jzx.localmusicplayer.entity.Music;
import com.jzx.localmusicplayer.musicinterface.MusicInterface;
import com.jzx.localmusicplayer.service.MusicService;
import com.jzx.localmusicplayer.utils.ComputationTimeUtils;
import com.jzx.localmusicplayer.utils.SearchLocalMusicUtils;

import java.nio.file.Files;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static ListView musicListView;
    private static SeekBar musicSeekBar;
    public static List<Music> musicList;
    public static ImageView loading;
    public static AnimationDrawable loadingDrawable;
    public static TextView loadingTextView;
    static TextView seekBarMax,seekBarCurrent,musicName;
    static int currentMusic = -1; // 记录当前播放的音乐的下标,-1为未播放过
    static MusicInterface music;
    public static Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 200:
                    // 给listview设置适配器
                    MusicAdapter musicAdapter = new MusicAdapter(MyApplication.getContext(), musicList);
                    musicListView.setAdapter(musicAdapter);
                    setListViewListener();
                    musicListView.setVisibility(View.VISIBLE);
                    loadingTextView.setVisibility(View.GONE);
                    loadingDrawable.stop();
                    loading.setVisibility(View.GONE);
                    break;
                case 100:
                    Bundle data = msg.getData();
                    int max = data.getInt("max");
                    int progress = data.getInt("progress");
                    musicSeekBar.setProgress(progress);
                    musicSeekBar.setMax(max);
                    seekBarCurrent.setText(ComputationTimeUtils.ComputationTime(progress));
                    break;
                case 3:
                    int maxPro = msg.arg1;
                    seekBarMax.setText(ComputationTimeUtils.ComputationTime(maxPro));
                    musicName.setText("正在播放:"+msg.obj);

                    break;

                case 1:
                    Toast.makeText(MyApplication.getContext(), "已经是最后一首了!", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(MyApplication.getContext(), "已经是第一首了!", Toast.LENGTH_SHORT).show();
                    break;
                case 888:
                    loadingTextView.setText((String)msg.obj);
                    break;
                default:
                    break;
            }
        }

    };
    private Intent intent;
    private MusicConnection conn;

    /**
     * 设置listview的item点击事件监听
     */
    private static void setListViewListener() {
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentMusic = position;
                music.play(position);
            }
        });
    };

    /**
     * music服务连接对象，获取可控制service的对象
     *
     * @author JunedQQ
     *
     */
    class MusicConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            music = (MusicInterface) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        // 启动服务
        startService(intent);
        // 绑定服务
        bindService(intent, conn, BIND_AUTO_CREATE);

        setSeekBarListener();
    }

    /**
     * 加载控件id
     */
    private void initView() {
        musicListView = findViewById(R.id.music_listview);
        musicSeekBar = findViewById(R.id.music_seekbar);
        intent = new Intent(MyApplication.getContext(), MusicService.class);
        conn = new MusicConnection();
        seekBarMax = findViewById(R.id.maxprogress);
        seekBarCurrent = findViewById(R.id.current);
        musicName = findViewById(R.id.music_name);
        loading = findViewById(R.id.loading);
        loadingTextView = findViewById(R.id.loading_textView);

        //配置加载动画
        loading.setBackgroundResource(R.drawable.loading);
        loadingDrawable = (AnimationDrawable) loading.getBackground();
    }

    /**
     * 设置seekbar的拖动事件监听
     */
    private void setSeekBarListener() {
        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                music.seekTo(seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
        });
    }
    /**
     * 上一曲
     *
     * @param view
     */
    public void lastMusic(View view) {
        music.last();
    }

    /**
     * 播放暂停
     *
     * @param view
     */
    public void play(View view) {
        // 如果没有播放过音乐，点击播放按钮默认播放第一曲歌
        if (musicList == null) {
            //判断是否有可播放的音乐
            Toast.makeText(this, "没有歌曲可以播放!", Toast.LENGTH_SHORT).show();
        } else if (currentMusic == -1) {
            music.play(0);
            currentMusic = 0;
        } else if (music.getPlaying()) {
            // 判断音乐是否在播放，如果在播放则暂停播放
            music.pause();
        } else if (music.getcurrent() != 0) {
            // 判断是否有播放进度，有播放进度就继续播放
            music.continuePlay();
        }
    }

    /**
     * 下一曲
     *
     * @param view
     */
    public void nextMusic(View view) {
        music.next();
    }

    // 创建optionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "扫描本地音乐");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                //隐藏listview控件
                musicListView.setVisibility(View.INVISIBLE);
                loading.setVisibility(View.VISIBLE);
                loadingTextView.setVisibility(View.VISIBLE);
                //显示动画
                loadingDrawable.start();
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    scan();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 开启分线程进行本地音乐扫描
     */
    public void scan(){
        // 开启子线程来扫描本地音乐文件
        new Thread() {
            public void run() {
                //重置扫描工具类
                SearchLocalMusicUtils.clear();
                musicList = SearchLocalMusicUtils.scan(Environment.getExternalStorageDirectory());
                handler.sendEmptyMessage(200);
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case  1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&grantResults[1] ==PackageManager.PERMISSION_GRANTED ){
                    scan();
                }else{
                    Toast.makeText(MyApplication.getContext(), "你拒绝了此权限，无法进行本地音乐扫描!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
