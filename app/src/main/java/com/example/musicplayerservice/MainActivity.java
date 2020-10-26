package com.example.musicplayerservice;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static SeekBar sb;
    private static TextView tv_progress,tv_total;
    private ObjectAnimator animator;
    private MusicService.MusicControl musicControl;
//    MusicServiceConn conn;
    Intent intent;
    //记录服务是否被解绑
    private boolean isUnbind = false;

    //定义自定义
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicControl = (MusicService.MusicControl)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        //绑定服务
        intent = new Intent(MainActivity.this,MusicService.class);
        bindService(intent,conn,BIND_AUTO_CREATE);
    }

    private void init() {
        tv_progress = findViewById(R.id.tv_progress);
        tv_total = findViewById(R.id.tv_total);
        sb = findViewById(R.id.sb);
        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_pause).setOnClickListener(this);
        findViewById(R.id.btn_continue_play).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.sb).setOnClickListener(this);

        //创建意图对象
        intent = new Intent(this,MusicService.class);
        //创建服务连接对象
        conn = new MusicServiceConn();
        //绑定服务
        bindService(intent,conn,BIND_AUTO_CREATE);
        //为滑动条添加事件监听
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            //滑动条进度改变时，调用此方法
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //当滑动条滑到末端时，结束动画
                if (progress == seekBar.getMax()){
                    //停止播放动画
                    animator.pause();
                }
            }
            //滑动条开始滑动时，调用此方法
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            //滑动条停止滑动时，调用此方法
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //根据拖动的进度改变音乐的播放进度
                //获取seekBar的进度
                int progress = seekBar.getProgress();
                //改变 播放进度
                musicControl.seekTo(progress);
            }
        });
        //获取 圆形音乐图片控件
        ImageView iv_music = findViewById(R.id.iv_music);
        //设置该图片控件为顺时针 360°旋转 (rotation表示设置为旋转动画)
        animator = ObjectAnimator.ofFloat(iv_music,"rotation",0f,360.0f);
        //动画旋转一周的时间为1秒
        animator.setDuration(10000);
        //设置动画匀速旋转
        animator.setInterpolator(new LinearInterpolator());
        // -1 表示 设置动画无限循环
        animator.setRepeatCount(-1);
    }

    //创建消息处理器对象
    //定义自定义Handler对象处理歌曲时间显示的消息
    public static Handler handler = new Handler(Looper.getMainLooper()){
        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg){
            int duration = msg.arg1;
            int currentPos = msg.arg2;
            sb.setMax(duration);
            sb.setProgress(currentPos);
            //获取从子线程发送过来的音乐播放进度
//            Bundle bundle = msg.getData();
            //音乐 总时长
//            int duration = bundle.getInt("duration");
//            //音乐 当前播放进度
//            int currentPosition = bundle.getInt("currentPosition");
            //设置进度条 的最大值为 音乐总时长
//            sb.setMax(duration);
//            //设置进度条 当前的进度位置
//            sb.setProgress(currentPosition);
            //音乐总时长
            int minute = duration/1000/60;
            int second = duration/1000%60;
//            String strMinute = null;
//            String strSecond= null;
            //如果 音乐的时间中的分钟小于10
//            if (minute<10){
//                //在分钟的前面加一个0
//                strMinute = "0"+minute;
//            }else {
//                strMinute = minute+"";
//            }
//            //如果 音乐的时间中的秒钟小于10
//            if (second<10){
//                //在秒钟的前面加一个0
//                strSecond = "0"+second;
//            }else {
//                strSecond = second+"";
//            }
            tv_total.setText(minute+":"+second);
            //音乐 当前播放时长
            minute = currentPos/1000/60;
            second = currentPos/1000%60;
            //如果 音乐的时间中的分钟小于10
//            if (minute<10){
//                //在分钟的前面加一个0
//                strMinute = "0"+minute;
//            }else {
//                strMinute = minute+"";
//            }
            //如果 音乐的时间中的秒钟小于10
//            if (second<10){
//                //在秒钟的前面加一个0
//                strSecond = "0"+second;
//            }else {
//                strSecond = second+"";
//            }
            tv_progress.setText(minute+":"+second);
        }
    };

    //用于实现连接服务
    private class MusicServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicControl = (MusicService.MusicControl)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    private void unbind(boolean isUnbind){
        //判断服务是否被解绑
        if (!isUnbind){
            //暂停播放音乐
            musicControl.pausePlay();
            //解绑服务
            unbindService(conn);
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //播放音乐点击事件
            case R.id.btn_play:
                //播放音乐
                musicControl.play();
                //播放动画
                animator.start();
                break;

            //继续播放音乐点击事件
            case R.id.btn_continue_play:
                //继续播放音乐
                musicControl.continuePlay();
                //播放动画
                animator.start();
                break;

            //暂停音乐点击事件
            case R.id.btn_pause:
                //暂停播放音乐
                musicControl.pausePlay();
                //暂停播放动画
                animator.pause();
                break;

            //退出按钮点击事件
            case R.id.btn_exit:
                unbindService(conn);
                stopService(intent);
                //关闭音乐播放器界面
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑服务
        unbind(isUnbind);
    }



}