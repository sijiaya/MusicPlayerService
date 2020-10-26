package com.example.musicplayerservice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    //创建音乐播放器
    private MediaPlayer player;
    private Timer timer;

    public MusicService() {
    }

    //通过该接口，与前端交互
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl();
    }


    @Override
    public void onCreate(){
        super.onCreate();
        //创建音乐播放器对象
        player = new MediaPlayer();
    }


    //添加计时器 用于设置音乐播放器中的播放进度条
    public void addTimer(){
        if (timer == null){
            //创建计时器对象
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (player == null) return;
                    //获取歌曲总时长
                    int duration = player.getDuration();
                    //获取播放进度
                    int currentPosition = player.getCurrentPosition();
//                   //创建消息对象
                    Message msg = MainActivity.handler.obtainMessage();
                    msg.arg1 = duration;
                    msg.arg2 = currentPosition;
//                    //将音乐的总时长和播放进度封装到消息对象中
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("duration",duration);
//                    bundle.putInt("currentPosition",currentPosition);
//                    msg.setData(bundle);
                    //将消息发送到主线程的消息队列
                    MainActivity.handler.sendMessage(msg);
                }
            };
            //开始计时任务后的5毫秒，第一次执行task任务，以后没500毫秒执行一次
            timer.schedule(task,5,500);
        }
    }
    //实现音乐的播放、暂停、继续和seekBar进度条的拖动(progress的设置)
    class MusicControl extends Binder {
        //加载歌曲
        public void play(){
            try{
                //重置音乐播放器
                player.reset();
                //加载多媒体文件
                player = MediaPlayer.create(getApplicationContext(),R.raw.music);
                //播放音乐
                player.start();
                //添加计时器，更新seekBar的progress的位置
                addTimer();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public void pausePlay(){
            //暂停播放音乐
            if (player == null){
                return;
            }
            player.pause();
        }
        public void continuePlay(){
            if (player == null){
                return;
            }
            //继续播放音乐
            player.start();
        }
        public void seekTo(int progress){
            if (player == null){
                return;
            }
            //设置音乐的播放位置
            player.seekTo(progress);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player == null)return;
        //停止播放音乐
        if (player.isPlaying()) player.stop();
        //释放占用资源
        player.release();
        //将player置为空
        player = null;
    }
}
