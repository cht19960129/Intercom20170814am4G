package com.nodepoint.residential;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nodepoint.residential.config.DeviceConfig;
import com.nodepoint.residential.service.MainService;
import com.nodepoint.residential.util.SqlUtil;

import org.json.JSONObject;

import java.util.List;

import jni.util.Utils;

public class InitActivity extends Activity {
    public static final int MSG_NO_MAC_ADDRESS=30001;
    public static final int MSG_GET_MAC_ADDRESS=30002;
    public static final int MSG_LOGIN=30003;
    public static final int MSG_GET_TOKEN=30004;
    public static final int MSG_CANNOT_GET_TOKEN=30005;
    public static final int MSG_RTC_REGISTER=30006;
    public static final int MSG_RTC_CANNOT_REGISTER=30007;
    public static final int MSG_LOGIN_ERROR=30008;
    public static final int MSG_NO_NETWORK=30009;
    public static final int MSG_CONNECT_SUCCESS=30010;
    public static final int MSG_CONNECT_FAIL=30011;
    public static final int MSG_WIFI_LIST=30012;
    public static final int MSG_WIFI_CONNECTED=30013;
    public static final int MSG_WIFI_CONNECT_FAIL=30014;
    public static final int MSG_INIT_RFID=30015;
    public static final int MSG_INIT_ASSEMBLE=30016;
    public static final int MSG_INIT_SQL=30017;

    LinearLayout mainLayout;
    protected Messenger initMessenger;
    protected Messenger serviceMessenger;
    protected Handler handler=null;

    Thread ThreadKeyDown = null;  //键盘事件线程
    String lockName=null;
    boolean hasRegisted=false;
    int networkState=0; //0: 网络连通 1：选择网线还是WIFI 2:WIFI检查 3：网线检查 4：WIFI选择 5:WIFI密码 6：WIFI连接成功 7：WIFI连接失败 10：网络连接成功
    int wifiListIndex=-1;
    List<ScanResult> wifiList=null;
    String wifiPassword="";
    SoundPool soundPool=null;
    int keyVoiceIndex=0;
    private int flagKeepScreenOn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("InitActivity","------>start init InitActivity<-------");
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,  flagKeepScreenOn);
        setContentView(R.layout.activity_init);  //zhl20170320
        SqlUtil sqlUtil=new SqlUtil(this);

        mainLayout=(LinearLayout) findViewById(R.id.mainLayout);  //zhl20170320
        initVersionInfo();  //显示版本信息
        initHandler();  //zhl20170320
        //initVoiceHandler();  //zhl20170320已经作废
        Log.v("InitActivity","------>start MainService<-------");
        startMainService(); //zhl20170320
        //closeBar(this);

    }

    Thread RefreshNetWorkThread=null;
    public  void  NetWorkRefresh(){
        RefreshNetWorkThread=new Thread(){
            public void run(){
                try {
                    while(true) {
                        if(isNetworkAvailable(InitActivity.this)){
                            Log.i("网络状态:","网络连接正常");
                        }else{
                            Log.i("网络状态:","网络连接异常");
                        }
                        Thread.sleep(400);
                    }
                }catch(Exception e){
                }
                RefreshNetWorkThread=null;
            }
        };
        RefreshNetWorkThread.start();
    }


    /**
     * 检测当的网络（WLAN、3G/2G）状态
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }


    public void closeBar(Context context) {
        try {
            // 需要root 权限
            Build.VERSION_CODES vc = new Build.VERSION_CODES();
            Build.VERSION vr = new Build.VERSION();
            String ProcID = "79";

            if (vr.SDK_INT >= vc.ICE_CREAM_SANDWICH) {
                ProcID = "42"; // ICS AND NEWER
            }

            // 需要root 权限
            Process proc = Runtime.getRuntime().exec(
                    new String[] {
                            "su",
                            "-c",
                            "service call activity " + ProcID
                                    + " s16 com.android.systemui" }); // WAS 79
            proc.waitFor();

        } catch (Exception ex) {
        }
    }

    //异步获取消息处理线程  zhl备注
    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                System.out.print(msg.what);
                if(msg.what == MSG_NO_MAC_ADDRESS){
                    setStatusText("无法获取设备编号");
                }else if(msg.what == MSG_GET_MAC_ADDRESS){
                    setStatusText("获取设备编号："+(String)msg.obj);
                }else if(msg.what == MSG_LOGIN){
                    JSONObject result=(JSONObject)msg.obj;
                    try{
                        int code=result.getInt("code");
                        if(code==0){
                            String name="";
                            JSONObject user=result.getJSONObject("user");
                            lockName=user.getString("lockName");
                            name=user.getString("communityName")+lockName;
                            setStatusText("设备为"+name+"可视对讲(版本"+DeviceConfig.APPVERSION+"),确认请按任意键继续...");
                            ThreadKeyDown = new Thread(){
                                public void run(){
                                try {
                                    sleep(1000*20);
                                    onKeyDown(KeyEvent.KEYCODE_0);
                                }catch(Exception e){}finally {
                                    ThreadKeyDown = null;
                                }
                                }
                            };
                            ThreadKeyDown.start();
                        }else if(code==1){
                            String mac=result.getString("mac");
                            setStatusText("该设备编号为"+mac+",在系统中未能找到,请在管理后台添加");
                        }
                    }catch(Exception e){
                    }
                }else if(msg.what==MSG_LOGIN_ERROR){
                    setStatusText("登录服务器发生错误，可能是网络连接不通，请检查后重启APP");
                }else if(msg.what == MSG_GET_TOKEN){
                    setStatusText("准备注册可视对讲服务...");
                }else if(msg.what == MSG_CANNOT_GET_TOKEN){
                    setStatusText("无法获取可视对讲服务器的token，请检查当前设备的网络是否稳定，系统时间是否准确");
                }else if(msg.what == MSG_RTC_REGISTER){
                    setStatusText("可视对讲服务注册成功");
                }else if(msg.what == MSG_RTC_CANNOT_REGISTER){
                    setStatusText("无法注册到可视对讲服务器，请重新启动APP");
                }else if(msg.what==MSG_NO_NETWORK){
                    networkState=1;
                    if(DeviceConfig.IS_SUPPORT_OFFLINE){
                        setStatusText("无法连接到互联网，请选择 1：WIFI连接 2：网线连接 3：离线运行");
                    }else{
                        setStatusText("无法连接到互联网，请选择 1：WIFI连接 2：网线连接");
                    }
                }else if(msg.what==MSG_CONNECT_SUCCESS){
                    networkState=10;
                    setStatusText("连接到互联网成功");
                }else if(msg.what==MSG_CONNECT_FAIL){
                    setStatusText("连接到互联网失败，请检查后按任意键重试...");
                }else if(msg.what==MSG_WIFI_LIST){
                    networkState=4;
                    List<ScanResult> wifiList=(List<ScanResult>)msg.obj;
                    showWifiList(wifiList);
                }else if(msg.what==MSG_WIFI_CONNECTED){
                    networkState=6;
                    setStatusText("WIFI连接成功，按任意键重试...");
                }else if(msg.what==MSG_WIFI_CONNECT_FAIL){
                    networkState=7;
                    setStatusText("WIFI连接失败，按任意键重试...");
                }else if(msg.what==MainService.MSG_ASSEMBLE_KEY){
                    int keyCode=(Integer)msg.obj;
                    onKeyDown(keyCode);
                }else if(msg.what==InitActivity.MSG_INIT_RFID){
                    Utils.DisplayToast(InitActivity.this, "初始化RFID设备");
                }else if(msg.what==InitActivity.MSG_INIT_ASSEMBLE){
                    Utils.DisplayToast(InitActivity.this, "初始化组合设备");
                }else if(msg.what==InitActivity.MSG_INIT_SQL){
                    String fingerNum=(String)msg.obj;
                    Utils.DisplayToast(InitActivity.this, "初始化数据库,共"+fingerNum+"条指纹");
                }
            }
        };
        initMessenger=new Messenger(handler);
    }

    //版本信息
    private void initVersionInfo(){
        //String txVersion = DeviceConfig.ApplicationEdit + String.valueOf(DeviceConfig.RELEASE_VERSION);
        setTextView(R.id.tx_version,"版本:"+DeviceConfig.APPVERSION);  //版本信息
    }

    //启动服务
    protected void startMainService(){
        Intent intent = new Intent(InitActivity.this,MainService.class);
        bindService(intent, connection, Service.BIND_AUTO_CREATE);
    }

    protected void sendMainMessenger(int code){
        Message message = Message.obtain();
        message.what = code;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void sendMainMessenger(int code,Object object){
        Message message = Message.obtain();
        message.what = code;
        message.obj=object;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onBtnCall(View viw) {
        //去下一个步骤
        gotoNextStep();
    }

    private void gotoNextStep(){
        if(lockName!=null&&!hasRegisted){
            hasRegisted=true;
            try {
                Utils.DisplayToast(InitActivity.this, "确认可视对讲的信息正确,正在注册可视对讲服务");
            }catch(Exception e){}
            //setTextView(R.id.ed_status,"确认可视对讲的信息正确,正在注册可视对讲服务...");
            sendMainMessenger(MainService.MSG_REGISTER);
        }else{
            try {
                Utils.DisplayToast(InitActivity.this, "正在检测设备信息");
            }catch(Exception e){}
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            onKeyDown(event);
//            keyVoice();
        }
        return false;
    }



    private void initVoiceHandler(){
        soundPool= new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        keyVoiceIndex=soundPool.load(this, R.raw.key, 1); //把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
    }

    private void keyVoice(){
        soundPool.play(keyVoiceIndex, 1, 1, 0, 0, 1);
    }

    public void onKeyDown(int keyCode) {
        Utils.DisplayToast(InitActivity.this, "code"+keyCode);
        if(networkState==0) {  //网络正常登录界面
            gotoNextStep();
        }else if(networkState==1){  //网络异常登录界面
            if ((keyCode == KeyEvent.KEYCODE_1)) {  //选择其他wifi设置
                onChooseWifi();
            }else if ((keyCode == KeyEvent.KEYCODE_2)) {  //以太网登录界面
                onChooseEth();
            }else if ((keyCode == KeyEvent.KEYCODE_3)) {  //离线状态进入
                if(DeviceConfig.IS_SUPPORT_OFFLINE){
                    onStartOffline();
                }
            }
        }else if(networkState==2){

        }else if(networkState==3){
            sendMainMessenger(MainService.MSG_CHECK_NETWORK);
        }else if(networkState==4){
            if ((keyCode == KeyEvent.KEYCODE_1)) {
                startWifiPassword();
            }else if ((keyCode == KeyEvent.KEYCODE_2)) {
                nextWifi();
            }
        }else if(networkState==5){
            inputWifiPassword(keyCode);
        }else if(networkState==6){
            sendMainMessenger(MainService.MSG_CHECK_NETWORK);
        }else if(networkState==7){
            networkState=4;
            showWifiList(wifiList);
        }else if(networkState==10){
            networkState=0;
            sendMainMessenger(MainService.MSG_START_INIT);
        }
    }

    //离线登录
    private void onStartOffline(){
        sendMainMessenger(MainService.MSG_START_OFFLINE);
    }

    public void onKeyDown(KeyEvent event){
        int keyCode=event.getKeyCode();
        onKeyDown(keyCode);
    }

    protected void inputWifiPassword(int keyCode){
        if ((keyCode == KeyEvent.KEYCODE_0)) {
            wifiPassword+="0";
        }else if ((keyCode == KeyEvent.KEYCODE_1)) {
            wifiPassword+="1";
        }else if ((keyCode == KeyEvent.KEYCODE_2)) {
            wifiPassword+="2";
        }else if ((keyCode == KeyEvent.KEYCODE_3)) {
            wifiPassword+="3";
        }else if ((keyCode == KeyEvent.KEYCODE_4)) {
            wifiPassword+="4";
        }else if ((keyCode == KeyEvent.KEYCODE_5)) {
            wifiPassword+="5";
        }else if ((keyCode == KeyEvent.KEYCODE_6)) {
            wifiPassword+="6";
        }else if ((keyCode == KeyEvent.KEYCODE_7)) {
            wifiPassword+="7";
        }else if ((keyCode == KeyEvent.KEYCODE_8)) {
            wifiPassword+="8";
        }else if ((keyCode == KeyEvent.KEYCODE_9)) {
            wifiPassword+="9";
        }else if ((keyCode == KeyEvent.KEYCODE_POUND)) {
            sendMainMessenger(MainService.MSG_WIFI_CONNECT,wifiListIndex+":"+wifiPassword);
        }else if ((keyCode == KeyEvent.KEYCODE_STAR)) {
            wifiPassword="";
        }
        setStatusText("请输入WIFI密码（按#号键确认，按*号键取消）："+wifiPassword);
    }

    protected void startWifiPassword(){
        networkState=5;
        setStatusText("请输入WIFI密码（按#号键确认，按*号键取消）：");
    }

    //选择其他wifi设置
    protected void onChooseWifi(){
        networkState=2;
        setStatusText("选择WIFI连接，正在搜索WIFI信号...");
        sendMainMessenger(MainService.MSG_CHECK_WIFI);
    }

    //以太网登录界面
    protected void onChooseEth(){
        networkState=3;
        setStatusText("选择通过网线连接网络,请将网线接好，并设置成自动分配IP及DNS，按任意键继续...");
    }

    protected void showWifiList(List<ScanResult> wifiList){
        this.wifiList=wifiList;
        if(wifiList!=null && wifiList.size()>0){
            nextWifi();
        }else{
            setStatusText("无WIFI信号，按任意键重试...");
        }
    }

    protected void nextWifi(){
        if(wifiListIndex==(wifiList.size()-1)){
            wifiListIndex=0;
        }else{
            wifiListIndex++;
        }
        ScanResult scanResult=wifiList.get(wifiListIndex);
        String title="搜索到WIFI信号【"+scanResult.SSID+"】,选择此WIFI信号请按1，选择其他WIFI请按2";
        setStatusText(title);
    }


    //启动Service服务（初始化设备）
    private ServiceConnection connection = new ServiceConnection() {
        @Override//连接成功
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获取Service端的Messenger
            serviceMessenger = new Messenger(service);
            //创建消息
            Message message = Message.obtain();
            message.what = MainService.REGISTER_ACTIVITY_INIT;
            message.replyTo = initMessenger;
            try {
                //通过ServiceMessenger将注册消息发送到Service中的Handler
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override//断开连接
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * Sets the status text.
     *MSG_GET_TOKEN
     * @param sText the new status text
     */
    void setStatusText(String sText) {
        final String outStr = sText;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.ed_status,outStr);
            }
        });
    }

    void setTextView(int id, String txt) {
        ((TextView) findViewById(id)).setText(txt);
    }

}
