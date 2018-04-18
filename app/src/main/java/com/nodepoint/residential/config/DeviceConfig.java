package com.nodepoint.residential.config;

/**
 * Created by simon on 2016/7/23.
 * 1.解决初始化设备，同步卡片和指纹数据--版本16.3   20170919am  zhl
 * 2.解决指纹内部死循环问题--版本16.4   20170922pm  zhl
 */
public class DeviceConfig {
    /********
     * 金指纹
     *****/

    //服务器地址
    //public static final String SERVER_URL="http://192.168.0.101:3000";
    //public static final String SERVER_URL="http://192.168.0.88:3000";
    //public static final String SERVER_URL="http://120.76.207.214";
    //public static final String SERVER_URL="http://192.168.0.102:3000";
    //public static final String SERVER_URL="http://39.108.154.137";
    //public static final String SERVER_URL="http://192.168.0.102:3000";
    //public static final String SERVER_URL = "http://jzwsit.nodepointech.com";
        public static final String SERVER_URL="http://uat.jzwimb.com";
    //public static final String SERVER_URL="http://residential.nodepointech.com";


    //更新应用地址
    //public static final String UPDATE_SERVER_URL="http://192.168.0.102:3000";
    //public static final String UPDATE_SERVER_URL="http://39.108.154.137";
    //public static final String UPDATE_SERVER_URL = "http://jzwsit.nodepointech.com";
    public static final String UPDATE_SERVER_URL="http://uat.jzwimb.com";
    //public static final String UPDATE_SERVER_URL="http://192.168.0.88:3000";
    //public static final String UPDATE_SERVER_URL="http://120.76.207.214";
    //public static final String UPDATE_SERVER_URL="http://192.168.0.101:3000";
    //public static final String UPDATE_SERVER_URL="http://192.168.0.102:3000";
    public static final String UPDATE_RELEASE_FOLDER = "/release/doorAccessMachine/v1/";
    public static final String UPDATE_RELEASE_PACKAGE = "package.json";


    //版本信息
    //public static final String APPVERSION="JZW16.4";    //版本(正式)
    public static final String APPVERSION="JZW-4G16.6";    //版本(正式4G)
    //public static final String APPVERSION = "SIT16.4";    //版本(测试)
    //public static final String APPVERSION="Loca14.1";    //版本(本地)


    //更新版本序列号
    public static final int RELEASE_VERSION = 164;    //版本(测试)


    //键盘输入串口接口
    public static final String ttysProt = "/dev/ttyS0";  //串口0安卓板（非4G）
//    public static final String ttysProt = "/dev/ttyS4";  //串口0安卓板（4G模块）


    public static int RELEASE_VERSION_WAIT_TIME = 1000 * 60 * 60;
    public static int RELEASE_VERSION_UPDATE_TIME = 3;
    public static final String TARGET_PACKAGE_NAME = "com.nodepoint.intermanager";
    public static final String TARGET_ACTIVITY_NAME = "com.nodepoint.intermanager.InitActivity";

    public static final int APPLICATION_MODEL = 0; //0:debug  1:product
    public static final int CALL_MEMBER_MODE_SERIAL = 0;
    public static final int CALL_MEMBER_MODE_PARALL = 1;
    public static final String LOCAL_FILE_PATH = "residential";
    public static final int HIDE_SCREEN_STATUS = 1;
    public static final boolean IS_SUPPORT_OFFLINE = true;

    public static boolean IS_PUSH_AVAILABLE = true;
    public static boolean IS_CALL_DIRECT_AVAILABLE = false; //  true呼叫电话  false取消呼叫电话
    public static int CALL_MEMBER_MODE = CALL_MEMBER_MODE_PARALL;

    public static boolean IS_RFID_AVAILABLE = false;
    public static boolean IS_ASSEMBLE_AVAILABLE = true;
    public static boolean IS_FINGER_AVAILABLE = true;

    public static int PARALL_WAIT_TIME = 1000 * 30;
    public static int SERIAL_WAIT_TIME = 1000 * 30;

    public static int AD_INIT_WAIT_TIME = 1000 * 60;
    public static int AD_REFRESH_WAIT_TIME = 1000 * 60 * 60;//1000*60*60;
    public static int CONNECT_REPORT_WAIT_TIME = 1000 * 60 * 10;
    public static int MAX_DIRECT_CALL_TIME = 1000 * 60 * 1;//通话时长应该有个最大值
    public static int PASSWORD_WAIT_TIME = 1000 * 20;

    public static int UNIT_NO_LENGTH = 4;
    public static int BLOCK_NO_LENGTH = 2;
    public static String DEVICE_TYPE = "B"; //C：社区大门门禁 B:楼栋单元门禁

    public static int VOLUME_STREAM_MUSIC = 5;
    public static int VOLUME_STREAM_VOICE_CALL = 5;
    public static int VOLUME_STREAM_RING = 5;
    public static int VOLUME_STREAM_SYSTEM = 5;

    public static int VIDEO_STATUS = 0; //0：标清；1：流畅；2：高清；3：720P；4：1080P
    public static int VIDEO_ADAPT = 1;  //0:关闭，1:开启
    /*******************************/
}
