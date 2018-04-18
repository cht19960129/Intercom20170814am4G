package com.nodepoint.residential;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    //重写onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Broadcast","------>start Broadcast<-------");
        Intent activityIntent = new Intent(context,com.nodepoint.residential.InitActivity.class);   //zhl20170320
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   //zhl20170320
        Log.v("Broadcast","------>start startActivity<-------");
        context.startActivity(activityIntent);   //zhl20170320
    }
}
