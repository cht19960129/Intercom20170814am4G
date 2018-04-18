package com.nodepoint.residential.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/3/6 0006.
 */

public class TimeToRestartService extends Service {
    private static final String TAG = "TimeToRestartService";
    private IBinder mBinder=new Binder();
    private Timer mTimer;
    private TimerTask mTimerTask;
    private Intent intent = new Intent("com.example.restart.RECEIVER");

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.e(TAG,"进入了TimeToRestartService");
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        String dateStr = sdf.format(new Date());
                        Log.e("重启时间",dateStr);
//                        if (dateStr.equals("17:32")||dateStr.equals("17:34")||dateStr.equals("17:36")){
                        if (dateStr.equals("03:00")){
                            sendBroadcast(intent);//发送广播
                            Log.e(TAG,"发送重启APP广播");
                        }
                    }
                }).start();
            }
        };

        mTimer.schedule(mTimerTask,4000,59000);
        mTimer=null;
        mTimerTask=null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mTimerTask.cancel();
    }
}
