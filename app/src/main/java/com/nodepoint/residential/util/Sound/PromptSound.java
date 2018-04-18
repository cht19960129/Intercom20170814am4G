package com.nodepoint.residential.util.Sound;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.nodepoint.residential.R;

/**
 * Created by think on 2017/9/20.
 */

public class PromptSound extends Activity {
    //开门提示语音
    protected  void openlockAudio(){
        try {
            //MediaPlayer mediaPlayer= MediaPlayer.create(this, R.raw.openlock);
            MediaPlayer mediaPlayer= MediaPlayer.create(this, R.raw.openlock);
            mediaPlayer.start();
            this.finish();
        }catch (Exception e){}

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openlockAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
