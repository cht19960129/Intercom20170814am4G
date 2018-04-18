package com.nodepoint.residential.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.ndk.AdTest.AdLoad;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;

import com.example.building.MBaseActivity;
import com.nodepoint.residential.DialActivity;
import com.nodepoint.residential.InitActivity;
import com.nodepoint.residential.config.DeviceConfig;
import com.nodepoint.residential.gilde.Constant;
import com.nodepoint.residential.gilde.PullFaceReceiver;
import com.nodepoint.residential.gilde.StreamTools;
import com.nodepoint.residential.gilde.ThreadManager;
import com.nodepoint.residential.util.Ajax;
import com.nodepoint.residential.util.AssembleUtil;
import com.nodepoint.residential.util.FingerData;
import com.nodepoint.residential.util.HttpUtils;
import com.nodepoint.residential.util.RfidUtil;
import com.nodepoint.residential.util.SqlUtil;
import com.nodepoint.residential.util.WifiAdmin;
import com.nodepoint.residential.util.finger.IFingerCheck;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.OnMeetingListener;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;
import com.yuntongxun.ecsdk.meeting.intercom.ECInterPhoneMeetingMsg;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;
import com.yuntongxun.ecsdk.meeting.voice.ECVoiceMeetingMsg;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import cn.trinea.android.common.entity.FaceDataInfo;
import jni.http.HttpManager;
import jni.http.HttpResult;
import jni.http.RtcHttpClient;
import rtc.sdk.clt.RtcClientImpl;
import rtc.sdk.common.RtcConst;
import rtc.sdk.common.SdkSettings;
import rtc.sdk.core.RtcRules;
import rtc.sdk.iface.ClientListener;
import rtc.sdk.iface.Connection;
import rtc.sdk.iface.ConnectionListener;
import rtc.sdk.iface.Device;
import rtc.sdk.iface.DeviceListener;
import rtc.sdk.iface.RtcClient;

/**
 * 程序的主要后台服务
 */
public class MainService extends Service implements IFingerCheck {
    private static final String TAG = "MainService";
    public static final String ETH0_MAC_ADDR = "/sys/class/net/eth0/address";
    public static final int SZ_SECURITY_LEVEL = (3);
    public static final int ADVERTISEMENT_WAITING=0;
    public static final int ADVERTISEMENT_REFRESHING=1;

    public static final int REGISTER_ACTIVITY_INIT=1;
    public static final int REGISTER_ACTIVITY_DIAL=3;

    public static final int MSG_GETTOKEN=10001;
    public static final int MSG_LOGIN=20001;
    public static final int MSG_CALLMEMBER=20002;
    public static final int MSG_CREATELOG=20003;
    public static final int MSG_REGISTER=20004;
    public static final int MSG_START_DIAL=20005;
    public static final int MSG_CHECK_PASSWORD=20006;
    public static final int MSG_GUEST_PASSWORD_CHECK=20007;
    public static final int MSG_CARD_INCOME=20008;
    public static final int MSG_DISCONNECT_VIEDO=20009;
    public static final int MSG_CANCEL_CALL=20010;
    public static final int MSG_CANCEL_DIRECT=20011;
    public static final int MSG_DISCONNECT_DIRECT=20012;
    //public static final int MSG_ADVERTISE_INIT=20013;
    public static final int MSG_CHECK_NETWORK=20014;
    public static final int MSG_START_INIT=20015;
    public static final int MSG_CHECK_WIFI=20016;
    public static final int MSG_WIFI_CONNECT=20017;
    public static final int MSG_CHANGE_FINGER=20018;
    public static final int MSG_CHANGE_CARD=20019;
    public static final int MSG_FINGER_OPENLOCK=20020;
    public static final int MSG_CARD_OPENLOCK=20021;
    public static final int MSG_ASSEMBLE_KEY=99922;
    public static final int MSG_CHECK_BLOCKNO=20022;
    public static final int MSG_FINGER_DETECT=20023;
    public static final int MSG_FINGER_DETECT_THREAD_COMPLETE=20024;//手指检测线程完成
    public static final int MSG_START_OFFLINE=20025;

    public static final int MSG_FIND_NEW_VERSION=30001;

    public static final int MSG_FACE=40001;

    public static final int CALL_WAITING=20;
    public static final int CALL_VIDEO_CONNECTING=21;
    public static final int CALL_VIDEO_CONNECTED=22;
    public static final int CALL_VIDEO_CONNECT_FAIL=23;
    public static final int CALL_DIRECT_CONNECTING=24;
    public static final int CALL_DIRECT_CONNECTED=25;
    public static final int CALL_DIRECT_CONNECT_FAIL=26;

    public static final String APP_ID = "71012";//天翼RTC APP_ID
    public static final String APP_KEY ="71007b1c-6b75-4d6f-85aa-40c1f3b842ef";//天翼RTC APP_KEY
    public static final String LOGTAG= "Intercom";

    public static String httpServerToken=null;

    int callType = RtcConst.CallType_A_V;//设置呼叫的方式

    public static String communityName="";
    public static String lockName="";

    public String mac=null;
    public String key=null;
    public int communityId=0;
    public int blockId=0;
    public int inputBlockId=0;
    public int lockId=0;
    public String unitNo="";
    public String tempKey="";
    public String messageFrom=null;
    public int callConnectState=CALL_WAITING;
    public int resetFlag=0;
    public String imageUrl=null;

    RtcClient rtcClient=null;
    Device device = null;
    public static Connection callConnection;
    private String token=null;
    boolean incomingFlag = false;

    protected Messenger initMessenger=null;
    protected Messenger dialMessenger=null;
    protected Messenger serviceMessenger=null;
    protected Handler handler=null;

    protected RfidUtil rfidUtil=null;
    protected AssembleUtil assembleUtil=null;
    //protected FingerUtil fingerUtil=null;
    protected SqlUtil sqlUtil=null;
    protected AdLoad adLoad = null;
    protected CardRecord cardRecord=new CardRecord();
    private ArrayList allUserList=new ArrayList();
    private ArrayList triedUserList=new ArrayList();
    private ArrayList onlineUserList=new ArrayList();
    private ArrayList offlineUserList=new ArrayList();
    private ArrayList rejectUserList=new ArrayList();

    private boolean isRongyunInitialized=false;
    Thread timeoutCheckThread=null;
    private String lastCurrentCallId=null;

    Thread advertisementThread=null;
    Thread connectReportThread=null;
    private JSONArray currentAdvertisementList=new JSONArray();
    private Hashtable<String,String> currentAdvertisementFiles=new Hashtable<String,String>();
    private int advertisementStatus=ADVERTISEMENT_WAITING;

    private WifiAdmin wifiAdmin=null;
    private List<ScanResult> wifiList=null;

    private Thread[] fingerDetectThreads=null;
    private int fingerDetectStatus=0;//手指检测状态
    private int fingerDetectSteps=0;//手指检测步骤
    private boolean fingerDetectResult=false;


    private int lastVersion=0;
    private String lastVersionFile="";
    private String lastVersionStatus="L"; //L: last version N: find new version D：downloading P: pending to install I: installing
    private int downloadingFlag=0; //0：not downloading 1:downloading 2:stop download

    Thread checkThread=null;
    Thread downloadThread=null;
    Thread updateThread=null;

    public MainService() {
    }

    @Override
    public void onCreate() {
        wifiAdmin=new WifiAdmin(this);
        Log.v("MainService","------>create MainService<-------");
        initHandler();
        initUpdateHandler();



    }



    protected void initAdLoad(){
        if(DeviceConfig.IS_FINGER_AVAILABLE){
            adLoad=new AdLoad();
        }
    }

    protected void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == REGISTER_ACTIVITY_INIT){
                    initMessenger = msg.replyTo;
                    init();
                    Log.i("MainService","register init messenger");
                }else if(msg.what == REGISTER_ACTIVITY_DIAL){
                    //startRongyun();  //VoIP呼叫 功能取消 zhl20170922am
                    initAdvertisement();
                    initConnectReport();
                    Log.i("MainService","register Dial messenger");
                    dialMessenger = msg.replyTo;
                }else if(msg.what==MSG_GETTOKEN){
                    //将获取的RTC的数据分析出来（RTC2）
                    onResponseGetToken(msg);
                }else if(msg.what==MSG_LOGIN){
                    onLogin(msg);//获取设备的楼栋 lockId 等信息
                }else if(msg.what==MSG_CALLMEMBER){
                    onCallMember(msg);//开始呼叫用户
                }else if(msg.what==MSG_CREATELOG){
                    onCreateLog(msg);
                }else if(msg.what==MSG_REGISTER){
                    startGetToken();  //获取RTC信息（RTC1）
                }else if(msg.what==MSG_START_DIAL){
                    String[] parameters=(String[])msg.obj;
                    unitNo=parameters[0];
                    imageUrl=parameters[1];
                    startCallMember();//获取呼叫的手机号码  RTC
                }else if(msg.what==MSG_CHECK_PASSWORD){
                    String[] parameters=(String[])msg.obj;
                    tempKey=parameters[0];
                    imageUrl=parameters[1];
                    startCheckGuestPassword();
                }else if(msg.what==MSG_GUEST_PASSWORD_CHECK){
                    onCheckGuestPassword((JSONObject)msg.obj);
                }else if(msg.what==MSG_CARD_INCOME){
                    onCardIncome((String)msg.obj);
                }else if(msg.what==MSG_DISCONNECT_VIEDO){
                    disconnectCallingConnection();//断开视频调用连接
                }else if(msg.what==MSG_CANCEL_CALL){
                    cancelCurrentCall();
                }else if(msg.what==MSG_CANCEL_DIRECT){
                    cancelDirectCall(msg);
                }else if(msg.what==MSG_DISCONNECT_DIRECT){
                    cancelDirectCall(msg);
                }else if(msg.what==MSG_CHECK_NETWORK){
                    checkNetwork();
                }else if(msg.what==MSG_START_INIT){
                    initWhenConnected();
                }else if(msg.what==MSG_START_OFFLINE){   //离线状态登录界面
                    initWhenOffline();
                }else if(msg.what==MSG_CHECK_WIFI){
                    initScanWifi();
                }else if(msg.what==MSG_WIFI_CONNECT){
                    String value=(String)msg.obj;
                    String[] values=value.split(":");
                    int index=new Integer(values[0]);
                    String password="";
                    if(values.length>=2){
                        password=values[1];
                    }

                }else if(msg.what==MSG_CHANGE_FINGER){
                    JSONArray[] lists=(JSONArray[])msg.obj;
                    JSONArray fingerListSuccess=lists[0];
                    JSONArray fingerListFailed=lists[1];
                    startChangeFingerComplete(fingerListSuccess, fingerListFailed);
                }else if(msg.what==MSG_CHANGE_CARD){
                    JSONArray[] lists=(JSONArray[])msg.obj;
                    JSONArray cardListSuccess=lists[0];
                    JSONArray cardListFailed=lists[1];
                    startChangeCardComplete(cardListSuccess, cardListFailed);
                }else if(msg.what==MSG_FINGER_OPENLOCK){
                    int index=(Integer)msg.obj;
                    startFingerOpenLock(index);
                }else if(msg.what==MSG_CARD_OPENLOCK){
                    int index=(Integer)msg.obj;
                    startCardOpenLock(index);
                }else if(msg.what==MSG_ASSEMBLE_KEY){
                    byte keyCode=(Byte)msg.obj;
                    try {
                        onAssembleKey(keyCode);
                    }catch(Exception e){
                    }
                }else if(msg.what==MSG_CHECK_BLOCKNO){
                    String blockNo=(String)msg.obj;
                    startCheckBlockNo(blockNo);
                }else if(msg.what==MSG_FINGER_DETECT){
                    byte[] data=(byte[])msg.obj;
                    onFingerDetect(data);
                }else if(msg.what==MSG_FINGER_DETECT_THREAD_COMPLETE){
                    //Log.v("M:-----","--------other finger"+msg.what);
                    FingerData fingerData=null;
                    if(msg.obj!=null) {
                        fingerData = (FingerData) msg.obj;
                    }
                    onFingerDetectThreadComplete(fingerData);//用户指纹开门记录
                }else if(msg.what == MSG_FIND_NEW_VERSION){
                    String version=(String)msg.obj;
                    onNewVersion(version);
                    Log.i("UpdateService","checked new version "+version);
                }else if(msg.what == MSG_FACE){
                    openLock();
                }
            }
        };
        serviceMessenger = new Messenger(handler);
    }

    protected void initScanWifi(){
        wifiAdmin.openWifi();
        wifiAdmin.startScan();
        wifiList=wifiAdmin.getWifiList();
        sendInitMessenger(InitActivity.MSG_WIFI_LIST,wifiList);
    }

    protected void connectWifi(int index,String password){
        ScanResult scanResult=wifiList.get(index);
        boolean result=wifiAdmin.connectWifi(scanResult.SSID,password);
        if(result){
            sendInitMessenger(InitActivity.MSG_WIFI_CONNECTED);
        }else{
            sendInitMessenger(InitActivity.MSG_WIFI_CONNECT_FAIL);
        }
    }

    //protected void
    protected void initLock(){
        int result=MBaseActivity.openled();
    }

    protected void startRongyun(){
        if(!isRongyunInitialized){
            isRongyunInitialized=true;
            if(DeviceConfig.IS_CALL_DIRECT_AVAILABLE){
                startYuntongxun();
            }
        }
    }

    protected void cancelDirectCall(Message msg){
        if(msg.what==MSG_CANCEL_DIRECT){
            releaseCallDirect();
        }else if(msg.what==MSG_DISCONNECT_DIRECT){
            releaseCallDirect();
        }
    }

    protected void cancelCurrentCall(){
        cancelOtherMembers(null);
        resetCallMode();
        stopTimeoutCheckThread();
    }

    //protected
    public void disconnectCallingConnection(){
        if (callConnection !=null) {
            callConnection.disconnect();
            callConnection=null;
            callingDisconnect();
        }
    }

    //打开主控板检查开关状态
    protected void initRfidUtil(){
        if(DeviceConfig.IS_RFID_AVAILABLE){
            rfidUtil=new RfidUtil(handler);
            try {
                rfidUtil.open();
            }catch(Exception e){
            }
            initLock();
            //sendInitMessenger(InitActivity.MSG_INIT_RFID);
        }
    }

    //打开主控板键盘
    protected void initAssembleUtil(){
        if(DeviceConfig.IS_ASSEMBLE_AVAILABLE){
            assembleUtil=new AssembleUtil(handler);
            try {
                assembleUtil.open();
            }catch(Exception e){
            }
            sendInitMessenger(InitActivity.MSG_INIT_ASSEMBLE);
        }
    }

    //将本地指纹数据读取出来
    protected void initSqlUtil(){
        sqlUtil=new SqlUtil(this);
        sqlUtil.loadFingerList();
        //sqlUtil.initFingerList();
        int fingerNum=sqlUtil.getFingerNum();
        sendInitMessenger(InitActivity.MSG_INIT_SQL,String.valueOf(fingerNum));
    }

    //初始化设备
    protected void init(){
        Log.i("MainService","init MainService");
        initRfidUtil();  //打开主控板检查锁的状态
        Log.i("MainService","init RFID");
        initAssembleUtil();  //打开主控板键盘
        Log.i("MainService","init ASSEMBLE");
        initAdLoad();     //初始化广告加载F
        initSqlUtil();    //将本地指纹数据读取出来
        Log.i("MainService","init SQL");
        if(isNetworkConnectedWithTimeout()){   //检查网络连接情况
            Log.i("MainService","Test Connected");
            initWhenConnected();
        }else{
            Log.i("MainService","Test NoNetwork");
            sendInitMessenger(InitActivity.MSG_NO_NETWORK);
        }
    }

    //设置RTC声音 & 获取服务器设备参数配置
    protected void initWhenConnected(){
        if(initMacAddress()){
            Log.i("MainService","INIT MAC Address");
            initRtcClient();  //设置RTC声音声道
            try{
                initClientInfo();   //获取服务器设备参数配置
            }catch(Exception e){
                Log.v("MainService","onDeviceStateChanged,result="+e.getMessage());
            }
        }
    }

    protected void initWhenOffline(){
        Log.i("MainService","init when offline");
        if(initMacAddress()){
            Log.i("MainService","INIT MAC Address");
            initRtcClient();  //设置RTC声音声道
            try{
                loadInfoFromLocal();
                startDialActivity();
            }catch(Exception e){
                Log.v("MainService","onDeviceStateChanged,result="+e.getMessage());
            }
        }
    }

    //检查网络连接情况（30次）
    public boolean isNetworkConnectedWithTimeout(){
        boolean result=false;
        for(int i=0;i<30;i++){
            if(isNetworkConnected()){
                result=true;
                break;
            }else{
                try {
                    Thread.currentThread().sleep(1000);
                }catch(InterruptedException e){}
            }
        }
        return result;
    }

    //检查网络连接情况
    public boolean isNetworkConnected(){
        String result = null;
        try {
            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
            // 读取ping的内容，可以不加
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            Log.d("------ping-----", "result content : " + stringBuffer.toString());
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("----result---", "result = " + result);
        }
        return false;
    }

    protected void checkNetwork(){
        if(isNetworkConnected()){
            sendInitMessenger(InitActivity.MSG_CONNECT_SUCCESS);
        }else{
            sendInitMessenger(InitActivity.MSG_CONNECT_FAIL);
        }
    }

    private String parseByte(byte b) {
        String s = "00" + Integer.toHexString(b)+":";
        return s.substring(s.length() - 3);
    }

    protected String getWifiMac(){
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();
        return mac;
    }

    protected String getEthMac() {
        byte[] mac = null;
        StringBuffer sb = new StringBuffer();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();

                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (ip.isAnyLocalAddress() || !(ip instanceof Inet4Address) || ip.isLoopbackAddress())
                        continue;
                    if (ip.isSiteLocalAddress())
                        mac = ni.getHardwareAddress();
                    else if (!ip.isLinkLocalAddress()) {
                        mac = ni.getHardwareAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if(mac != null){
            for(int i=0 ;i<mac.length ;i++){
                sb.append(parseByte(mac[i]));
            }
            return sb.substring(0, sb.length()-1);
        }else{
            return null;
        }
    }

    protected String getMac(){
        String mac=getWifiMac();
        if(mac==null){
            return getEthMac();
        }else{
            return mac;
        }
    }

    protected String retrieveEthMac(){
        String mac=null;
        try {
            return readLine(ETH0_MAC_ADDR);
        } catch (IOException e) {
        }
        return mac;
    }

    private String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    protected boolean initMacAddress(){
        String mac= getMac();
        if(mac==null||mac.length()==0){
            Message message = Message.obtain();
            message.what = InitActivity.MSG_NO_MAC_ADDRESS;
            try {
                initMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return false;
        }else{
            this.mac=mac;
            this.key=mac.replace(":", "");
            Message message = Message.obtain();
            message.what = InitActivity.MSG_GET_MAC_ADDRESS;
            message.obj=mac;
            try {
                initMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    //设置RTC声音声道
    private void initRtcClient() {
        rtcClient = new RtcClientImpl();
        rtcClient.initialize(this.getApplicationContext(), new ClientListener() {
            @Override   //初始化结果回调
            public void onInit(int result) {
                Log.v("MainService","onInit,result="+result);//常见错误9003:网络异常或系统时间差的太多
                if(result == 0) {
                    rtcClient.setAudioCodec(RtcConst.ACodec_OPUS); //声道设置
                    rtcClient.setVideoCodec(RtcConst.VCodec_VP8);
                    if(DeviceConfig.VIDEO_STATUS==0){
                        rtcClient.setVideoAttr(RtcConst.Video_SD);
                    }else if(DeviceConfig.VIDEO_STATUS==1){
                        rtcClient.setVideoAttr(RtcConst.Video_FL);
                    }else if(DeviceConfig.VIDEO_STATUS==2){
                        rtcClient.setVideoAttr(RtcConst.Video_HD);
                    }else if(DeviceConfig.VIDEO_STATUS==3){
                        rtcClient.setVideoAttr(RtcConst.Video_720P);
                    }else if(DeviceConfig.VIDEO_STATUS==4){
                        rtcClient.setVideoAttr(RtcConst.Video_1080P);
                    }
                    rtcClient.setVideoAdapt(DeviceConfig.VIDEO_ADAPT);
                }
            }
        });
    }

    //获取服务器设备参数配置
    protected boolean getClientInfo() throws JSONException {
        Log.v("MainService","start get client info");
        JSONObject data=new JSONObject();
        data.put("username",mac);
        data.put("password",key);
        boolean resultValue=false;
        try {
            Log.v("MainService","POST Login ajax");
            URL url = new URL(DeviceConfig.SERVER_URL+"/app/auth/deviceLogin");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST");
            if(httpServerToken!=null) {
                connection.setRequestProperty("Authorization", "Bearer " + httpServerToken);
            }
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();
            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream(), "UTF-8");
            out.append(data.toString());
            out.flush();
            out.close();
            InputStream is = connection.getInputStream();
            String result = HttpUtils.readMyInputStream(is);
            Log.v("MainService","response="+result);
            JSONObject resultObj=Ajax.getJSONObject(result);
            int code=resultObj.getInt("code");
            if (code == 0) {
                resultValue=true;
                try {
                    httpServerToken = resultObj.getString("token");
                }catch(Exception e){
                    httpServerToken=null;
                }
                initDeviceConfig(resultObj);
            }
            Message message = handler.obtainMessage();
            message.what =MSG_LOGIN;
            resultObj.put("mac",this.mac);
            message.obj = resultObj;
            handler.sendMessage(message);
        }catch (IOException e) {
            //e.printStackTrace();
            Log.v("MainService","response error="+e.getMessage());
            Message message = Message.obtain();
            message.what = InitActivity.MSG_LOGIN_ERROR;
            try {
                initMessenger.send(message);
            } catch (RemoteException er) {
                e.printStackTrace();
            }
        }
        return resultValue;
    }

    //设备参数设置
    protected void initDeviceConfig(JSONObject resultObj){
        try {
            JSONObject deviceUser=resultObj.getJSONObject("user");
            resetFlag=deviceUser.getInt("resetFlag");
            JSONObject config = resultObj.getJSONObject("config");
            if(config!=null){
                DeviceConfig.PARALL_WAIT_TIME=1000*config.getInt("parallWaitTime");
                DeviceConfig.SERIAL_WAIT_TIME=1000*config.getInt("serialWaitTime");

                DeviceConfig.AD_INIT_WAIT_TIME=1000*60*config.getInt("adInitWaitTime");
                DeviceConfig.AD_REFRESH_WAIT_TIME=1000*60*config.getInt("adRefreshWaitTime");
                DeviceConfig.CONNECT_REPORT_WAIT_TIME=1000*60*config.getInt("connectReportWaitTime");
                DeviceConfig.MAX_DIRECT_CALL_TIME=1000*config.getInt("maxDirectCallTime");
                DeviceConfig.PASSWORD_WAIT_TIME=1000*config.getInt("passwordWaitTime");

                DeviceConfig.UNIT_NO_LENGTH=config.getInt("unitNoLength");
                DeviceConfig.BLOCK_NO_LENGTH=config.getInt("blockNoLength");

                DeviceConfig.VOLUME_STREAM_MUSIC=config.getInt("musicVolume");
                DeviceConfig.VOLUME_STREAM_VOICE_CALL=config.getInt("voiceCallVolume");
                DeviceConfig.VOLUME_STREAM_RING=config.getInt("ringVolume");
                DeviceConfig.VOLUME_STREAM_SYSTEM=config.getInt("systemVolume");

                try {
                    DeviceConfig.VIDEO_STATUS = config.getInt("videoStatus");
                    DeviceConfig.VIDEO_ADAPT = config.getInt("videoAdapt");
                }catch(Exception e){
                    DeviceConfig.VIDEO_STATUS = 0;
                    DeviceConfig.VIDEO_ADAPT = 1;
                }
            }
        }catch(JSONException e){
        }
    }

    protected void initClientInfo(){
        new Thread(){
            public void run(){
                boolean result=false;
                try {
                    do {
                        result=getClientInfo();
                        if(!result){
                            sleep(1000*30);
                        }
                    }while(!result);
                }catch(Exception e){}
            }
        }.start();
    }

    private void onAssembleKey(byte keyCode)throws RemoteException{
        Message message = Message.obtain();
        message.what = MSG_ASSEMBLE_KEY;
        if(keyCode==0){
            message.obj= KeyEvent.KEYCODE_0;
        }else if(keyCode==1){
            message.obj= KeyEvent.KEYCODE_1;
        }else if(keyCode==2){
            message.obj= KeyEvent.KEYCODE_2;
        }else if(keyCode==3){
            message.obj= KeyEvent.KEYCODE_3;
        }else if(keyCode==4){
            message.obj= KeyEvent.KEYCODE_4;
        }else if(keyCode==5){
            message.obj= KeyEvent.KEYCODE_5;
        }else if(keyCode==6){
            message.obj= KeyEvent.KEYCODE_6;
        }else if(keyCode==7){
            message.obj= KeyEvent.KEYCODE_7;
        }else if(keyCode==8){
            message.obj= KeyEvent.KEYCODE_8;
        }else if(keyCode==9){
            message.obj= KeyEvent.KEYCODE_9;
        }else if(keyCode==10){
            message.obj=KeyEvent.KEYCODE_STAR;
        }else if(keyCode==11){
            message.obj=KeyEvent.KEYCODE_POUND;
        }
        Log.v("MainService","key message="+message.obj);
        if(dialMessenger!=null){
            Log.v("MainService","send to Dial messenger"+message.obj);
            dialMessenger.send(message);
        }else{
            Log.v("MainService","send to Init messenger"+message.obj);
            initMessenger.send(message);
        }
    }

    //
    private void onCardIncome(String card){
        if(!this.cardRecord.checkLastCard(card)){
            if(checkCardAvailable(card)){
                openLock();
                startCardAccessLog(card);//开始卡访问日志
            }else{
                sendDialMessenger(DialActivity.MSG_INVALID_CARD);
            }
        }
    }

    private boolean checkCardAvailable(String cardNo){
        return sqlUtil.checkCardAvailable(cardNo);
    }

    private void startCardAccessLog(final String cardNo){
        new Thread(){
            public void run(){
                onCardAccessLog(cardNo);
            }
        }.start();
    }

    private void startCheckBlockNo(final String blockNo){
        new Thread(){
            public void run(){
                onCheckBlockNo(blockNo);
            }
        }.start();
    }


    //楼栋号
    private void onCheckBlockNo(String blockNo){
        try{
            String url=DeviceConfig.SERVER_URL+"/app/device/checkBlockNo?communityId="+this.communityId;
            url=url+"&blockNo="+blockNo;
            try{
                URL thisUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
                conn.setRequestMethod("GET");
                if(httpServerToken!=null) {
                    conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
                }
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                if (code == 200) {
                    InputStream is = conn.getInputStream();
                    String result = HttpUtils.readMyInputStream(is);
                    JSONObject resultObj=Ajax.getJSONObject(result);
                    int resultCode=resultObj.getInt("code");
                    if(resultCode==0){
                        inputBlockId=resultObj.getInt("blockId");
                    }else{
                        inputBlockId=0;
                    }
                    sendDialMessenger(DialActivity.MSG_CHECK_BLOCKNO,inputBlockId);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
        }
    }

    //开始卡访问日志
    private void onCardAccessLog(String cardNo){
        try{
            String url=DeviceConfig.SERVER_URL+"/app/device/createCardAccessLog?communityId="+this.communityId;
            url=url+"&lockId="+this.lockId;
            url=url+"&cardNo="+cardNo;
            try{
                URL thisUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
                conn.setRequestMethod("GET");
                if(httpServerToken!=null) {
                    conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
                }
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                if (code == 200) {
                    InputStream is = conn.getInputStream();
                    String result = HttpUtils.readMyInputStream(is);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
        }
    }

    private void startCheckGuestPassword(){
        new Thread(){
            public void run(){
                checkGuestPassword();
            }
        }.start();
    }

    //访客密码
    private void checkGuestPassword(){
        try{
            String url=DeviceConfig.SERVER_URL+"/app/device/openDoorByTempKey?from=";
            url=url+this.key;
            url=url+"&communityId="+this.communityId;
            url=url+"&lockId="+this.lockId;
            url=url+"&tempKey="+this.tempKey;
            if(imageUrl!=null) {
                url = url + "&imageUrl=" + URLEncoder.encode(this.imageUrl, "UTF-8");
            }
            try{
                URL thisUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
                conn.setRequestMethod("GET");
                if(httpServerToken!=null) {
                    conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
                }
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int code = conn.getResponseCode();
                if (code == 200) {
                    InputStream is = conn.getInputStream();
                    String result = HttpUtils.readMyInputStream(is);

                    Message message = handler.obtainMessage();
                    message.what =MSG_GUEST_PASSWORD_CHECK;
                    message.obj = Ajax.getJSONObject(result);
                    handler.sendMessage(message);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
        }
    }

    private void onCheckGuestPassword(JSONObject result){
        try{
            int code=result.getInt("code");
            if(code==0){
                openLock();
            }
            Message message = Message.obtain();
            message.what = DialActivity.MSG_PASSWORD_CHECK;
            message.obj=code;
            try {
                dialMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }catch(JSONException e){
        }
    }

    //将获取的RCT数据分析出来
    private void onResponseGetToken(Message msg) {
        HttpResult ret = (HttpResult) msg.obj;
        Log.v("MainService", "handleMessage getCapabilityToken status:" + ret.getStatus());
        JSONObject jsonrsp = (JSONObject) ret.getObject();
        if (jsonrsp != null && jsonrsp.isNull("code") == false) {
            try {
                String code = jsonrsp.getString(RtcConst.kcode);
                String reason = jsonrsp.getString(RtcConst.kreason);
                Log.v("MainService", "Response getCapabilityToken code:" + code + " reason:" + reason);
                if (code.equals("0")) {
                    //前端输出提示
                    token = jsonrsp.getString(RtcConst.kcapabilityToken);
                    Log.v("MainService", "handleMessage getCapabilityToken:" + token);

                    Message message = Message.obtain();
                    message.what = InitActivity.MSG_GET_TOKEN;
                    message.obj = token;
                    try {
                        initMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    //注册RTC & 跳转主界面
                    rtcRegister();
                } else {
                    onGetTokenError();  //前端提示错误信息
                    Log.e(TAG,"获取天翼token信息失败 11111111 ");
                    Log.v("MainService", "获取token失败 [status:" + ret.getStatus() + "]" + ret.getErrorMsg());
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                onGetTokenError();  //前端提示错误信息
                Log.e(TAG,"获取天翼token信息失败 22222222 ");
                Log.v("MainService", "获取token失败 [status:" + e.getMessage() + "]");
            }
        } else {
            onGetTokenError();  //前端提示错误信息
        }
    }

    protected void onGetTokenError(){
        Message message = Message.obtain();
        message.what = InitActivity.MSG_CANNOT_GET_TOKEN;
        try {
            initMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void loadInfoFromLocal(){
        SharedPreferences sharedPreferences= getSharedPreferences("residential",Activity.MODE_PRIVATE);
        communityId=sharedPreferences.getInt("communityId",0);
        blockId=sharedPreferences.getInt("blockId",0);
        lockId=sharedPreferences.getInt("lockId",0);
        communityName=sharedPreferences.getString("communityName", "");
        lockName=sharedPreferences.getString("lockName", "");

        
    }

    protected void saveInfoIntoLocal(int communityId,int blockId,int lockId,String communityName,String lockName){
        Constant.communityId = communityId;
        Constant.lockId = lockId;
        Log.e("saveInfoIntoLocal:", "communityId: " + communityId + "  lockId: " + lockId);
        SharedPreferences sharedPreferences= getSharedPreferences("residential",Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("communityId", communityId);
        editor.putInt("blockId", blockId);
        editor.putInt("lockId",lockId);
        editor.putString("communityName",communityName);
        editor.putString("lockName",lockName);
        editor.commit();
    }

    //登录的时候获取 机器设备的信息
    protected void onLogin(Message msg){
        JSONObject result=(JSONObject) msg.obj;
        try {
            int code=result.getInt("code");
            JSONObject user=null;
            if(code==0){
                user = result.getJSONObject("user");
                this.blockId=(Integer)user.get("blockId");
                this.communityId=(Integer)user.get("communityId");
//                Constant.communityId = (String) user.get("communityId");
                this.lockId=(Integer)user.get("rid");
//                Constant.lockId= (String) user.get("rid");
                lockName=user.getString("lockName");
                communityName=user.getString("communityName");
                if(this.blockId==0){
                    DeviceConfig.DEVICE_TYPE="C";
                }
                saveInfoIntoLocal(communityId,blockId,lockId,communityName,lockName);
            }

            Message message = Message.obtain();
            message.what = InitActivity.MSG_LOGIN;
            message.obj=result;
            try {
                initMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }catch(JSONException e){}
    }

    /**
     * 终端直接从rtc平台获取token，应用产品需要通过自己的服务器来获取，rtc平台接口请参考开发文档<2.5>章节.
     */
    private void getTokenFromServer() {
        Log.e("Token信息", "开始获取Token");
        RtcConst.UEAPPID_Current = RtcConst.UEAPPID_Self;//账号体系，包括私有、微博、QQ等，必须在获取token之前确定。
        JSONObject jsonobj = HttpManager.getInstance().CreateTokenJson(0, key, RtcHttpClient.grantedCapabiltyID, "");
        HttpResult ret = HttpManager.getInstance().getCapabilityToken(jsonobj, APP_ID, APP_KEY);
        Log.e(TAG,"获取token:"+ret.getStatus()+" reason:"+ret.getErrorMsg());
        Message msg = new Message();
        msg.what = MSG_GETTOKEN;
        msg.obj = ret;
        handler.sendMessage(msg);
    }

    private void startGetToken() {
        new Thread(){
            public void run(){
                //从服务器获取token
                getTokenFromServer();
                try{
                    sleep(500);
                }catch (Exception e){}
            }
        }.start();
    }

    //注册RTC & 跳转主界面
    private void rtcRegister() {
        Log.v("MainService","rtcRegister:"+key+"token:"+token);
        if(communityId>0&&token!=null){
            try {

                //注册RTC
                JSONObject jargs = SdkSettings.defaultDeviceSetting();
                jargs.put(RtcConst.kAccPwd,token);
                //账号格式形如“账号体系-号码~应用id~终端类型”，以下主要设置账号内各部分内容，其中账号体系的值要在获取token之前确定，默认为私有账号
                jargs.put(RtcConst.kAccAppID,APP_ID);//应用id
                jargs.put(RtcConst.kAccUser,key); //号码
                jargs.put(RtcConst.kAccType,RtcConst.UEType_Current);//终端类型
                device = rtcClient.createDevice(jargs.toString(), deviceListener); //注册

                //前端提示信息
                Message message = Message.obtain();
                message.what = InitActivity.MSG_RTC_REGISTER;
                Log.e("注册信息","前面为注册成功");
                try {
                    initMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Log.e("跳转页面","跳转到主界面");
                //跳转主界面
                onRegisterCompleted();
            } catch (JSONException e) {
                Message message = Message.obtain();
                message.what = InitActivity.MSG_RTC_CANNOT_REGISTER;
                try {
                    initMessenger.send(message);
                } catch (RemoteException err) {
                    err.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

    protected void onRegisterCompleted(){
        startDialActivity();
    }

    //跳转
    protected void startDialActivity(){
        Intent intent = new Intent(getBaseContext(),DialActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    protected void sendMessenger(int code,Object object){
        Message message = handler.obtainMessage();
        message.what =code;
        message.obj = object;
        handler.sendMessage(message);
    }

    protected void sendDialMessenger(int code){
        Message message = Message.obtain();
        message.what = code;
        try {
            dialMessenger.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void sendDialMessenger(int code,Object object){
        Message message = Message.obtain();
        message.what = code;
        message.obj=object;
        try {
            dialMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void sendInitMessenger(int code){
        Message message = Message.obtain();
        message.what = code;
        try {
            initMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void sendInitMessenger(int code,Object object){
        Message message = Message.obtain();
        message.what = code;
        message.obj=object;
        try {
            initMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void startYuntongxun(){
        // 判断SDK是否已经初始化，如果已经初始化则可以直接调用登陆接口
        // 没有初始化则先进行初始化SDK，然后调用登录接口注册SDK
        if(!ECDevice.isInitialized()) {
            ECDevice.initial(this.getApplicationContext(), new ECDevice.InitListener() {
                @Override
                public void onInitialized() {
                    // SDK已经初始化成功
                    initYuntongxunHandler();
                }

                @Override
                public void onError(Exception exception) {
                    sendDialMessenger(DialActivity.ON_YUNTONGXUN_INIT_ERROR);
                    // SDK 初始化失败,可能有如下原因造成
                    // 1、可能SDK已经处于初始化状态
                    // 2、SDK所声明必要的权限未在清单文件（AndroidManifest.xml）里配置、
                    //    或者未配置服务属性android:exported="false";
                    // 3、当前手机设备系统版本低于ECSDK所支持的最低版本（当前ECSDK支持
                    //    Android Build.VERSION.SDK_INT 以及以上版本）
                }
            });
        }
    }

    protected void initYuntongxunHandler(){
        // 构建注册所需要的参数信息
        //5.0.3的SDK初始参数的方法：ECInitParams params = new ECInitParams();
        //5.1.*以上版本如下：
        ECInitParams params = ECInitParams.createParams();
        //自定义登录方式：
        //测试阶段Userid可以填写手机
        params.setUserid(this.key);
        params.setAppKey("8aaf0708560cf0f501560d03a9a80023");
        params.setToken("900dd8bdcd111f811b1e609026811bb3");
        // 设置登陆验证模式（是否验证密码）NORMAL_AUTH-自定义方式
        params.setAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);
        // 1代表用户名+密码登陆（可以强制上线，踢掉已经在线的设备）
        // 2代表自动重连注册（如果账号已经在其他设备登录则会提示异地登陆）
        // 3 LoginMode（强制上线：FORCE_LOGIN  默认登录：AUTO）
        params.setMode(ECInitParams.LoginMode.FORCE_LOGIN);

        //voip账号+voip密码方式：
//        params.setUserid("8015009900000002");
//        params.setPwd("ZdVO83Jj");
//        params.setAppKey("8aaf0708560cf0f501560d03a9a80023");
//        // 设置登陆验证模式（是否验证密码）PASSWORD_AUTH-密码登录方式
//        params.setAuthType(ECInitParams.LoginAuthType.PASSWORD_AUTH);
//        // 1代表用户名+密码登陆（可以强制上线，踢掉已经在线的设备）
//        // 2代表自动重连注册（如果账号已经在其他设备登录则会提示异地登陆）
//        // 3 LoginMode（强制上线：FORCE_LOGIN  默认登录：AUTO）
//        params.setMode(ECInitParams.LoginMode.FORCE_LOGIN);

        // 如果是v5.1.8r开始版本建议使用
        // ECDevice.setOnDeviceConnectListener（new ECDevice.OnECDeviceConnectListener()）
        // 如果是v5.1.8r以前版本设置登陆状态回调如下
        params.setOnDeviceConnectListener(new ECDevice.OnECDeviceConnectListener() {
            public void onConnect() {
                // 兼容4.0，5.0可不必处理
            }

            @Override
            public void onDisconnect(ECError error) {
                // 兼容4.0，5.0可不必处理
            }
            @Override
            public void onConnectState(ECDevice.ECConnectState state, ECError error) {
                if(state == ECDevice.ECConnectState.CONNECT_FAILED ){
                    if(error.errorCode == SdkErrorCode.SDK_KICKED_OFF) {
                        //账号异地登陆
                        sendDialMessenger(DialActivity.ON_YUNTONGXUN_LOGIN_FAIL);
                        DeviceConfig.IS_CALL_DIRECT_AVAILABLE=false;
                    }
                    else
                    {
                        //连接状态失败
                        sendDialMessenger(DialActivity.ON_YUNTONGXUN_LOGIN_FAIL);
                        DeviceConfig.IS_CALL_DIRECT_AVAILABLE=false;
                    }
                    return;
                }
                else if(state == ECDevice.ECConnectState.CONNECT_SUCCESS) {
                    // 登陆成功
                    sendDialMessenger(DialActivity.ON_YUNTONGXUN_LOGIN_SUCCESS);
                }
            }
        });

        // 如果是v5.1.8r版本 ECDevice.setOnChatReceiveListener(new OnChatReceiveListener())
        // 5.1.7r及以前版本设置SDK接收消息回调
        params.setOnChatReceiveListener(new OnChatReceiveListener() {
            @Override
            public void OnReceivedMessage(ECMessage msg) {
                // 收到新消息
            }

            @Override
            public void onReceiveMessageNotify(ECMessageNotify ecMessageNotify) {

            }

            @Override
            public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage notice) {
                // 收到群组通知消息（有人加入、退出...）
                // 可以根据ECGroupNoticeMessage.ECGroupMessageType类型区分不同消息类型
            }

            @Override
            public void onOfflineMessageCount(int count) {
                // 登陆成功之后SDK回调该接口通知账号离线消息数
            }

            @Override
            public int onGetOfflineMessage() {
                return 0;
            }

            @Override
            public void onReceiveOfflineMessage(List msgs) {
                // SDK根据应用设置的离线消息拉去规则通知应用离线消息
            }

            @Override
            public void onReceiveOfflineMessageCompletion() {
                // SDK通知应用离线消息拉取完成
            }

            @Override
            public void onServicePersonVersion(int version) {
                // SDK通知应用当前账号的个人信息版本号
            }

            @Override
            public void onReceiveDeskMessage(ECMessage ecMessage) {

            }

            @Override
            public void onSoftVersion(String s, int i) {

            }
        });

        // 获得SDKVoIP呼叫接口
        // 注册VoIP呼叫事件回调监听
        ECVoIPCallManager callInterface = ECDevice.getECVoIPCallManager();
        if(callInterface != null) {
            callInterface.setOnVoIPCallListener(new ECVoIPCallManager.OnVoIPListener() {
                @Override
                public void onVideoRatioChanged(VideoRatio videoRatio) {

                }

                @Override
                public void onSwitchCallMediaTypeRequest(String s, ECVoIPCallManager.CallType callType) {

                }

                @Override
                public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {

                }

                @Override
                public void onDtmfReceived(String s, char c) {
                    if(c=='#'){
                        openLock();
                    }
                }

                @Override
                public void onCallEvents(ECVoIPCallManager.VoIPCall voipCall) {
                    // 处理呼叫事件回调
                    if(voipCall == null) {
                        Log.e("SDKCoreHelper", "handle call event error , voipCall null");
                        return ;
                    }
                    // 根据不同的事件通知类型来处理不同的业务
                    ECVoIPCallManager.ECCallState callState = voipCall.callState;
                    switch (callState) {
                        case ECCALL_PROCEEDING:
                            // 正在连接服务器处理呼叫请求
                            break;
                        case ECCALL_ALERTING:
                            // 呼叫到达对方客户端，对方正在振铃
                            if(callConnectState==CALL_DIRECT_CONNECTING){
                                sendDialMessenger(DialActivity.MSG_CALLMEMBER_DIRECT_DIALING);
                            }
                            break;
                        case ECCALL_ANSWERED:
                            // 对方接听本次呼叫
                            if(callConnectState==CALL_DIRECT_CONNECTING){
                                sendDialMessenger(DialActivity.MSG_CALLMEMBER_DIRECT_SUCCESS);
                            }
                            callConnectState=CALL_DIRECT_CONNECTED;
                            startCallDirectTimeoutChecking(lastCurrentCallId);
                            break;
                        case ECCALL_FAILED:
                            // 本次呼叫失败，根据失败原因播放提示音
                            if(callConnectState==CALL_DIRECT_CONNECTING){
                                sendDialMessenger(DialActivity.MSG_CALLMEMBER_DIRECT_FAILED);
                                callMemberDirectly();
                            }
                            break;
                        case ECCALL_RELEASED:
                            // 通话释放[完成一次呼叫]
                            if(callConnectState==CALL_DIRECT_CONNECTED){
                                sendDialMessenger(DialActivity.MSG_CALLMEMBER_DIRECT_COMPLETE);
                            }
                            resetCallMode();
                            break;
                        default:
                            Log.e("SDKCoreHelper", "handle call event error , callState " + callState);
                            break;
                    }
                }
            });
        }

        // 注册会议消息处理监听
        if(ECDevice.getECMeetingManager() != null) {
            ECDevice.getECMeetingManager().setOnMeetingListener(new OnMeetingListener() {
                @Override
                public void onReceiveInterPhoneMeetingMsg(ECInterPhoneMeetingMsg msg) {
                    // 处理实时对讲消息Push
                }

                @Override
                public void onReceiveVoiceMeetingMsg(ECVoiceMeetingMsg msg) {
                    // 处理语音会议消息push
                }

                @Override
                public void onReceiveVideoMeetingMsg(ECVideoMeetingMsg msg) {
                    // 处理视频会议消息Push（暂未提供）
                }
                @Override
                public void onVideoRatioChanged(VideoRatio videoRatio){

                }
            });
        }

        if(params.validate()) {
            // 判断注册参数是否正确
            ECDevice.login(params);
        }
    }

    public void startCallDirectTimeoutChecking(final String thisCallId){
        new Thread(){
            public void run(){
                try {
                    sleep(DeviceConfig.MAX_DIRECT_CALL_TIME);
                    if(thisCallId.equals(lastCurrentCallId)){
                        releaseCallDirect();
                    }
                }catch(Exception e){}
            }
        }.start();
    }

    protected synchronized void setLastCurrentCallId(String lastCurrentCallId){
        this.lastCurrentCallId=lastCurrentCallId;
    }

    public void startCallDirect(String mobile) {
        String callId=ECDevice.getECVoIPCallManager().makeCall(ECVoIPCallManager.CallType.DIRECT, mobile);
        System.out.println(callId);
        setLastCurrentCallId(callId);
    }

    public void releaseCallDirect(){
        if(lastCurrentCallId!=null){
            ECDevice.getECVoIPCallManager().releaseCall(lastCurrentCallId);
            setLastCurrentCallId(null);
        }
    }

    protected void startCallMember(){
        new Thread(){
            public void run(){
                callMember();
            }
        }.start();
    }

    //拨打用户   获取拨打用户的 房间内的信息  参数  kay  communityId  blockId  blockId(房间号)
    protected void callMember(){
        try{
            String url= DeviceConfig.SERVER_URL+"/app/device/callAllMembers?from=";
            url=url+this.key;
            url=url+"&communityId="+this.communityId;
            if(DeviceConfig.DEVICE_TYPE.equals("C")){
                url=url+"&blockId="+this.inputBlockId;
            }else{
                url=url+"&blockId="+this.blockId;
            }
            url=url+"&unitNo="+this.blockId;
            try{
                URL thisUrl = new URL(url);
                Log.e(TAG,"获取拨打用户的 房间内的信息url "+ url );
                HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
                conn.setRequestMethod("GET");
                if(httpServerToken!=null) {
                    conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
                }
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                if (code == 200) {//判断是否连接网络
                    InputStream is = conn.getInputStream();
                    String result = HttpUtils.readMyInputStream(is);
                    Message message = handler.obtainMessage();
                    message.what = MSG_CALLMEMBER;
                    message.obj = Ajax.getJSONObject(result);
                    handler.sendMessage(message);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }catch(Exception e){
        }
    }

    //RTC获取拨打人员信息
    protected void onCallMember(Message msg){
        try {
            JSONObject result=(JSONObject) msg.obj;
            Log.e("呼叫用户的 result", String.valueOf(result));
            JSONArray userList = (JSONArray) result.get("userList");
            Log.e("获取房间内的人信息", String.valueOf(userList));
            JSONArray unitDeviceList = (JSONArray) result.get("unitDeviceList");   //单位设备列表
            if((userList!=null&&userList.length()>0)||(unitDeviceList!=null&&unitDeviceList.length()>0)){
                //先全部清空    后添加数据    避免下次用的时候会有数据
                allUserList.clear(); //全部
                triedUserList.clear(); //试验过的
                onlineUserList.clear(); //在线UserList
                offlineUserList.clear(); //离线
                rejectUserList.clear(); //拒绝
                callConnectState=CALL_VIDEO_CONNECTING;
                if(unitDeviceList!=null){  //单位设备列表 不等于空
                    for(int i=0;i<unitDeviceList.length();i++){
                        allUserList.add(unitDeviceList.get(i));
                    }
                }
                if(userList!=null){
                    for(int i=0;i<userList.length();i++){
                        allUserList.add(userList.get(i));
                    }
                }

                if(DeviceConfig.CALL_MEMBER_MODE==DeviceConfig.CALL_MEMBER_MODE_PARALL){
                    sendCallMessageParall();
                }else{
                    sendCallMessageSerial();
                }
            }else{
                Message message = Message.obtain();
                message.what = DialActivity.MSG_CALLMEMBER_ERROR;
                try {
                    dialMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void sendCallMessageParall(){
        if(callConnectState==CALL_VIDEO_CONNECTING){
            try{
                JSONObject data = new JSONObject();
                data.put("command","call");
                data.put("from",this.key);
                data.put("imageUrl",this.imageUrl);
                data.put("communityName",this.communityName);
                data.put("lockName",this.lockName);
                if(allUserList.size()>0){
                    JSONObject userObject=(JSONObject)allUserList.remove(0);
                    String username=(String)userObject.get("username");
                    if(username.length()==17){
                        username=username.replaceAll(":","");
                    }
                    String userUrl= RtcRules.UserToRemoteUri_new(username,RtcConst.UEType_Any);//username  用户名
                    int sendResult=device.sendIm(userUrl,"cmd/json",data.toString());
                    Log.v("MainService", "sendIm(): "+sendResult);
                    //视频通话
                    triedUserList.add(userObject);
                }else{
                    afterTryAllMembers();
                }
            }catch(JSONException e){
            }
        }
    }

    //全部人员尝试并行呼叫后，检查在线的用户，如果有在线用户则等待，否则立即启动直拨
    protected void afterTryAllMembers(){
        boolean needWait=false;
        if(DeviceConfig.IS_PUSH_AVAILABLE){
            needWait=triedUserList.size()>0;
            pushCallMessage();
        }else{
            needWait=onlineUserList.size()>0;
        }
        if(needWait){ //检查在线人数,大于0则等待一段时间
            startTimeoutChecking();
        }else{
            if(DeviceConfig.IS_CALL_DIRECT_AVAILABLE){ //如果支持直拨，立刻进入直拨电话模式
                sendDialMessenger(DialActivity.MSG_CALLMEMBER_TIMEOUT_AND_TRY_DIRECT);
                cancelOtherMembers(null);
                startCallMemberDirectly();
            }else{ //告诉用户无人在线
                sendDialMessenger(DialActivity.MSG_CALLMEMBER_NO_ONLINE);
            }
        }
    }

    protected void pushCallMessage(){
        String pushList=null;
        for(int j=0;j<offlineUserList.size();j++){
            JSONObject userObject=(JSONObject)offlineUserList.get(j);
            String username=null;
            try {
                username = (String) userObject.get("username");
            }catch(JSONException e){}
            if(username.length()!=17){
                if(pushList==null){
                    pushList=username;
                }else{
                    pushList=pushList+","+username;
                }
            }
        }
        if(pushList!=null){
            startPushCallMessage(pushList);
        }
    }

    protected void startPushCallMessage(final String pushList){
        Thread thread=new Thread(){
            public void run() {
                try {
                    onPushCallMessage(pushList);
                }catch(IOException e){
                }catch(JSONException e){
                }
            }
        };
        thread.start();
    }

    protected void onPushCallMessage(String pushList) throws JSONException,IOException{
        JSONObject data=new JSONObject();
        data.put("pushList",pushList);
        data.put("from",key);
        data.put("unitName",communityName+unitNo);
        String dataStr=data.toString();
        URL url = new URL(DeviceConfig.SERVER_URL+"/app/device/pushCallMessage");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");
        if(httpServerToken!=null) {
            connection.setRequestProperty("Authorization", "Bearer " + httpServerToken);
        }
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect();
        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream(), "UTF-8");
        out.append(dataStr);
        out.flush();
        out.close();
        InputStream is = connection.getInputStream();
        String result = HttpUtils.readMyInputStream(is);
        JSONObject resultObj=Ajax.getJSONObject(result);
        int code=resultObj.getInt("code");
        if(code==0){
        }else{
        }
    }

    protected void startCallMemberDirectly(){
        callConnectState=CALL_DIRECT_CONNECTING;
        onlineUserList.clear();
        offlineUserList.clear();
        removeRejectedUser();
        removeDeviceUser();
        callMemberDirectly();
    }

    protected void removeRejectedUser(){
        for(int i=0;i<rejectUserList.size();i++){
            String from=(String)rejectUserList.get(i);
            for(int j=0;j<triedUserList.size();j++){
                JSONObject userObject=(JSONObject)triedUserList.get(j);
                String username=null;
                try {
                    username = (String) userObject.get("username");
                }catch(JSONException e){}
                if(from.equals(username)){
                    triedUserList.remove(userObject);
                    break;
                }
            }
        }
    }

    protected void removeDeviceUser(){
        ArrayList<JSONObject> removeList=new ArrayList<JSONObject>();
        for(int j=0;j<triedUserList.size();j++){
            JSONObject userObject=(JSONObject)triedUserList.get(j);
            String username=null;
            try {
                username = (String) userObject.get("username");
            }catch(JSONException e){}
            if(username.length()==17){
                removeList.add(userObject);
            }
        }
        for(int i=0;i<removeList.size();i++){
            triedUserList.remove(removeList.get(i));
        }
    }

    protected void callMemberDirectly(){
        if(callConnectState==CALL_DIRECT_CONNECTING){
            if(triedUserList.size()>0){
                JSONObject userObject=(JSONObject)triedUserList.remove(0);
                String mobile=null;
                try{
                    mobile=(String)userObject.get("username");
                }catch(JSONException e){
                }
                startCallDirect(mobile);
            }else{
                callMemberDirectlyFailed();
            }
        }
    }

    protected void callMemberDirectlyFailed(){
        sendDialMessenger(DialActivity.MSG_CALLMEMBER_DIRECT_TIMEOUT);
        resetCallMode();
    }
    /**
     * 重置呼叫状态，将所有设置恢复至初始状态
     */
    private void resetCallMode(){
        callConnectState=CALL_WAITING;
        allUserList.clear();
        triedUserList.clear();
        onlineUserList.clear();
        offlineUserList.clear();
        rejectUserList.clear();
    }

    private void startTimeoutChecking(){
        timeoutCheckThread=new Thread(){
            public void run(){
                try {
                    sleep(DeviceConfig.PARALL_WAIT_TIME); //等待指定的一个并行时间
                    if(!isInterrupted()){ //检查线程没有被停止
                        if(callConnectState==CALL_VIDEO_CONNECTING){ //如果现在是尝试连接状态
                            if(DeviceConfig.IS_CALL_DIRECT_AVAILABLE){ //如果支持直拨，立刻进入直拨电话模式
                                sendDialMessenger(DialActivity.MSG_CALLMEMBER_TIMEOUT_AND_TRY_DIRECT); //通知界面目前已经超时，并尝试直拨电话
                                startCallMemberDirectly();
                            }else{
                                resetCallMode();
                                sendDialMessenger(DialActivity.MSG_CALLMEMBER_TIMEOUT); //通知界面目前已经超时，并进入初始状态
                            }
                        }
                    }
                }catch(InterruptedException e){
                }
                timeoutCheckThread=null;
            }
        };
        timeoutCheckThread.start();
    }

    private void stopTimeoutCheckThread(){
        if(timeoutCheckThread!=null){
            timeoutCheckThread.interrupt();
            timeoutCheckThread=null;
        }
    }

    protected void sendCallMessageSerial(){
        if(callConnectState==CALL_VIDEO_CONNECTING){
            try{
                JSONObject data = new JSONObject();
                data.put("command","call");
                data.put("from",this.key);
                if(allUserList.size()>0){
                    JSONObject userObject=(JSONObject)allUserList.remove(0);
                    String username=(String)userObject.get("username");
                    String userUrl= RtcRules.UserToRemoteUri_new(username,RtcConst.UEType_Any);
                    int sendResult=device.sendIm(userUrl,"cmd/json",data.toString());
                    Log.v("MainService","sendIm(): "+sendResult);
                    triedUserList.add(userObject);
                }else{
                    //TODO
                }
            }catch(JSONException e){
            }
        }
    }

    protected void onCreateLog(Message msg){
    }

    /** The m a listener. */
    DeviceListener deviceListener = new DeviceListener() {
        @Override
        public void onDeviceStateChanged(int result) {
            Log.v("MainService","onDeviceStateChanged,result="+result);
            if(result == RtcConst.CallCode_Success) { //注销也存在此处
                //device连接成功
                onConnectSuccess();
                Log.i("zhlRTC----","CallCode_Success");
            }else if(result == RtcConst.NoNetwork) {
                //没有网络
                onNoNetWork();
                Log.i("zhlRTC----","NoNetwork");
            }else if(result == RtcConst.ChangeNetwork) {
                //改变网络
                changeNetWork();
                Log.i("zhlRTC----","ChangeNetwork");
            }else if(result == RtcConst.PoorNetwork) {
                //糟糕的网络   网络信号不好
                onPoorNetWork();
                Log.i("zhlRTC----","PoorNetwork");
            }else if(result == RtcConst.ReLoginNetwork) {

                Log.v("MainService","onDeviceStateChanged,ReLoginNetwork");
                onConnectError();
                Log.i("zhlRTC----","ReLoginNetwork");
            }else if(result == RtcConst.DeviceEvt_KickedOff) {
                // 被另外一个终端踢下线，由用户选择是否继续，如果再次登录，需要重新获取token，重建device
                Log.v("MainService","onDeviceStateChanged,DeviceEvt_KickedOff");
                onConnectError();
                Log.i("zhlRTC----","DeviceEvt_KickedOff");
            }else if(result == RtcConst.DeviceEvt_MultiLogin) {
                Log.v("MainService","onDeviceStateChanged,DeviceEvt_MultiLogin");
                Log.i("zhlRTC----","DeviceEvt_MultiLogin");
            }else {
                //  CommFunc.DisplayToast(MyApplication.this, "注册失败:"+result);
            }
        }
        private void onPoorNetWork() {
            Log.v("MainService","onPoorNetWork");
        }

        private void onConnectSuccess(){
            if(dialMessenger!=null){
                Message message = Message.obtain();
                message.what = DialActivity.MSG_CONNECT_SUCCESS;
                try {
                    dialMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        private void onConnectError(){
            if(dialMessenger!=null){
                Message message = Message.obtain();
                message.what = DialActivity.MSG_CONNECT_ERROR;
                try {
                    dialMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        private void onNoNetWork() {
            Log.v("MainService", "onNoNetWork");
            //断网销毁
            if (callConnection !=null) {
                callConnection.disconnect();
                callConnection = null;
                callingDisconnect();
            }
            onConnectError();
        }
        private void changeNetWork() {
            Log.v("MainService","changeNetWork");
            //自动重连接
        }
        @Override//新电话
        public void onNewCall(Connection call) {
            JSONObject callInfo=null;
            String acceptMember=null;
            try{
                callInfo=new JSONObject(call.info());
                acceptMember=callInfo.getString("uri");
            }catch(JSONException e){}
            Log.v("MainService","onNewCall,call="+call.info());
            if (callConnection !=null) {
                call.reject();
                call = null;
                Log.v("MainService","onNewCall,reject call");
                return;
            }
            incomingFlag = true;
            callConnection = call;
            call.setIncomingListener(connectionListener);
            call.accept(callType);
            cancelOtherMembers(acceptMember);
            resetCallMode();
            stopTimeoutCheckThread();
            Message message = Message.obtain();
            message.what = DialActivity.MSG_RTC_NEWCALL;
            try {
                dialMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onQueryStatus(int status, String paramers) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSendIm(int status) {
            // TODO Auto-generated method stub
            if(callConnectState==CALL_VIDEO_CONNECTING){
                //检查当前的呼叫模式是并行还是串行
                if(DeviceConfig.CALL_MEMBER_MODE==DeviceConfig.CALL_MEMBER_MODE_PARALL){
                    //检查并行呼叫状态
                    checkSendCallMessageParall(status);
                }else{
                    checkSendCallMessageSerial(status);
                }
            }
        }
        @Override
        public void onReceiveIm(String from,String mime,String content) {
            // TODO Auto-generated method stub
            onMessage(from,mime,content);
        }
    };

    private void checkSendCallMessageParall(int status){
        Object object=triedUserList.get(triedUserList.size()-1);
        if(status==RtcConst.CallCode_Success){
            onlineUserList.add(object);
        }else{
            offlineUserList.add(object);
        }
        sendCallMessageParall();
    }

    private void checkSendCallMessageSerial(int status){

    }

    //RTC 拨打
    private void cancelOtherMembers(String acceptMember){
        try{
            JSONObject command = new JSONObject();
            command.put("command","cancelCall");
            command.put("from",this.key);
            if(onlineUserList!=null&&onlineUserList.size()>0){
                for(int i=0;i<onlineUserList.size();i++){
                    JSONObject userObject=(JSONObject) onlineUserList.get(i);
                    String username=(String)userObject.get("username");
                    if(username.length()==17){
                        username=username.replaceAll(":","");
                    }
                    if(!username.equals(acceptMember)){
                        String userUrl= RtcRules.UserToRemoteUri_new(username,RtcConst.UEType_Any);
                        int sendResult=device.sendIm(userUrl,"cmd/json",command.toString());
                    }
                }
            }
        }catch(Exception e){}
    }

    ConnectionListener connectionListener = new ConnectionListener() {
        @Override//连接中
        public void onConnecting() {

        }
        @Override//连接完成
        public void onConnected() {
        }
        @Override//断开连接
        public void onDisconnected(int code) {
            Log.v("MainService","onDisconnected timerDur"+ callConnection.getCallDuration());
            callConnection = null;
            callingDisconnect();
        }
        @Override//视频
        public void onVideo() {
            Message message = Message.obtain();
            message.what = DialActivity.MSG_RTC_ONVIDEO;
            try {
                dialMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onNetStatus(int msg, String info) {
            // TODO Auto-generated method stub
            System.out.println(msg);
            System.out.println(info);
        }
    };


    //调用断开
    private void callingDisconnect(){
        Message message = Message.obtain();
        message.what = DialActivity.MSG_RTC_DISCONNECT;
        try {
            dialMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    //根据消息队列进行进行数据更新和开门
    protected void onMessage(String from,String mime,String content){
        if(content.equals("refresh card info")){
            sendDialMessenger(DialActivity.MSG_REFRESH_DATA,"card");
            retrieveChangedCardList();
        }else if(content.equals("refresh finger info")){
            sendDialMessenger(DialActivity.MSG_REFRESH_DATA,"finger");
            retrieveChangedFingerList();
        }else if(content.equals("refresh all info")){
            sendDialMessenger(DialActivity.MSG_REFRESH_DATA,"allinfo");
            resetFlag=1;   //20170920pm增加批量修改功能
            initDeviceData();
            //retrieveChangedFingerList();
            //retrieveChangedCardList();
        }else if(content.equals("reject call")){  //可视对讲挂断
            if(!rejectUserList.contains(from)){
                rejectUserList.add(from);
            }
            sendDialMessenger(DialActivity.MSG_CALLMEMBER_CANCEL);
        }else if(content.startsWith("open the door")){
            String imageUrl=null;
            int thisIndex=content.indexOf("-");
            if(thisIndex>0){
                imageUrl=content.substring(thisIndex+1);
            }else{
                imageUrl=null;
            }
            startCreateAccessLog(from,imageUrl);  //创建日志
            resetCallMode();  //释放可视对讲对象
            stopTimeoutCheckThread();  //释放IP电话对象
            openLock();  //开启门禁信号
        }else if(content.equals("refresh face info")){
            //人脸识别相关   接收信息
            sendDialMessenger(DialActivity.MSG_REFRESH_DATA,"faceInfo");
            Log.e("实时接收消息","接收消息");
            ThreadManager.getSlingeThread().execute(new Runnable() {
                @Override
                public void run() {
                    faceReceiveData();
                }
            });

        }
    }

    //人脸识别
    @SuppressLint("HandlerLeak")
    Handler facehandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.obj.equals("success")){
//                final int lockIds = msg.what;
                ThreadManager.getSlingeThread().execute(new Runnable() {
                    @Override
                    public void run() {
//                        String url = "http://uat.jzwimb.com/app/pcfid/changeFaceComplete";
                        String url = DeviceConfig.SERVER_URL+ "/app/pcfid/changeFaceComplete";
                        HttpClient client = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost(url);
                        BasicNameValuePair bnvp = new BasicNameValuePair("lockIds",String.valueOf(Constant.lockId));
                        BasicNameValuePair bnvp1 = new BasicNameValuePair("communityId",String.valueOf(Constant.communityId));
                        BasicNameValuePair bnvp2 = new BasicNameValuePair("fingerListSuccess",String.valueOf(19));
                        BasicNameValuePair bnvp3 = new BasicNameValuePair("fingerListFailed",String.valueOf(31));

                        List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
                        parameters.add(bnvp);
                        parameters.add(bnvp1);
                        parameters.add(bnvp2);
                        parameters.add(bnvp3);
                        try {
                            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "utf-8");
                            httpPost.setEntity(entity);
                            HttpResponse httpResponse = client.execute(httpPost);
                            if(httpResponse.getStatusLine().getStatusCode() == 200){
                                InputStream is = httpResponse.getEntity().getContent();//获取内容
                                final String result = StreamTools.streamToStr(is); // 通过工具类转换文本
                                Log.e("result",result);
                                JSONObject facejson = null;
                                try {
                                    facejson = new JSONObject(result);
                                    int code = facejson.getInt("code");
                                    if(code == 0){
                                        Log.e("实时提交成功","提交成功");
                                    }else{
                                        Log.e("实时提交失败","提交失败");
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }
    };
    //人脸识别相关
    public void faceReceiveData() {
        Log.e("实时faceReceiveData", "进来了faceReceiveData方法");
//        PullFaceReceiver pullFaceReceiver = new PullFaceReceiver();
//        pullFaceReceiver.httpGetFaceData();

//        String faceUrl = "http://uat.jzwimb.com/app/pcfid/retrieveChangedFaceList?lockIds=323";
        String faceUrl = DeviceConfig.SERVER_URL+ "/app/pcfid/retrieveChangedFaceList?lockIds="+Constant.lockId;
        //用httpClient 分为五个步骤
        //  1 创建httpclient
        HttpClient client = new DefaultHttpClient();
        //  2 创建代表请求的对象,参数是访问的服务器地址
        HttpGet httpGet = new HttpGet(faceUrl);
        try {
            //  3 执行请求，获取服务器发还的相应对象
            HttpResponse httpResponse = client.execute(httpGet);
            //  4 检查相应的状态是否正常：检查状态码的值是200表示正常
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //  5 从相应的对象当中取出数据，放到entity中
                HttpEntity entity = httpResponse.getEntity();
                String response = EntityUtils.toString(entity, "utf-8");
                Log.e("实时face数据", response.toString());
                JSONObject facejson = new JSONObject(response);
                int code = facejson.getInt("code");
                JSONArray data = facejson.getJSONArray("data");
                if (code == 0) {
                    for (int i = 0; i < data.length(); i++) {
                        Log.e("实时data的数量", data.length() + "");
                        JSONObject obj = data.getJSONObject(i);
                        int rid = obj.getInt("rid");
//                        int faceId = obj.getInt("faceId");
                        int userId = obj.getInt("userId");
                        Log.e("实时userId", userId + "");
                        int communityId = obj.getInt("communityId");
                        String state = obj.getString("state");
//                        int lockIds = obj.getInt("lockIds");
                        String lockIds = obj.getString("lockIds");
                        Log.e("实时lockIds",String.valueOf(lockIds));
                        String creDate = obj.getString("creDate");
                        int order = obj.getInt("order");
                        String faceData = obj.getString("faceData");
                        Log.e("实时faceData数据", faceData.length() + "");
                        byte[] facedata = Base64.decode(faceData, Base64.DEFAULT);
                        if (DialActivity.faceManager.addCharacter(rid, facedata)) {
                            Log.e("实时addFace", "添加人脸成功" + "rid: " + rid);
                            DialActivity.faceManager.stopScan();
                            DialActivity.faceManager.startScan();

//                            //将数据信息添加到RE_FACEDATA表中
//                            sqlUtil = new SqlUtil(context);
//                            FaceDataInfo faceDataInfo = new FaceDataInfo();
//                            faceDataInfo.setRid(rid);
//                            faceDataInfo.setUserId(userId);
//                            faceDataInfo.setCommunityId(communityId);
//                            faceDataInfo.setState(state);
//                            faceDataInfo.setLockIds(lockIds);
//                            faceDataInfo.setOrderId(order);
//                            faceDataInfo.setCreDate(creDate);
//                            faceDataInfo.setFaceData(faceData);
//                            sqlUtil.addFaceData(faceDataInfo);

                            //使用handler 发送消息   告诉服务器  加载成功
                            Message msg = Message.obtain();
//                            msg.what = lockIds;
                            msg.obj = "success";
                            facehandler.sendMessage(msg);
                        } else {
                            Log.e("addFace", "添加人脸失败");
                        }
                    }
                } else {
                    Log.e("faceReceiveData", "请求失败");
                }
            } else {
                Log.e("faceReceiveData", "网络异常...............");
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

    }


    //开启门禁信号
    protected void openLock(){
        if(DeviceConfig.IS_RFID_AVAILABLE){
            openLedLock();
        }else if(DeviceConfig.IS_ASSEMBLE_AVAILABLE){
            openAssembleLock();
        }
    }

    //开启LED锁
    private void openLedLock(){
        //MBaseActivity.ioctl(1, 1); // led1 on
        MBaseActivity.ioct2(1, 1); // led1 on
        //MBaseActivity.controlCamera(1,1);
        sendDialMessenger(DialActivity.MSG_LOCK_OPENED);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //MBaseActivity.ioctl(1, 2); // led1 off
        MBaseActivity.ioct2(1, 2); // led1 off
        //MBaseActivity.controlCamera(1,2);
    }

    //开启assemble锁
    private void openAssembleLock(){
        assembleUtil.openLock();
        sendDialMessenger(DialActivity.MSG_LOCK_OPENED);
    }

    //启用线程修建日志
    protected void startCreateAccessLog(String from,final String imageUrl){
        this.messageFrom=from;
        new Thread(){
            public void run(){
                createAccessLog(imageUrl);
            }
        }.start();
    }


    //创建日志
    protected void createAccessLog(String imageUrl){
        String url=DeviceConfig.SERVER_URL+"/app/device/createAccessLog?from=";
        url=url+this.messageFrom;
        url=url+"&communityId="+this.communityId;
        url=url+"&lockId="+this.lockId;
        if(imageUrl!=null){
            try {
                url = url + "&imageUrl=" + URLEncoder.encode(imageUrl, "UTF-8");
            }catch(Exception e){}
        }
        try{
            URL thisUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
            conn.setRequestMethod("GET");
            if(httpServerToken!=null) {
                conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
            }
            conn.setConnectTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                String result = HttpUtils.readMyInputStream(is);
                Message message = handler.obtainMessage();
                message.what = MSG_CREATELOG;
                message.obj = Ajax.getJSONObject(result);
                handler.sendMessage(message);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("MainService","onDestroy()");
        if (callConnection !=null) {
            callConnection.disconnect();
            callConnection = null;
            callingDisconnect();
        }
        if(device !=null) {
            device.release();
            device = null;
        }
        if(rtcClient !=null) {
            rtcClient.release();
            rtcClient = null;
        }
        if(sqlUtil!=null){
            sqlUtil.close();
        }
        if(rfidUtil!=null){
            rfidUtil.close();
        }
        if(assembleUtil!=null){
            assembleUtil.close();
        }
    }


    //启用线程检查机器在线时间
    protected void initConnectReport(){
        connectReportThread=new Thread(){
            public void run(){
                try {
                    connectReport();
                    while(!isInterrupted()) {
                        sleep(DeviceConfig.CONNECT_REPORT_WAIT_TIME); //等待广告刷新的时间
                        connectReport();
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        connectReportThread.start();
    }


    //检查机器在线时间
    protected void connectReport(){
        String url=DeviceConfig.SERVER_URL+"/app/device/connectReport?communityId="+this.communityId;
        url=url+"&lockId="+this.lockId;
        try{
            URL thisUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
            conn.setRequestMethod("GET");
            if(httpServerToken!=null) {
                conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
            }
            conn.setConnectTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                String result = HttpUtils.readMyInputStream(is);
                JSONObject resultObject = Ajax.getJSONObject(result);
                int resultCode=resultObject.getInt("code");
                if(resultCode==0){
                }else{
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //从服务器中获取指纹和卡片信息
    protected void initDeviceData(){
        if(resetFlag>0){
            sqlUtil.clearDeviceData();
            String url=DeviceConfig.SERVER_URL+"/app/device/retrieveDeviceData?communityId="+this.communityId;
            url=url+"&blockId="+this.blockId;
            url=url+"&lockId="+this.lockId;
            try{
                URL thisUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
                conn.setRequestMethod("GET");
                if(httpServerToken!=null) {
                    conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
                }
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                if (code == 200) {
                    InputStream is = conn.getInputStream();
                    String result = HttpUtils.readMyInputStream(is);
                    JSONObject resultObject = Ajax.getJSONObject(result);
                    int resultCode=resultObject.getInt("code");
                    if(resultCode==0) {
                        JSONArray fingerList = resultObject.getJSONArray("fingerList");
                        sqlUtil.changeFinger(fingerList);
                        if (DeviceConfig.IS_ASSEMBLE_AVAILABLE && !DeviceConfig.IS_FINGER_AVAILABLE) {
                            assembleUtil.changeFinger(fingerList);
                        }
                        JSONArray cardList = resultObject.getJSONArray("cardList");
                        sqlUtil.changeCard(cardList);
                        if (DeviceConfig.IS_ASSEMBLE_AVAILABLE) {
                            assembleUtil.changeCard(cardList);
                        }
                        completeInitDeviceData();
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        resetFlag=0;
    }


    //设置不重新刷新数据
    protected void completeInitDeviceData(){
        String url=DeviceConfig.SERVER_URL+"/app/device/completeInitDeviceData?communityId="+this.communityId;
        url=url+"&blockId="+this.blockId;
        url=url+"&lockId="+this.lockId;
        try{
            URL thisUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
            conn.setRequestMethod("GET");
            if(httpServerToken!=null) {
                conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
            }
            conn.setConnectTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                String result = HttpUtils.readMyInputStream(is);
                JSONObject resultObject = Ajax.getJSONObject(result);
                int resultCode=resultObject.getInt("code");
                if(resultCode==0){
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //启用线程初始化设备数据
    protected void initAdvertisement(){
        advertisementThread=new Thread(){
            public void run(){
                try {
                    //retrieveCardList();
                    if(resetFlag>0){
                        initDeviceData();
                    }else{
                        retrieveChangedFingerList();
                        retrieveChangedCardList();
                    }
                    sleep(DeviceConfig.AD_INIT_WAIT_TIME);
                    while(!isInterrupted()) {
                        getLastAdvertisementList();
                        sleep(DeviceConfig.AD_REFRESH_WAIT_TIME); //等待广告刷新的时间
                        //retrieveCardList();
                        retrieveChangedFingerList();
                        retrieveChangedCardList();
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        advertisementThread.start();
    }


    //获取指纹数据
    protected void retrieveChangedFingerList(){
        String url=DeviceConfig.SERVER_URL+"/app/device/retrieveChangedFingerList?communityId="+this.communityId;
        url=url+"&blockId="+this.blockId;
        url=url+"&lockId="+this.lockId;
        try{
            URL thisUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
            conn.setRequestMethod("GET");
            if(httpServerToken!=null) {
                conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
            }
            conn.setConnectTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                String result = HttpUtils.readMyInputStream(is);
                JSONObject resultObject = Ajax.getJSONObject(result);
                int resultCode=resultObject.getInt("code");
                if(resultCode==0){
                    JSONArray data=resultObject.getJSONArray("data");
                    sqlUtil.changeFinger(data);
                    if(DeviceConfig.IS_ASSEMBLE_AVAILABLE && !DeviceConfig.IS_FINGER_AVAILABLE) {
                        assembleUtil.changeFinger(data);
                    }else{
                        JSONArray list=new JSONArray();
                        for(int i=0;i<data.length();i++){
                            JSONObject item=data.getJSONObject(i);
                            list.put(item.getInt("lockIndex"));
                        }
                        startChangeFingerComplete(list,new JSONArray());
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //获取卡片数据
    protected void retrieveChangedCardList(){
        String url=DeviceConfig.SERVER_URL+"/app/device/retrieveChangedCardList?communityId="+this.communityId;
        url=url+"&blockId="+this.blockId;
        url=url+"&lockId="+this.lockId;
        try{
            URL thisUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
            conn.setRequestMethod("GET");
            if(httpServerToken!=null) {
                conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
            }
            conn.setConnectTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                String result = HttpUtils.readMyInputStream(is);
                JSONObject resultObject = Ajax.getJSONObject(result);
                int resultCode=resultObject.getInt("code");
                if(resultCode==0){
                    JSONArray data=resultObject.getJSONArray("data");
                    sqlUtil.changeCard(data);
                    if(DeviceConfig.IS_ASSEMBLE_AVAILABLE) {
                        assembleUtil.changeCard(data);
                    }else{
                        JSONArray list=new JSONArray();
                        for(int i=0;i<data.length();i++){
                            JSONObject item=data.getJSONObject(i);
                            list.put(item.getInt("lockIndex"));
                        }
                        startChangeCardComplete(list,new JSONArray());
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void startCardOpenLock(final int index){
        Thread thread=new Thread(){
            public void run() {
                try {
                    onCardOpenLock(index);
                }catch(IOException e){
                }catch(JSONException e){
                }
            }
        };
        thread.start();
    }


    //记录卡片开门记录
    protected void onCardOpenLock(int index) throws JSONException,IOException{
        JSONObject data=new JSONObject();
        data.put("lockId",lockId);
        data.put("communityId",communityId);
        data.put("lockIndex",index);
        String dataStr=data.toString();
        URL url = new URL(DeviceConfig.SERVER_URL+"/app/device/cardOpenLock");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");
        if(httpServerToken!=null) {
            connection.setRequestProperty("Authorization", "Bearer " + httpServerToken);
        }
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect();
        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream(), "UTF-8");
        out.append(dataStr);
        out.flush();
        out.close();
        InputStream is = connection.getInputStream();
        String result = HttpUtils.readMyInputStream(is);
        JSONObject resultObj=Ajax.getJSONObject(result);
        int code=resultObj.getInt("code");
        if(code==0){
        }else{
        }
    }

    //指纹开门
    protected void onFingerDetect(byte[] data){
        int fingerNum=sqlUtil.getFingerNum();
        //Log.v("M:-----","------"+fingerNum);
        if(fingerNum>0) {
            startFingerDetect(fingerNum, data);
        }else{
            sendDialMessenger(DialActivity.MSG_FINGER_CHECK,false);
        }
    }

    //获取线程数
    private int getFingerDetectThreadNum(int fingerNum){
        int threadNum=1;
        if(fingerNum>40000){
            threadNum=30;
        }else if(fingerNum>10000){
            threadNum=20;
        }else if(fingerNum>5000){
            threadNum=15;
        }else if(fingerNum>1000){
            threadNum=5;
        }
        return threadNum;
    }


    //指纹检查状态
    public synchronized boolean isFingerChecking(){
        return fingerDetectStatus==1;
    }

    //设备指纹状态
    public synchronized void setFingerCheckStatus(int value){
        fingerDetectStatus=value;
    }

    //指纹检查累加器
    private synchronized void increaseDetectSteps(){
        fingerDetectSteps++;
    }

    //多线程检查指纹开锁
    protected void onFingerDetectThreadComplete(FingerData fingerData){
        if(fingerData!=null){
            setFingerCheckStatus(0);
            setFingerCheckResult(true);
            openLock();
            startFingerUserOpenLock(fingerData);//开门指纹记录
        }
        increaseDetectSteps();
        if(fingerDetectSteps==fingerDetectThreads.length){
            setFingerCheckStatus(0);
            fingerDetectSteps=0;
            if(fingerDetectThreads!=null){
                for(int i=0;i<fingerDetectThreads.length;i++){
                    try {
                        fingerDetectThreads[i].interrupt();
                    }catch(Exception e){
                    }
                    fingerDetectThreads[i]=null;
                }
                fingerDetectThreads=null;
                sendDialMessenger(DialActivity.MSG_FINGER_CHECK,fingerDetectResult);
            }
        }
    }


    //设置指纹检查返回值
    protected synchronized void setFingerCheckResult(boolean result){
        fingerDetectResult=result;
    }

    //开始指纹检查
    protected void startFingerDetect(int fingerNum,final byte[] data){
        if(fingerDetectThreads==null){
            setFingerCheckStatus(1);
            setFingerCheckResult(false);
            fingerDetectSteps=0;
            int threadNum=getFingerDetectThreadNum(fingerNum);
            int stepNum=fingerNum/threadNum;
            if(stepNum*threadNum<fingerNum){
                threadNum++;
            }
            fingerDetectThreads=new Thread[threadNum];
            for(int i=0;i<threadNum;i++){
                final int limit=stepNum;
                final int offset=i*stepNum;
                final IFingerCheck iFingerCheck=this;
                fingerDetectThreads[i]=new Thread(){
                    public void run() {
                        FingerData fingerData = null;
                        try {
                            fingerData = sqlUtil.checkFinger(data, iFingerCheck, limit, offset);
                        }catch (Exception e){}
                        sendMessenger(MSG_FINGER_DETECT_THREAD_COMPLETE,fingerData);
                   }
                };
                fingerDetectThreads[i].start();
            }
        }
    }


    //指纹搜索
    protected boolean isSameFinger(byte[] from,byte[] to){
        int[]  matchResult = new int[1];
        boolean result=false;
        if(adLoad.FPMatch(from,to,SZ_SECURITY_LEVEL,matchResult,null)==0){
            if(matchResult[0] == 1){
                result=true;
            }
        }
        return result;
    }


    //开始指纹开锁
    protected void startFingerOpenLock(final int index){
        Thread thread=new Thread(){
            public void run() {
                try {
                    onFingerOpenLock(index);
                }catch(IOException e){
                }catch(JSONException e){
                }
            }
        };
        thread.start();
    }


    //指纹开锁记录
    protected void onFingerOpenLock(int index) throws JSONException,IOException{
        JSONObject data=new JSONObject();
        data.put("lockId",lockId);
        data.put("communityId",communityId);
        data.put("lockIndex",index);
        String dataStr=data.toString();
        URL url = new URL(DeviceConfig.SERVER_URL+"/app/device/fingerOpenLock");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");
        if(httpServerToken!=null) {
            connection.setRequestProperty("Authorization", "Bearer " + httpServerToken);
        }
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect();
        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream(), "UTF-8");
        out.append(dataStr);
        out.flush();
        out.close();
        InputStream is = connection.getInputStream();
        String result = HttpUtils.readMyInputStream(is);
        JSONObject resultObj=Ajax.getJSONObject(result);
        int code=resultObj.getInt("code");
        if(code==0){
        }else{
        }
    }

    //用户指纹开锁
    protected void startFingerUserOpenLock(final FingerData fingerData){
        Thread thread=new Thread(){
            public void run() {
                try {
                    onFingerUserOpenLock(fingerData);
                }catch(IOException e){
                }catch(JSONException e){
                }
            }
        };
        thread.start();
    }


    //用户指纹开门记录  开门指纹记录
    protected void onFingerUserOpenLock(FingerData fingerData) throws JSONException,IOException{
        JSONObject data=new JSONObject();
        data.put("lockId",lockId);
        data.put("communityId",communityId);
        data.put("userId",fingerData.userId);
        data.put("employeeId",fingerData.employeeId);
        String dataStr=data.toString();
        URL url = new URL(DeviceConfig.SERVER_URL+"/app/device/fingerUserOpenLock");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");
        if(httpServerToken!=null) {
            connection.setRequestProperty("Authorization", "Bearer " + httpServerToken);
        }
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect();
        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream(), "UTF-8");
        out.append(dataStr);
        out.flush();
        out.close();
        InputStream is = connection.getInputStream();
        String result = HttpUtils.readMyInputStream(is);
        JSONObject resultObj=Ajax.getJSONObject(result);
        int code=resultObj.getInt("code");
        if(code==0){
        }else{
        }
    }

//    //用户人脸开门记录    目前   lockId   communityId  userId
//    protected void onFaceUserOpenLock(FaceData faceData) throws JSONException,IOException{
//        JSONObject data=new JSONObject();
//        data.put("lockId",lockId);
//        data.put("communityId",communityId);
//        data.put("userId",faceData.userId);
////        data.put("employeeId",fingerData.employeeId);
//        String dataStr=data.toString();
//        URL url = new URL(DeviceConfig.SERVER_URL+"/app/device/faceUserOpenLock");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setDoOutput(true);
//        connection.setDoInput(true);
//        connection.setUseCaches(false);
//        connection.setInstanceFollowRedirects(true);
//        connection.setRequestMethod("POST");
//        if(httpServerToken!=null) {
//            connection.setRequestProperty("Authorization", "Bearer " + httpServerToken);
//        }
//        connection.setRequestProperty("Accept", "application/json");
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.connect();
//        OutputStreamWriter out = new OutputStreamWriter(
//                connection.getOutputStream(), "UTF-8");
//        out.append(dataStr);
//        out.flush();
//        out.close();
//        InputStream is = connection.getInputStream();
//        String result = HttpUtils.readMyInputStream(is);
//        JSONObject resultObj=Ajax.getJSONObject(result);
//        int code=resultObj.getInt("code");
//        if(code==0){
//        }else{
//        }
//    }

    //更新指纹信息
    protected void startChangeFingerComplete(final JSONArray fingerListSuccess,final JSONArray fingerListFailed){
        Thread thread=new Thread(){
            public void run() {
                try {
                    onChangeFingerComplete(fingerListSuccess, fingerListFailed);
                }catch(IOException e){
                }catch(JSONException e){
                }
            }
        };
        thread.start();
    }



    //更新卡片信息
    protected void startChangeCardComplete(final JSONArray cardListSuccess,final JSONArray cardListFailed){
        Thread thread=new Thread(){
            public void run() {
                try {
                    onChangeCardComplete(cardListSuccess, cardListFailed);
                }catch(IOException e){
                }catch(JSONException e){
                }
            }
        };
        thread.start();
    }


    //从云平台同步指纹到门禁机
    protected void onChangeFingerComplete(JSONArray fingerListSuccess,JSONArray fingerListFailed) throws JSONException,IOException{
        JSONObject data=new JSONObject();
        data.put("lockId",lockId);
        data.put("communityId",communityId);
        data.put("fingerListSuccess",fingerListSuccess);
        data.put("fingerListFailed",fingerListFailed);
        String dataStr=data.toString();
        URL url = new URL(DeviceConfig.SERVER_URL+"/app/device/changeFingerComplete");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");
        if(httpServerToken!=null) {
            connection.setRequestProperty("Authorization", "Bearer " + httpServerToken);
        }
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect();
        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream(), "UTF-8");
        out.append(dataStr);
        out.flush();
        out.close();
        InputStream is = connection.getInputStream();
        String result = HttpUtils.readMyInputStream(is);
        JSONObject resultObj=Ajax.getJSONObject(result);
        int code=resultObj.getInt("code");
        if(code==0){
        }else{
        }
    }

    //从云平台同步卡片信息到门禁机
    protected void onChangeCardComplete(JSONArray cardListSuccess,JSONArray cardListFailed) throws JSONException,IOException{
        JSONObject data=new JSONObject();
        data.put("lockId",lockId);
        data.put("communityId",communityId);
        data.put("cardListSuccess",cardListSuccess);
        data.put("cardListFailed",cardListFailed);
        String dataStr=data.toString();
        URL url = new URL(DeviceConfig.SERVER_URL+"/app/device/changeCardComplete");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("POST");
        if(httpServerToken!=null) {
            connection.setRequestProperty("Authorization", "Bearer " + httpServerToken);
        }
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect();
        OutputStreamWriter out = new OutputStreamWriter(
                connection.getOutputStream(), "UTF-8");
        out.append(dataStr);
        out.flush();
        out.close();
        InputStream is = connection.getInputStream();
        String result = HttpUtils.readMyInputStream(is);
        JSONObject resultObj=Ajax.getJSONObject(result);
        int code=resultObj.getInt("code");
        if(code==0){
        }else{
        }
    }

    //
    protected void getLastAdvertisementList(){
        if(advertisementStatus==ADVERTISEMENT_WAITING){
            advertisementStatus=ADVERTISEMENT_REFRESHING;
            currentAdvertisementFiles.clear();
            JSONArray rows=checkAdvertiseList();
            if(rows!=null){
                adjustAdvertiseFiles();
                restartAdvertise(rows);
                removeAdvertiseFiles();
            }
            advertisementStatus=ADVERTISEMENT_WAITING;
        }
    }

    //广告
    protected JSONArray checkAdvertiseList(){
        String url=DeviceConfig.SERVER_URL+"/app/advertisement/checkAdvertiseList?communityId="+this.communityId;
        url=url+"&lockId="+this.lockId;
        JSONArray rows=null;
        try{
            URL thisUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
            conn.setRequestMethod("GET");
            if(httpServerToken!=null) {
                conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
            }
            conn.setConnectTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                String result = HttpUtils.readMyInputStream(is);
                JSONObject obj = Ajax.getJSONObject(result);
                int resultCode=obj.getInt("code");
                if(resultCode==0){
                    try {
                        rows = obj.getJSONArray("data");
                        if(rows!=null){
                            downloadAdvertisement(rows);
                        }
                    }catch(Exception e){
                        rows=null;
                    }
                    try {
                        String communityName=obj.getString("communityName");
                        String lockName=obj.getString("lockName");
                        sendDialMessenger(DialActivity.MSG_REFRESH_COMMUNITYNAME,communityName);
                        sendDialMessenger(DialActivity.MSG_REFRESH_LOCKNAME,lockName);
                    }catch(Exception e){
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return rows;
    }

    protected void downloadAdvertisement(JSONArray rows)throws Exception{
        for(int i=0;i<rows.length();i++){
            try {
                JSONObject row = rows.getJSONObject(i);
                downloadAdvertisementItem(row);
            }catch(JSONException e){}
        }
    }

    protected void downloadAdvertisementItem(JSONObject row)throws Exception{
        try {
            JSONArray items=row.getJSONArray("items");
            for(int i=0;i<items.length();i++){
                JSONObject item=items.getJSONObject(i);
                downloadAdvertisementItemFiles(item);
            }
        }catch(JSONException e){}
    }

    protected void downloadAdvertisementItemFiles(JSONObject item) throws Exception{
        try {
            String fileUrls=item.getString("fileUrls");
            JSONObject urls = new JSONObject(fileUrls);
            if(item.getString("adType").equals("V")){
                String videoFile=urls.getString("video");
                downloadAdvertisementFile(videoFile);
            }else if(item.getString("adType").equals("I")){
                String voiceFile=urls.getString("voice");
                downloadAdvertisementFile(voiceFile);
                JSONArray imageFilesObject=urls.getJSONArray("images");
                for(int i=0;i<imageFilesObject.length();i++){
                    JSONObject imageObject=imageFilesObject.getJSONObject(i);
                    String imageFile=imageObject.getString("image");
                    downloadAdvertisementFile(imageFile);
                }
            }

        }catch(JSONException e){}
    }

    protected void downloadAdvertisementFile(String file) throws Exception{
        int lastIndex=file.lastIndexOf("/");
        String fileName=file.substring(lastIndex+1);
        String localFile=HttpUtils.getLocalFile(fileName);
        if(localFile==null){
            localFile=HttpUtils.downloadFile(file);
            if(localFile!=null){
                if(localFile.endsWith(".temp")){
                    localFile=localFile.substring(0,localFile.length()-5);
                }
                currentAdvertisementFiles.put(fileName,localFile);
            }
        }else{
            currentAdvertisementFiles.put(fileName,localFile);
        }
    }

    protected void restartAdvertise(JSONArray rows){
        if(!isAdvertisementListSame(rows)){
            sendDialMessenger(DialActivity.MSG_ADVERTISE_REFRESH,rows);
        }
        currentAdvertisementList=rows;
    }


    protected boolean isAdvertisementListSame(JSONArray rows){
        boolean result=true;
        String thisValue=currentAdvertisementList.toString();
        String thisRow=rows.toString();
        return thisRow.equals(thisValue);
    }


    //
    protected void adjustAdvertiseFiles(){
        String SDCard= Environment.getExternalStorageDirectory()+"";
        String localFilePath=SDCard+"/"+ DeviceConfig.LOCAL_FILE_PATH+"/";//文件存储路径
        Enumeration<String> keys=currentAdvertisementFiles.keys();
        while(keys.hasMoreElements()){
            String fileName=keys.nextElement();
            String filePath=currentAdvertisementFiles.get(fileName);
            File file=new File(filePath+".temp");
            if(file.exists()){
                file.renameTo(new File(filePath));
            }
        }
    }

    protected void removeAdvertiseFiles(){
        File[] files=HttpUtils.getAllLocalFiles();
        for(int i=0;i<files.length;i++) {
            File file=files[i];
            String fileName=file.getAbsolutePath();
            if(fileName.endsWith(".temp")){
                file.delete();
            }else if(!currentAdvertisementFiles.containsValue(fileName)){
                file.delete();
            }
        }
    }

    @Override
    public boolean checkFinger(byte[] thisFinger, byte[] fingerTemplate) {
        return isSameFinger(thisFinger,fingerTemplate);
    }

    /***********************Update Service(更新版本信息)********************/
    public void initUpdateHandler() {
        Log.v("UpdateService","------>init Update Handler<-------");
        loadVersionFromLocal();  //更新版本信息
        adjustVersionStatus();   //下载保存版本
        startCheckThread();      //版本信息
        startUpdateThread();     //更新版本信息
    }

    protected void adjustVersionStatus(){
        if(lastVersionStatus.equals("P")){
            String SDCard= Environment.getExternalStorageDirectory()+"";
            String fileName=SDCard+"/"+lastVersionFile;
            File file=new File(fileName);
            if(!file.exists()){
                lastVersionFile="";
                lastVersionStatus="L";
                saveVersionFromLocal();
            }
        }
    }

    protected void startCheckThread(){
        Log.v("UpdateService","start Check Thread");
        checkThread=new Thread(){
            public void run(){
                try{
                    while(!isInterrupted()) {
                        checkNewVersion();
                        sleep(DeviceConfig.RELEASE_VERSION_WAIT_TIME); //等待新版本检查的时间
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        checkThread.start();
    }

    protected void checkNewVersion(){
        Log.v("UpdateService","check New Version");
        String url= DeviceConfig.UPDATE_SERVER_URL+DeviceConfig.UPDATE_RELEASE_FOLDER+DeviceConfig.UPDATE_RELEASE_PACKAGE;
        try{
            URL thisUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)thisUrl.openConnection();
            conn.setRequestMethod("GET");
            if(httpServerToken!=null) {
                conn.setRequestProperty("Authorization", "Bearer " + httpServerToken);
            }
            conn.setConnectTimeout(5000);
            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                String result = HttpUtils.readMyInputStream(is);
                Log.v("UpdateService","result="+result);
                JSONObject resultObj= Ajax.getJSONObject(result);
                int lastVersion=resultObj.getInt("version");
                if(lastVersion>DeviceConfig.RELEASE_VERSION){ //检查当前版本是否和服务器最新版本一致，如果不是最新版本则发出更新消息
                    String packageName=resultObj.getString("name")+"."+lastVersion+".apk";
                    Message message = handler.obtainMessage();
                    message.what = MSG_FIND_NEW_VERSION;
                    message.obj =packageName;
                    handler.sendMessage(message);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void onNewVersion(String newFile){
        Log.v("UpdateService","on new version"+newFile);
        try {
            String[] fileValues = newFile.split("\\.");
            String versionName=fileValues[1];
            int version=new Integer(versionName);
            Log.v("UpdateService","lastVersionStatus="+lastVersionStatus+",this.lastVersion="+lastVersion);
            if(version==this.lastVersion){
                if(lastVersionStatus.equals("L")){
                    lastVersionStatus="N";
                    lastVersion=version;
                    lastVersionFile=newFile;
                    saveVersionFromLocal();
                    Log.v("UpdateService","from L to N");
                    startDownloadThread();
                }else if(lastVersionStatus.equals("N")){
                    startDownloadThread();
                }
            }else if(version>this.lastVersion){
                if(lastVersionStatus.equals("D")){
                    stopDownloadThread();
                }
                lastVersionStatus="N";
                lastVersion=version;
                lastVersionFile=newFile;
                saveVersionFromLocal();
                startDownloadThread();
            }
        }catch(Exception e){
        }
    }

    protected void startDownloadThread(){
        final String url=DeviceConfig.UPDATE_SERVER_URL+DeviceConfig.UPDATE_RELEASE_FOLDER+lastVersionFile;
        final String fileName=lastVersionFile;
        Log.v("UpdateService","start download thread->"+url+"-->"+fileName);
        if(lastVersionStatus.equals("N")){
            lastVersionStatus="D";
            Log.v("UpdateService","change version status to D");
            downloadThread=new Thread(){
                public void run(){
                    try{
                        while(!isInterrupted()) {
                            if(getDownloadingFlag()==0){
                                break;
                            }else{
                                sleep(1000); //等待上一次下载线程结束，并关闭文件
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                    if(getDownloadingFlag()==0){
                        Log.v("UpdateService","download file begin");
                        String lastFile=downloadFile(url,fileName);
                        if(lastFile!=null){
                            if(lastVersionStatus.equals("D")){
                                Log.v("UpdateService","change status to P");
                                lastVersionStatus="P";
                                saveVersionFromLocal();
                            }
                        }
                    }
                }
            };
            downloadThread.start();
        }
    }

    protected void stopDownloadThread(){
        Log.v("UpdateService","stop download thread");
        lastVersionStatus="N";
        saveVersionFromLocal();
        setDownloadingFlag(2);
        if(downloadThread!=null){
            downloadThread.interrupt();
        }
    }

    public String downloadFile(String url,String fileName){
        OutputStream output=null;
        String localFile=null;
        String result=null;
        setDownloadingFlag(1);
        try {
            URL urlObject=new URL(url);
            HttpURLConnection conn=(HttpURLConnection)urlObject.openConnection();
            String SDCard= Environment.getExternalStorageDirectory()+"";
            localFile=SDCard+"/"+fileName;//文件存储路径
            File file=new File(localFile);
            InputStream input=conn.getInputStream();
            if(!file.exists()){
                file.createNewFile();//新建文件
            }
            output=new FileOutputStream(file);
            //读取大文件
            byte[] buffer = new byte[1024 * 8];
            BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
            BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
            int count = 0, n = 0;
            try {
                while ((n = in.read(buffer, 0, 1024 * 8)) != -1&&getDownloadingFlag()==1) {
                    out.write(buffer, 0, n);
                    count += n;
                }
                out.flush();
                if(getDownloadingFlag()==1){
                    result=localFile;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if(output!=null){
                    output.close();
                }
            } catch (Exception e) {
            }
        }
        setDownloadingFlag(0);
        Log.v("UpdateService","download file end="+result);
        return result;
    }

    protected void startUpdateThread(){
        updateThread=new Thread(){
            public void run(){
                try{
                    while(!isInterrupted()) {
                        Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        if(DeviceConfig.APPLICATION_MODEL==0) {
                            if (lastVersionStatus.equals("P")) {
                                lastVersionStatus = "I";
                                updateApp();
                            } else {
                                sleep(1000 * 60 * 3);
                            }
                        }else{
                            if(hour==DeviceConfig.RELEASE_VERSION_UPDATE_TIME){
                                if (lastVersionStatus.equals("P")) {
                                    lastVersionStatus = "I";
                                    updateApp();
                                }
                            } else {
                                sleep(1000 * 60 * 30);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        updateThread.start();
    }

    protected void stopUpdateThread(){
        if(updateThread!=null){
            updateThread.interrupt();
            updateThread=null;
        }
    }

    protected void updateApp(){
        String SDCard= Environment.getExternalStorageDirectory()+"";
        String fileName=SDCard+"/"+lastVersionFile;
        File file=new File(fileName);
        Log.v("UpdateService","------>start Update App<------");
        if(file.exists()){
            Log.v("UpdateService","check update file OK");
            startInstallApp(fileName);
        }
    }

    protected void startInstallApp(String fileName){
        Intent app = this.getPackageManager().getLaunchIntentForPackage(DeviceConfig.TARGET_PACKAGE_NAME);
        if(app!=null){
            app.putExtra("installFileName",fileName);
            this.startActivity(app);
        }
    }

    protected void loadVersionFromLocal(){
        SharedPreferences sharedPreferences= getSharedPreferences("residential", Activity.MODE_PRIVATE);
        lastVersion=sharedPreferences.getInt("lastVersion",0);
        lastVersionFile=sharedPreferences.getString("lastVersionFile","");
        lastVersionStatus=sharedPreferences.getString("lastVersionStatus","L");
    }

    protected void saveVersionFromLocal(){
        SharedPreferences sharedPreferences= getSharedPreferences("residential",Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastVersion", lastVersion);
        editor.putString("lastVersionFile",lastVersionFile);
        editor.putString("lastVersionStatus",lastVersionStatus);
        editor.commit();
    }

    protected synchronized void setDownloadingFlag(int flag){
        downloadingFlag=flag;
    }

    protected synchronized int getDownloadingFlag(){
        return downloadingFlag;
    }
}

class CardRecord{
    public String card=null;
    public Date creDate=null;

    public CardRecord(){
        this.card="";
        this.creDate=new Date();
    }

    public boolean checkLastCard(String card){
        boolean result=false;
        if(this.card.equals(card)){
            long offset=new Date().getTime()-this.creDate.getTime();
            if(offset>1000){
                this.card=card;
                this.creDate=new Date();
            }else{
                result=true;
            }
        }else{
            this.card=card;
            this.creDate=new Date();
        }
        return result;
    }
}
