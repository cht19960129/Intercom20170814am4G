package com.nodepoint.residential;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.biometric.oneface.FaceManager;
import com.isnc.facesdk.SuperID;
import com.isnc.facesdk.common.Cache;
import com.isnc.facesdk.common.SDKConfig;
import com.nodepoint.residential.config.DeviceConfig;
import com.nodepoint.residential.service.MainService;
import com.nodepoint.residential.gilde.PullFaceReceiver;
import com.nodepoint.residential.service.TimeToRestartService;
import com.nodepoint.residential.util.AdvertiseHandler;
import com.nodepoint.residential.util.HttpUtils;
import com.nodepoint.residential.util.Sound.PromptSound;
import com.nodepoint.residential.util.UploadUtil;
import com.nodepoint.residential.util.finger.IFingerState;
import com.nodepoint.residential.util.finger.SZOEMHost_Lib;
import com.nodepoint.residential.view.FaceRegionView;
import org.json.JSONArray;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import jni.util.Utils;


public class DialActivity extends Activity implements IFingerState, FaceManager.OnFaceMatchedListener {
    private static final String TAG = "DialActivity";
    public static final int MSG_RTC_NEWCALL=10000;
    public static final int MSG_RTC_ONVIDEO=10001;
    public static final int MSG_RTC_DISCONNECT=10002;
    public static final int MSG_PASSWORD_CHECK=10003;
    public static final int MSG_LOCK_OPENED=10004;
    public static final int MSG_CALLMEMBER_ERROR=10005;
    public static final int MSG_CALLMEMBER_TIMEOUT=11005;
    public static final int MSG_CALLMEMBER_NO_ONLINE=12005;
    public static final int MSG_CALLMEMBER_TIMEOUT_AND_TRY_DIRECT=13005;
    public static final int MSG_CALLMEMBER_DIRECT_TIMEOUT=14005;
    public static final int MSG_CALLMEMBER_DIRECT_DIALING=15005;
    public static final int MSG_CALLMEMBER_DIRECT_SUCCESS=16005;
    public static final int MSG_CALLMEMBER_DIRECT_FAILED=17005;
    public static final int MSG_CALLMEMBER_DIRECT_COMPLETE=18005;
    public static final int MSG_CALLMEMBER_CANCEL = 19005;

    public static final int MSG_CONNECT_ERROR=10007;
    public static final int MSG_CONNECT_SUCCESS=10008;
    public static final int ON_YUNTONGXUN_INIT_ERROR=10009;
    public static final int ON_YUNTONGXUN_LOGIN_SUCCESS=10010;
    public static final int ON_YUNTONGXUN_LOGIN_FAIL=10011;
    public static final int MSG_CANCEL_CALL_COMPLETE=10012;

    public static final int MSG_ADVERTISE_REFRESH=10013;
    public static final int MSG_ADVERTISE_IMAGE=10014;
    public static final int MSG_INVALID_CARD=10015;
    public static final int MSG_CHECK_BLOCKNO=10016;
    public static final int MSG_FINGER_CHECK=10017;
    public static final int MSG_REFRESH_DATA=10018;
    public static final int MSG_REFRESH_COMMUNITYNAME=10019;
    public static final int MSG_REFRESH_LOCKNAME=10020;

    public static final int CALL_MODE=1;
    public static final int PASSWORD_MODE=2;
    public static final int CALLING_MODE=3;
    public static final int ONVIDEO_MODE=4;
    public static final int DIRECT_MODE=5;
    public static final int ERROR_MODE=6;
    public static final int DIRECT_CALLING_MODE=7;
    public static final int DIRECT_CALLING_TRY_MODE=8;
    public static final int PASSWORD_CHECKING_MODE=9;

    protected Messenger serviceMessenger;
    protected Messenger dialMessenger;
    protected Handler handler=null;

    public static int currentStatus=CALL_MODE;
    private String blockNo="";
    private int blockId=0;
    private String callNumber="";
    private String guestPassword="";
    private int checkingStatus=0;
    Thread ThreadPIC = null;  //定义全局线程
    Camera camera= null;

    //VideoView video;
    TextView headPaneTextView=null;
    SurfaceView videoView = null;
    ImageView imageView=null;

    SurfaceView localView = null;
    SurfaceView remoteView = null;
    LinearLayout videoLayout;//视频通话

    SurfaceView autoCameraSurfaceView=null;
    SurfaceHolder autoCameraHolder=null;

    AdvertiseHandler advertiseHandler=null;
    Thread passwordTimeoutThread=null;
    Thread clockRefreshThread=null;

    //人脸识别相关
    private FaceRegionView mFaceRegistView;
    private SurfaceView surface;
    private TextView tvToast;
    public static FaceManager faceManager;

    private static SZOEMHost_Lib fingerHost;
    SoundPool soundPool=null;
    int keyVoiceIndex=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DeviceConfig.HIDE_SCREEN_STATUS==1) {
            Window window = getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            window.setAttributes(params);
        }

        setContentView(R.layout.activity_dial);

        //人脸识别相关
        mFaceRegistView = (FaceRegionView) findViewById(R.id.mFaceRegistView);
        surface = (SurfaceView) findViewById(R.id.surface);
        tvToast = (TextView) findViewById(R.id.tv_toast);

        initSDKFaceManager();
        //人脸识别相关   定时拉取信息
        timingPullFaceData();

//        //定时重启APP
//        startTimeToRestartAppService();

        //初始化屏幕
        initScreen();
        Intent intent = new Intent(DialActivity.this,MainService.class);
        bindService(intent,connection,0);
        initHandler();
        initVoiceHandler();
        initVoiceVolume(); //设置播放广告媒体声音 zhl 20170706pm
        initAdvertiseHandler();
        initAutoCamera();
        if(DeviceConfig.DEVICE_TYPE.equals("C")){
            setDialStatus("请输入楼栋编号");//将汉字放入tv_input_label控件
        }

        startClockRefresh();  //刷新时间星期
        initFingerHost();     //初始化指纹模块
        openFingerDevice();   //打开指纹模块
    }
//    //定时重启APP
//    private void startTimeToRestartAppService() {
//        Intent intent = new Intent(getApplicationContext(), TimeToRestartService.class);
//        bindService(intent, connServiceconn, BIND_AUTO_CREATE);
//    }
//    private Serviceconn connServiceconn = new Serviceconn();
//    private class Serviceconn implements ServiceConnection {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.e("--------", "------开启服务成功---");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    }



    private void timingPullFaceData() {
        Log.e("timingPullFaceData方法","执行了timingPullFaceData");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取AlarmManager对象
                AlarmManager aManager=(AlarmManager)getSystemService(Service.ALARM_SERVICE);
                Intent intent =new Intent(DialActivity.this, PullFaceReceiver.class);
                intent.setAction("repeating");
                //开始时间
                long firstime= SystemClock.elapsedRealtime();
                PendingIntent sender=PendingIntent.getBroadcast(DialActivity.this, 0, intent, 0);
                //10分钟一个周期，不停的发送广播
                aManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, 10*60*1000, sender);
            }
        }).start();

    }

    //人脸识别相关  初始化FaceManager
    public void initSDKFaceManager() {
        faceManager = new FaceManager(this);
        faceManager.init(surface, new FaceManager.OnInitListener() {
            @Override
            public void onCompleted() {
                //sdkManager初始化成功
                Log.e("onCompleted()方法","sdkFaceManager初始化成功");
                faceManager.setOnFaceMatchedListener(DialActivity.this);
                faceManager.startScan();
            }
            @Override
            public void onFailed() {
                //sdkManager初始化失败
                Log.e("onFailed()方法","sdkFaceManager初始化失败");
            }
        });
    }

    protected void initAutoCamera(){
        autoCameraSurfaceView = (SurfaceView) findViewById(R.id.autoCameraSurface);
        autoCameraHolder = autoCameraSurfaceView.getHolder();
        autoCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //RTC呼叫    第一步
    protected void callhomemuber(final String thisValue, final boolean isCall) {
        Log.e(TAG, "callhomemuber: thisValue " + thisValue);
        new Thread() {
            public void run() {
                try {
                    Message message = Message.obtain();
                    if (isCall) {
                        message.what = MainService.MSG_START_DIAL;
                    } else {
                        message.what = MainService.MSG_CHECK_PASSWORD;
                    }
                    String[] parameters = new String[2];
                    parameters[0] = thisValue;  //房屋编号
                    parameters[1] = "";    //服务器图片路径
                    message.obj = parameters;
                    serviceMessenger.send(message);  //发送消息到服务线程
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }  //end run
        }.start();  //end Thread

    }

    //呼叫拍照留影  zhl 20170605am
    protected void takePicture(final String thisValue,final boolean isCall){
        ThreadPIC =new Thread(){
            public void run(){
                try {
                    if (camera != null) {
                        try {
                            camera.stopPreview(); //关闭视像头
                            camera.release();
                            camera = null;
                        } catch (Exception e) {
                        }
                    }
                    try {
                        camera = Camera.open(0);
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.set("jpeg-quality", 35);//设置照片质量
                        parameters.setPreviewSize(320, 240);
                        camera.setParameters(parameters);
                        camera.setPreviewDisplay(autoCameraHolder);  //zhl 设置显示拍摄图片
                        camera.startPreview();
                        camera.autoFocus(null);
                        camera.takePicture(null, null, new Camera.PictureCallback() {
                            public void onPictureTaken(byte[] data, Camera camera) {
                                try {
                                    //启动本地视像头拍照，并保存到本地_STAR=======================
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    final File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
                                    FileOutputStream outputStream = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                    outputStream.close();
                                    //启动本地视像头拍照，并保存到本地_END=======================
                                    camera.stopPreview(); //关闭视像头
                                    camera.release();
                                    camera = null;
                                    final String url = DeviceConfig.SERVER_URL + "/app/upload/image";
                                    new Thread() {
                                        public void run() {
                                            boolean result = false;
                                            String fileUrl = null;
                                            try {
                                                fileUrl = UploadUtil.uploadFile(file, url);  //上传本地图片到服务器
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }finally {
                                                try {
                                                    Message message = Message.obtain();
                                                    if (isCall) {
                                                        message.what = MainService.MSG_START_DIAL;
                                                    } else {
                                                        message.what = MainService.MSG_CHECK_PASSWORD;
                                                    }
                                                    String[] parameters = new String[2];
                                                    parameters[0] = thisValue;  //房屋编号
                                                    parameters[1] = fileUrl;    //服务器图片路径
                                                    message.obj = parameters;
                                                    serviceMessenger.send(message);  //发送消息到服务线程
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }

                                            try {
                                                if (file != null) {
                                                    file.deleteOnExit();  //删除本地图片文件
                                                }
                                            } catch (Exception e) {
                                            }
                                        }  //end run
                                    }.start();  //end Thread
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } //end try
                            }  //end onPictureTaken
                        });  //end takePicture
                    } catch (Exception e) {
                        Message message = Message.obtain();
                        if (isCall) {
                            message.what = MainService.MSG_START_DIAL;   //呼叫房号异常，像服务线程发送消息
                        } else {
                            message.what = MainService.MSG_CHECK_PASSWORD;   //密码开门异常，像服务线程发送消息
                        }
                        String[] parameters = new String[2];
                        parameters[0] = thisValue;    //房屋编号
                        parameters[1] = null;        //服务器图片路径
                        message.obj = parameters;
                        try {
                            serviceMessenger.send(message);  //发送消息到服务线程
                        } catch (RemoteException er) {
                            er.printStackTrace();
                        }  //end try
                    }  //end try
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    ThreadPIC=null;
                }
            }  //end run
        };
        ThreadPIC.start();  //end Thread
    }

    protected void initFingerHost(){
        if(DeviceConfig.IS_FINGER_AVAILABLE){
            if(fingerHost == null){
                Log.i("finglog:","SZOEMHost_Lib to new!");
                fingerHost = new SZOEMHost_Lib(this,this);
            }else{
                Log.i("finglog:","SZOEMHost_Lib to obj!");
                fingerHost.SZOEMHost_Lib_Init(this,this);
            }
        }
    }

    public void openFingerDevice(){
        if(DeviceConfig.IS_FINGER_AVAILABLE) {
            if (fingerHost.OpenDevice() == 0) {
                Log.i("finglog:","fingerHost.OpenDevice to 0!");
            }else{
                Log.i("finglog:","fingerHost.OpenDevice not 0!");
            }
        }
    }

    //控制设备媒体声音  zhl20170707pm
    protected void initVoiceVolume(){
        AudioManager audioManager=(AudioManager)getSystemService(this.AUDIO_SERVICE);
        initVoiceVolume(audioManager,AudioManager.STREAM_RING,DeviceConfig.VOLUME_STREAM_RING);   //铃声音量
        initVoiceVolume(audioManager,AudioManager.STREAM_MUSIC,DeviceConfig.VOLUME_STREAM_MUSIC); //音乐回放即媒体音量
        initVoiceVolume(audioManager,AudioManager.STREAM_SYSTEM,DeviceConfig.VOLUME_STREAM_SYSTEM); //系统音量
        initVoiceVolume(audioManager,AudioManager.STREAM_VOICE_CALL,DeviceConfig.VOLUME_STREAM_VOICE_CALL);  //通话音量
    }

    protected void initVoiceVolume(AudioManager audioManager,int type,int value){
        int thisValue=audioManager.getStreamMaxVolume(type);
        thisValue=thisValue*value/100;
        audioManager.setStreamVolume(type,thisValue,AudioManager.FLAG_PLAY_SOUND);
    }

    protected void initSuperID(){
        SuperID.initFaceSDK(this);
        SuperID.setDebugMode(true);
        HashMap<String, String> map = new HashMap<String,String>();
        map.put(SDKConfig.KEY_CAMERATYPE,"0");
        SuperID.getInstance().setHashMap(map);
    }

    protected void initScreen(){
        headPaneTextView=(TextView)findViewById(R.id.header_pane);
        videoLayout=(LinearLayout) findViewById(R.id.ll_video);//视频通话
        setTextView(R.id.tv_community,MainService.communityName);
        setTextView(R.id.tv_lock,MainService.lockName);
    }

    protected void initAdvertiseHandler(){
        advertiseHandler=new AdvertiseHandler();
        videoView=(SurfaceView)findViewById(R.id.surface_view);

        imageView=(ImageView)findViewById(R.id.image_view);
        advertiseHandler.init(videoView,imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        //定时重启App
//        unregisterReceiver(reStartReceiver);
//        unbindService(connServiceconn);
        unbindService(connection);
        advertiseHandler.onDestroy();
        fingerHost.CloseDevice();
    }

    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MSG_RTC_NEWCALL){
                   onRtcConnected();//新电话
                }else if(msg.what == MSG_RTC_ONVIDEO){
                    onRtcVideoOn(); //视频
                }else if(msg.what == MSG_RTC_DISCONNECT){
                    onRtcDisconnect();//断开连接
                }else if(msg.what==MSG_PASSWORD_CHECK){
                    onPasswordCheck((Integer) msg.obj);
                }else if(msg.what==MSG_LOCK_OPENED){
                    onLockOpened();
                }else if(msg.what==MSG_CALLMEMBER_ERROR){
                    onCallMemberError(msg.what);
                }else if(msg.what==MSG_CALLMEMBER_CANCEL){  //挂断拨号
                    resetDial();
                    startCancelCall();
                }else if(msg.what==MSG_CALLMEMBER_NO_ONLINE){
                    onCallMemberError(msg.what);
                }else if(msg.what==MSG_CALLMEMBER_TIMEOUT){
                    onCallMemberError(msg.what);
                }else if(msg.what==MSG_CALLMEMBER_TIMEOUT_AND_TRY_DIRECT){
                    Utils.DisplayToast(DialActivity.this, "可视对讲无法拨通，尝试直拨电话");
                    setCurrentStatus(DIRECT_CALLING_TRY_MODE);
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_TIMEOUT){
                    onCallMemberError(msg.what);
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_DIALING){
                    Utils.DisplayToast(DialActivity.this, "开始直拨电话");
                    setCurrentStatus(DIRECT_CALLING_MODE);
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_SUCCESS){
                    Utils.DisplayToast(DialActivity.this, "电话已接通，请让对方按#号键开门");
                    onCallDirectlyBegin();
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_FAILED){
                    Utils.DisplayToast(DialActivity.this, "电话未能接通，重试中..");
                }else if(msg.what==MSG_CALLMEMBER_DIRECT_COMPLETE){
                    onCallDirectlyComplete();
                }else if(msg.what==MSG_CONNECT_ERROR){
                    onConnectionError();
                }else if(msg.what== MSG_CONNECT_SUCCESS){
                    onConnectionSuccess();
                }else if(msg.what==ON_YUNTONGXUN_INIT_ERROR){
                    Utils.DisplayToast(DialActivity.this, "直拨电话初始化异常");
                }else if(msg.what==ON_YUNTONGXUN_LOGIN_SUCCESS){
                    Utils.DisplayToast(DialActivity.this, "直拨电话服务器连接成功");
                }else if(msg.what==ON_YUNTONGXUN_LOGIN_FAIL){
                    Utils.DisplayToast(DialActivity.this, "直拨电话服务器连接失败");
                }else if(msg.what==MSG_CANCEL_CALL_COMPLETE){
                    setCurrentStatus(CALL_MODE);
                }else if(msg.what==MSG_ADVERTISE_REFRESH){
                    onAdvertiseRefresh(msg.obj);
                }else if(msg.what==MSG_ADVERTISE_IMAGE){
                    onAdvertiseImageChange(msg.obj);
                }else if(msg.what==MSG_INVALID_CARD){
                    Utils.DisplayToast(DialActivity.this, "无效房卡");
                }else if(msg.what==MainService.MSG_ASSEMBLE_KEY){
                    int keyCode=(Integer)msg.obj;
                    Log.i("zhlKey---","----"+keyCode);
                    onKeyDown(keyCode);
                }else if(msg.what==MSG_CHECK_BLOCKNO){
                    blockId=(Integer)msg.obj;
                    onCheckBlockNo();
                }else if(msg.what==MSG_FINGER_CHECK){
                    boolean result=(Boolean)msg.obj;
                    onFingerCheck(result);
                }else if(msg.what==MSG_REFRESH_DATA){
                    onFreshData((String)msg.obj);
                }else if(msg.what==MSG_REFRESH_COMMUNITYNAME){
                    onFreshCommunityName((String)msg.obj);
                }else if(msg.what==MSG_REFRESH_LOCKNAME){
                    onFreshLockName((String)msg.obj);
                }
            }
        };
        dialMessenger=new Messenger(handler);
    }


    private void onFreshData(String type){
        if("card".equals(type)){
            Utils.DisplayToast(DialActivity.this, "更新卡数据");
        }else if("finger".equals(type)){
            Utils.DisplayToast(DialActivity.this, "更新指纹数据");
        }else if("faceInfo".equals(type)){
            Utils.DisplayToast(DialActivity.this, "更新人脸数据");
        }else if("allinfo".equals(type)) {
            Utils.DisplayToast(DialActivity.this, "初始化设备数据");
        }
    }

    private void onFreshCommunityName(String communityName){
        if(communityName!=null){
            setCommunityName(communityName);
        }
    }

    private void onFreshLockName(String lockName){
        if(lockName!=null){
            setLockName(lockName);
        }
    }

    private void onFingerCheck(boolean result){
        if(result){
            //openlockAudio();
            Utils.DisplayToast(DialActivity.this, "指纹开门成功");
        }else{
            Utils.DisplayToast(DialActivity.this, "指纹开门失败，您没有获得该门禁的权限");
        }
    }

    //开门提示语音
    protected  void openlockAudio(){
//        try {
//            MediaPlayer mediaPlayer= MediaPlayer.create(this, R.raw.openlock);
//            mediaPlayer.start();
//        }catch (Exception e){}
//        Intent intent = new Intent(getBaseContext(), PromptSound.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
        //String Source = "android.resource://" + getPackageName() + "/" + R.raw.openlock;
        //Uri ul  = Uri.parse("android.resource://" + getPackageName() + "/" +R.raw.openlock);
       // advertiseHandler.startSourcePlay(this,R.raw.openlock);
        Intent intent = new Intent(DialActivity.this, PromptSound.class);
        startActivity(intent);

    }

    protected  void openlockAudio1(){
        try {
            MediaPlayer mediaPlayer= MediaPlayer.create(DialActivity.this, R.raw.openlock);
            mediaPlayer.start();
        }catch (Exception e){}

    }

    private void onCheckBlockNo(){
        checkingStatus=0;
        if(blockId==0){
            blockNo="";
            callNumber="";
            setDialValue(blockNo);
            Utils.DisplayToast(DialActivity.this, "楼栋编号不存在");
        }else{
            callNumber="";
            setDialValue(callNumber);
            setDialStatus("输入房间号呼叫");
        }
    }

    private void onConnectionError(){
        setCurrentStatus(ERROR_MODE);
        setTextView(R.id.header_pane,"可视对讲设备异常，网络连接已断开");
        headPaneTextView.setVisibility(View.VISIBLE);
    }

    private void onConnectionSuccess(){
        if(currentStatus==ERROR_MODE){
            initDialStatus();
            setTextView(R.id.header_pane,"");
            headPaneTextView.setVisibility(View.INVISIBLE);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获取Service端的Messenger
            serviceMessenger = new Messenger(service);
            Message message = Message.obtain();
            message.what = MainService.REGISTER_ACTIVITY_DIAL;
            message.replyTo = dialMessenger;
            try {
                //通过ServiceMessenger将注册消息发送到Service中的Handler
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private void keyInput(int key){

    }

    //开始取消
    private void startCancelDirectCall(){
        Message message = Message.obtain();
        message.what = MainService.MSG_CANCEL_DIRECT;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startDisconnectDirectCall(){
        Message message = Message.obtain();
        message.what = MainService.MSG_DISCONNECT_DIRECT;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startDisconnectVideo(){
        Message message = Message.obtain();
        message.what = MainService.MSG_DISCONNECT_VIEDO;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startDialing(){
        setCurrentStatus(CALLING_MODE);
        String thisNumber=callNumber;
        callNumber="";
        if(DeviceConfig.DEVICE_TYPE=="C"){
            blockId=0;
            blockNo ="";
            setDialStatus("请输入楼栋编号");
        }
        //setDialValue("呼叫"+thisNumber+"，取消请按#号键");
        setDialValue("呼叫"+thisNumber);
        //takePicture(thisNumber,true);
        callhomemuber(thisNumber,true);
    }

    private void callInput(int key){
        if(DeviceConfig.DEVICE_TYPE.equals("C")){
            if(blockId==0){
                if(blockNo.length()<DeviceConfig.BLOCK_NO_LENGTH){
                    blockNo=blockNo+key;
                    setDialValue(blockNo);
                }
                if(blockNo.length()== DeviceConfig.BLOCK_NO_LENGTH){
                    checkingStatus=1;
//                    setDialValue("检查楼栋编号:"+blockNo);
                    Message message = Message.obtain();
                    message.what = MainService.MSG_CHECK_BLOCKNO;
                    message.obj = blockNo;
                    try {
                        serviceMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                if(checkingStatus==0) {
                    unitNoInput(key);
                }
            }
        }else{
            unitNoInput(key);
        }
    }
    private void unitNoInput(int key){
        callNumber=callNumber+key;
        setDialValue(callNumber);
        if(callNumber.length()== DeviceConfig.UNIT_NO_LENGTH){
            startDialing();
        }
    }

    private String backKey(String code){
        if(code!=null&&code!=""){
            int length=code.length();
            if(length==1){
                code="";
            }else{
                code=code.substring(0,(length-1));
            }
        }
        return code;
    }
    private void callInput(){
        if(DeviceConfig.DEVICE_TYPE.equals("C")) {
            if(blockId>0){
                if(callNumber.equals("")){
                    blockId=0;
                    blockNo = backKey(blockNo);
                    setDialStatus("请输入楼栋编号");
                    setDialValue(blockNo);
                }else{
                    callNumber = backKey(callNumber);
                    setDialValue(callNumber);
                }
            }else{
                blockNo = backKey(blockNo);
                setDialValue(blockNo);
            }
        }else{
            callNumber = backKey(callNumber);
            setDialValue(callNumber);
        }
    }

    private void checkPassword(){
        setCurrentStatus(PASSWORD_CHECKING_MODE);
        String thisPassword=guestPassword;
        guestPassword="";
        setTempkeyValue("验证密码"+thisPassword+"...");
        takePicture(thisPassword,false);
    }

    private void onLockOpened(){
        setDialValue("");
        setTempkeyValue("");
        if(currentStatus!=PASSWORD_MODE&&currentStatus!=PASSWORD_CHECKING_MODE){
            setCurrentStatus(CALL_MODE);
        }
        Utils.DisplayToast(DialActivity.this, "门锁已经打开");
    }

    private void onPasswordCheck(int code){
        setCurrentStatus(PASSWORD_MODE);
        setTempkeyValue("");
        if(code==0){
            Utils.DisplayToast(DialActivity.this, "您输入的密码验证成功");
        }else{
            if(code==1){
                Utils.DisplayToast(DialActivity.this, "您输入的密码不存在");
            }else if(code==2){
                Utils.DisplayToast(DialActivity.this, "您输入的密码已经过期");
            }
        }
    }

    private void passwordInput(int key){
        guestPassword=guestPassword+key;
        setTempkeyValue(guestPassword);
        if(guestPassword.length()==6){
            checkPassword();
        }
    }

    private void passwordInput(){
        guestPassword=backKey(guestPassword);
        setTempkeyValue(guestPassword);
    }


    private int convertKeyCode(int keyCode){
        int value=-1;
        if ((keyCode == KeyEvent.KEYCODE_0)) {
            value=0;
        }else if ((keyCode == KeyEvent.KEYCODE_1)) {
            value=1;
        }else if ((keyCode == KeyEvent.KEYCODE_2)) {
            value=2;
        }else if ((keyCode == KeyEvent.KEYCODE_3)) {
            value=3;
        }else if ((keyCode == KeyEvent.KEYCODE_4)) {
            value=4;
        }else if ((keyCode == KeyEvent.KEYCODE_5)) {
            value=5;
        }else if ((keyCode == KeyEvent.KEYCODE_6)) {
            value=6;
        }else if ((keyCode == KeyEvent.KEYCODE_7)) {
            value=7;
        }else if ((keyCode == KeyEvent.KEYCODE_8)) {
            value=8;
        }else if ((keyCode == KeyEvent.KEYCODE_9)) {
            value=9;
        }
        return value;
    }

    //物理按键触发的事件
    private void onKeyDown(int keyCode){
        if(currentStatus==CALL_MODE || currentStatus==PASSWORD_MODE){
            int key=convertKeyCode(keyCode);
            if(key>=0){
                if(currentStatus==CALL_MODE){
                    callInput(key);
                }else{
                    passwordInput(key);
                }
            }else if(keyCode==KeyEvent.KEYCODE_POUND){
                if(currentStatus==CALL_MODE){
                    initPasswordStatus();
                }else{
                    initDialStatus();
                }
            }else if(keyCode==KeyEvent.KEYCODE_STAR){
                if(currentStatus==CALL_MODE){
                    callInput();
                }else{
                    passwordInput();
                }
            }
        }else if(currentStatus==ERROR_MODE){
            Utils.DisplayToast(DialActivity.this, "当前网络异常");
        }else if(currentStatus==CALLING_MODE){
            if(keyCode==KeyEvent.KEYCODE_POUND){
                Utils.DisplayToast(DialActivity.this, "您已经取消拨号");
                resetDial();
                startCancelCall();
            }
        }else if(currentStatus==ONVIDEO_MODE){
            if(keyCode==KeyEvent.KEYCODE_POUND) {
                startDisconnectVideo();
            }
        }else if(currentStatus==DIRECT_CALLING_MODE){
            if(keyCode==KeyEvent.KEYCODE_POUND) {
                resetDial();
                startCancelDirectCall();
            }
        }else if(currentStatus==DIRECT_CALLING_TRY_MODE){
            if(keyCode==KeyEvent.KEYCODE_POUND) {
                resetDial();
                startCancelDirectCall();
            }
        }else if(currentStatus==DIRECT_MODE){
            if(keyCode==KeyEvent.KEYCODE_POUND) {
                resetDial();
                startDisconnectDirectCall();
            }
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            int keyCode=event.getKeyCode();
            onKeyDown(keyCode);
            //keyVoice();
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

    protected void startFaceLogin(){
        //SuperID.faceLogin(this);
        SuperID.faceVerify(this,1);
    }

    public void onFaceLogin(View viw) {
        startFaceLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case SDKConfig.VERIFY_SUCCESS:
                String openid = Cache.getCached(this,SDKConfig.KEY_OPENID);
                if(openid!=null){
                    Utils.DisplayToast(DialActivity.this, openid);
                }
                break;
            case SDKConfig.VERIFY_FAIL:
                Utils.DisplayToast(DialActivity.this, "您不是注册用户");
                break;
            default:
                break;
        }
    }

    protected void resetDial(){
        callNumber="";
        setDialValue(callNumber);
        setCurrentStatus(CALL_MODE);
    }

    protected void startCancelCall(){
        Message message = Message.obtain();
        message.what = MainService.MSG_CANCEL_CALL;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initDialStatus(){
        videoLayout.setVisibility(View.INVISIBLE);////视频通话
        setCurrentStatus(CALL_MODE);
        callNumber="";
        blockNo="";
        blockId=0;
        if(DeviceConfig.DEVICE_TYPE=="C"){
            setDialStatus("请输入楼栋编号");
        }else{
            setDialStatus("请输入房号");
        }
        setDialValue(callNumber);
    }

    private void initPasswordStatus(){
        stopPasswordTimeoutChecking();
        setDialStatus("请输入访客密码");
        videoLayout.setVisibility(View.INVISIBLE);//视频通话
        setCurrentStatus(PASSWORD_MODE);
        guestPassword="";
        setTempkeyValue(guestPassword);
        startTimeoutChecking();
    }

    private void startClockRefresh(){
        clockRefreshThread=new Thread(){
            public void run(){
                try {
                    setNewTime();
                    while(true) {
                        sleep(1000 * 60); //等待指定的一个等待时间
                        if (!isInterrupted()) { //检查线程没有被停止
                            setNewTime();
                        }
                    }
                }catch(InterruptedException e){
                }
                clockRefreshThread=null;
            }
        };
        clockRefreshThread.start();
    }
    //时间
    private void setNewTime(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Date now=new Date();
                SimpleDateFormat dateFormat=new SimpleDateFormat("E");
                String dayStr=dateFormat.format(now);
                dateFormat=new SimpleDateFormat("yyyy-MM-dd");
                String dateStr=dateFormat.format(now);
                dateFormat=new SimpleDateFormat("HH:mm");
                String timeStr=dateFormat.format(now);;

                setTextView(R.id.tv_day,dayStr);
                setTextView(R.id.tv_date,dateStr);
                setTextView(R.id.tv_time,timeStr);
                //setTextView(R.id.tv_version,"版本:"+DeviceConfig.ApplicationEdit + String.valueOf(DeviceConfig.RELEASE_VERSION));  //版本信息
                setTextView(R.id.tv_version,"版本:"+DeviceConfig.APPVERSION);  //版本信息

            }
        });
    }

    //开始超时检查
    private void startTimeoutChecking(){
        passwordTimeoutThread=new Thread(){
            public void run(){
                try {
                    sleep(DeviceConfig.PASSWORD_WAIT_TIME); //等待指定的一个等待时间
                    if(!isInterrupted()){ //检查线程没有被停止
                        if(currentStatus==PASSWORD_MODE){ //如果现在是密码输入状态
                            if(guestPassword.equals("")){ //如果密码一直是空白的
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        initDialStatus();
                                    }
                                });
                                stopPasswordTimeoutChecking();
                            }
                        }
                    }
                }catch(InterruptedException e){
                }
                passwordTimeoutThread=null;
            }
        };
        passwordTimeoutThread.start();
    }

    protected void stopPasswordTimeoutChecking(){
        if(passwordTimeoutThread!=null){
            passwordTimeoutThread.interrupt();
            passwordTimeoutThread=null;
        }
    }
    synchronized void setCurrentStatus(int status) {
        currentStatus=status;
    }
    void setDialStatus(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_input_label,thisValue);
            }
        });
    }
    void setDialValue(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_input_text,thisValue);
            }
        });
    }
    void setTempkeyValue(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_input_text,thisValue);
            }
        });
    }

    void setCommunityName(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_community,thisValue);
            }
        });
    }

    void setLockName(String value) {
        final String thisValue=value;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setTextView(R.id.tv_lock,thisValue);
            }
        });
    }

    void setTextView(int id,String txt) { ((TextView)findViewById(id)).setText(txt); }

    void initVideoViews() {
        if (localView !=null) return;
        if(MainService.callConnection != null)
            localView = (SurfaceView) MainService.callConnection.createVideoView(true, this, true);
        localView.setVisibility(View.INVISIBLE);
        videoLayout.addView(localView);//视频通话
        localView.setKeepScreenOn(true);
        localView.setZOrderMediaOverlay(true);
        localView.setZOrderOnTop(true);

        if(MainService.callConnection != null)
            remoteView = (SurfaceView) MainService.callConnection.createVideoView(false, this, true);
        remoteView.setVisibility(View.INVISIBLE);
        remoteView.setKeepScreenOn(true);
        remoteView.setZOrderMediaOverlay(true);
        remoteView.setZOrderOnTop(true);
        //remoteLayout.addView(remoteView);
    }

    /**
     * Sets the video surface visibility.
     *
     * @param visible the new video surface visibility
     */
    void setVideoSurfaceVisibility(int visible) {
        if(localView !=null)
            localView.setVisibility(visible);
        if(remoteView !=null)
            remoteView.setVisibility(visible);
    }
    protected void onCallMemberError(int reason){
        setDialValue("");
        setCurrentStatus(CALL_MODE);
        if(reason==MSG_CALLMEMBER_ERROR){
            Utils.DisplayToast(DialActivity.this, "您呼叫的房间号错误或者无注册用户");
        }else if(reason==MSG_CALLMEMBER_NO_ONLINE){
            Utils.DisplayToast(DialActivity.this, "您呼叫的房间号无人在线");
        }else if(reason==MSG_CALLMEMBER_TIMEOUT){
            Utils.DisplayToast(DialActivity.this, "您呼叫的房间号无人应答");
        }else if(reason==MSG_CALLMEMBER_DIRECT_TIMEOUT){
            Utils.DisplayToast(DialActivity.this, "您呼叫的房间直拨电话无人应答");
        }
    }

    public void onCallDirectlyBegin(){
        setCurrentStatus(DIRECT_MODE);
        advertiseHandler.pause();
    }

    public void onCallDirectlyComplete(){
        setCurrentStatus(CALL_MODE);
        callNumber="";
        setDialValue(callNumber);
        advertiseHandler.start();
    }

    //新电话
    public void onRtcConnected(){
        //在此处理faceSdk 视频镜头的冲突
        faceManager.stopScan();
        //新增代码
        surface.setVisibility(View.INVISIBLE);
//        videoLayout.setVisibility(View.VISIBLE);
        Log.e("faceManager","faceManager.stopScan()  停止faceSDK的摄像头");

        setCurrentStatus(ONVIDEO_MODE);
        //setDialValue("");   //取消清空呼叫状态 zhl20170830pm
        advertiseHandler.pause();
    }

    //视频中
    public void onRtcVideoOn(){
        initVideoViews();//初始化视频视图
        MainService.callConnection.buildVideo(remoteView);
        videoLayout.setVisibility(View.VISIBLE);//视频通话
        setVideoSurfaceVisibility(View.VISIBLE);
    }

    //rtc 断开连接
    public void onRtcDisconnect(){
//        Log.e("disConnect", "进入disConnect 方法");
//        //释放Camera
//        if (camera == null) {
//            camera = Camera.open(0);
//        }
//        camera.setPreviewCallback(null);
//        camera.stopPreview();
//        camera.release();
//        camera = null;
//
//        setCurrentStatus(CALL_MODE);
//        advertiseHandler.start();
//        videoLayout.setVisibility(View.INVISIBLE);//视频通话
//        MainService mainService = new MainService();
//        mainService.disconnectCallingConnection();
//        setVideoSurfaceVisibility(View.INVISIBLE);
//
//        //打开facesdk
//        surface.setVisibility(View.VISIBLE);
//        faceManager.startScan();
//        Log.e("faceManager", "faceManager.startScan()");

        Log.e("disConnect","进入disConnect 方法");
        setCurrentStatus(CALL_MODE);
        advertiseHandler.start();
        videoLayout.setVisibility(View.INVISIBLE);//视频通话
        setVideoSurfaceVisibility(View.INVISIBLE);

        //释放Camera
        if(camera !=null){
            Log.e("camera","camera不为空");
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            Log.e("释放相机","释放Camera");
        }else{
            Log.e("camera","camera为空");
        }
        try {
            Thread.sleep(1000);
            Log.e("sleep","休息1秒 释放资源");
            //打开facesdk
            surface.setVisibility(View.VISIBLE);
            faceManager.startScan();
            Log.e("faceManager","faceManager.startScan()");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected void onAdvertiseRefresh(Object obj){
        JSONArray rows=(JSONArray)obj;
        advertiseHandler.initData(rows,dialMessenger);
    }

    protected void onAdvertiseImageChange(Object obj){
        String source=(String)obj;
        source= HttpUtils.getLocalFileFromUrl(source);
        Bitmap bm = BitmapFactory.decodeFile(source);
        imageView.setImageBitmap(bm);
    }

    @Override
    public void onFinger(byte[] fingerData) {
        Message message = Message.obtain();
        message.what = MainService.MSG_FINGER_DETECT;
        message.obj = fingerData;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //人脸识别相关
    @Override
    public void onFaceDetected(int i) {
        if (i > 0) {
            Log.e("omFaceDetected()方法", "识别成功，用户ID为" + i);
            tvToast.setText("提示:欢迎回家(" + i +")");
            Message message = Message.obtain();
            message.what = MainService.MSG_FACE;
            message.obj = null;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else {
            Log.e("omFaceDetected()方法", "识别失败，没有此用户信息");
            tvToast.setText("提示:人脸识别失败");
        }
    }

    //人脸识别相关
    @Override
    public void onMatchedFaceData(int cameraWidth, int cameraHeight, int left, int top, int right, int bottom) {
        mFaceRegistView.setCameraDimension(cameraWidth,cameraHeight);
    }

    //人脸识别相关
    @Override
    protected void onPause() {
        super.onPause();
        if (faceManager != null) {
            faceManager.stopScan();
        }
    }

    //人脸识别相关
    @Override
    protected void onResume() {
        super.onResume();
//        //定时重启APP
//        startTimeToRestartAppService();//开启服务
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("com.example.restart.RECEIVER");
//        registerReceiver(reStartReceiver, filter);
        if (faceManager != null) {
            faceManager.stopScan();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    faceManager.startScan();
                }
            }, 50);
        }
    }

//    //定时重启APP
//    private BroadcastReceiver reStartReceiver = new BroadcastReceiver(){
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.e(TAG,"接收到重启的广播");
//            String action = intent.getAction();
//            if (action.equals("com.example.restart.RECEIVER")) {
//                finish();
//                Intent i = DialActivity.this.getPackageManager()
//                        .getLaunchIntentForPackage(DialActivity.this.getPackageName());
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(i);
//            }
//        }
//    };


}
