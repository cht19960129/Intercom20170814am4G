package com.example.building;

import android.app.Activity;

import jni.util.Utils;

/**
 * Created by simon on 2016/7/7.
 */
public class MBaseActivity extends Activity {
    static {
        try {
            Utils.PrintLog(5,"JNI", "try to load libbuilding.so");
            System.loadLibrary("building");
            // 加载本地库,也就是JNI生成的libxxx.so文件，下面再说。
        } catch (UnsatisfiedLinkError ule) {
            Utils.PrintLog(5,"JNI", "WARNING: Could not load libbuilding.so");
        }
    }

    // 检测锁状态
    public static native int openled();
    // 关闭锁
    public static native void closeled();
    // 开普通门锁
    public static native int ioctl(int led_num, int on_off);
    // 开继电器门锁
    public static native int ioct2(int led_num, int on_off);
    // 获取当前是白天还是黑夜
    public static native int getLightIntensityStatus();
    // 摄像头灯光的打开与关闭
    public static native int controlCamera(int led_num, int on_off);
    //获得IO端口的状态
    public static native int getGPIO10Status();
}
