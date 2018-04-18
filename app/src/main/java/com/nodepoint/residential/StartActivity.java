package com.nodepoint.residential;

import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);   //zhl20170320
        //setContentView(R.layout.activity_start);  //zhl20170320

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //public void onReboot(View v){
    public void onReboot(){
        try {
            Runtime.getRuntime().exec("su -c \"/system/bin/shutdown\"");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
