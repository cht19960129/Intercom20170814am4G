package com.nodepoint.residential.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.biometric.oneface.env.DBHelper;
import com.nodepoint.residential.config.DeviceConfig;
import com.nodepoint.residential.util.finger.IFingerCheck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.entity.FaceDataInfo;
import cn.trinea.android.common.entity.FaceInfo;

/**
 * Created by simon on 2016/7/11.
 */


public class SqlUtil {
    static final int SZ_FP_TEMPLATE_SIZE = (570);
    public static final String DATABASE_FILE_NAME = "residential.db";
    public static final int DATABASE_VERSION = 3;
    private SQLiteDatabase db = null;
    private SqlHelper sqlHelper = null;
    private ArrayList<FingerData> fingerList = new ArrayList<FingerData>();

    //人脸识别相关
    private ArrayList<FaceDataInfo> faceDataList = new ArrayList<FaceDataInfo>();


    public SqlUtil(Context context) {
        sqlHelper = new SqlHelper(context);
        db = sqlHelper.getWritableDatabase();
    }

    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    public void changeFinger(JSONArray data) {
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                try {
                    JSONObject fingerItem = data.getJSONObject(i);
                    int lockIndex = fingerItem.getInt("lockIndex");
                    int lockId = fingerItem.getInt("lockId");
                    int userId = fingerItem.getInt("userId");
                    int employeeId = fingerItem.getInt("employeeId");
                    String finger = fingerItem.getString("finger");
                    String state = fingerItem.getString("state");
                    if (state.equals("N") || state.equals("F") || state.equals("G")) {
                        writeFinger(lockId, lockIndex, userId, employeeId, finger);
                    } else if (state.equals("D") || state.equals("R")) {
                            removeFinger(lockId, lockIndex, userId, employeeId);
                    } else if (state.equals("U")) {
                        updateFinger(lockId, lockIndex, userId, employeeId, finger);
                    }
                } catch (JSONException e) {
                }
            }
        }
    }

//    //人脸识别相关
//    public void changeFace(JSONArray data) {
//        if(data != null){
//            for (int i = 0; i < data.length(); i++) {
//                try {
//                    JSONObject faceItem = data.getJSONObject(i);
//                    int lockIndex = faceItem.getInt("lockIndex");
//                    int lockId = faceItem.getInt("lockId");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    }

    public void changeCard(JSONArray data) {
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                try {
                    JSONObject cardItem = data.getJSONObject(i);
                    int lockIndex = cardItem.getInt("lockIndex");
                    int lockId = cardItem.getInt("lockId");
                    String cardNo = cardItem.getString("cardNo");
                    String state = cardItem.getString("state");
                    if (state.equals("N") || state.equals("F") || state.equals("G") || state.equals("U")) {
                        writeCard(lockId, lockIndex, cardNo);
                    } else if (state.equals("D") || state.equals("R")) {
                        removeCard(lockId, lockIndex, cardNo);
                    }
                } catch (JSONException e) {
                }
            }
        }
    }

    public void clearDeviceData() {
        try {
            db.execSQL("DELETE FROM RE_CARD", new Object[]{});
        } finally {
        }

        try {
            db.execSQL("DELETE FROM RE_FINGER", new Object[]{});
        } finally {
        }
        fingerList.clear();

        //人脸识别相关
        try {
            db.execSQL("DELETE FROM RE_FACEDATA", new Object[]{});
        } finally {

        }
        faceDataList.clear();

    }

    protected void writeCard(int lockId, int lockIndex, String cardNo) {
        try {
            db.execSQL("INSERT INTO RE_CARD(lockId,lockIndex,cardNo)"
                    + " VALUES(?, ?, ?)", new Object[]{lockId, lockIndex, cardNo});
        } finally {
        }
    }

    protected void removeCard(int lockId, int lockIndex, String cardNo) {
        db.execSQL("DELETE FROM RE_CARD where lockId=? and cardNo=?", new Object[]{lockId, cardNo});
    }

    protected byte[] convertData(String data) {
        JSONArray array = null;
        byte[] byteData = null;
        try {
            array = new JSONArray(data);
        } catch (JSONException e) {
        }

        if (array != null) {
            byteData = new byte[array.length()];
            for (int i = 0; i < array.length(); i++) {
                try {
                    byteData[i] = (byte) array.getInt(i);
                } catch (JSONException e) {
                }
            }
        }
        return byteData;
    }

    public void initFingerList() {
        int fingerNum = getFingerNum();
        System.out.println(fingerNum);
//        if(fingerNum!=0){
//            clearDeviceData();
//        }
//        Thread thread=new Thread(){
//            public void run() {
//                try {
//                    initTestFinger();
//                }catch(Exception e){
//                }
//            }
//        };
//        thread.start  ();
    }

    //加载指纹列表
    public void loadFingerList() {
        String sql = "select userId,employeeId,finger from RE_FINGER";
        Cursor cursor = db.rawQuery(sql, new String[]{});
        while (cursor.moveToNext()) {
            FingerData fingerData = new FingerData();
            fingerData.userId = cursor.getInt(0);
            fingerData.employeeId = cursor.getInt(1);
            fingerData.finger = cursor.getBlob(2);
            fingerList.add(fingerData);
        }
        cursor.close();
    }

//    //人脸识别相关   加载人脸列表   查询RE_FACE表的信息 并放入faceList集合中  不返回数据
//    public void loadFaceList(){
//        //select rid,faceId,userId,communityId,lockId,creDate,order,facepicpath,faceData,upDate,pictureUrl from RE_FACE
//        String sql = "select rid,faceId,userId,communityId,lockId,creDate,facepicpath,faceData,pictureUrl from RE_FACE";
//        Cursor cursor = db.rawQuery(sql, new String[]{});
//        while (cursor.moveToNext()){
//            FaceInfo faceInfo = new FaceInfo();
//            faceInfo.rid = cursor.getInt(0);
//            faceInfo.faceId = cursor.getInt(1);
//            faceInfo.userId = cursor.getInt(2);
//            faceInfo.communityId = cursor.getInt(3);
//            faceInfo.lockId = cursor.getInt(4);
//            faceInfo.creDate = cursor.getString(5);
////            faceInfo.order = cursor.getInt(6);
//            faceInfo.facepicpath = cursor.getString(6);
//            faceInfo.faceData = cursor.getString(7);
////            faceInfo.upDate = cursor.getString(9);
//            faceInfo.pictureUrl = cursor.getString(8);
//            faceList.add(faceInfo);
//        }
//        cursor.close();
//    }
//    //获取face的数量
//    public int getFaceNum() {
//        return faceList.size();
//    }



    //人脸识别相关   addFace
    public void addFaceData(FaceDataInfo faceDataInfo){
//        //实例化常量值
//        ContentValues cValue = new ContentValues();
//        //添加信息
//        cValue.put("rid",faceDataInfo.getRid());
//        cValue.put("faceId",faceDataInfo.getFaceId());
//        cValue.put("userId",faceDataInfo.getUserId());
//        cValue.put("communityId",faceDataInfo.getCommunityId());
//        cValue.put("state",faceDataInfo.getState());
//        cValue.put("lockIds",faceDataInfo.getLockIds());
//        cValue.put("creDate",faceDataInfo.getCreDate());
//        cValue.put("orderId",faceDataInfo.getOrderId());
//        cValue.put("faceData",faceDataInfo.getFaceData());
//        db.insert("RE_FACEDATA",null,cValue);
        //开启事务
        db.beginTransaction();
        String sql = "insert into RE_FACEDATA(rid,userId,communityId,state,lockIds,creDate,orderId,faceData) values(?,?,?,?,?,?,?,?)";//faceId,
        try {
            db.execSQL(sql,new Object[]{faceDataInfo.getRid(),faceDataInfo.getUserId(),faceDataInfo.getCommunityId(),  //faceDataInfo.getFaceId(),
                    faceDataInfo.getState(),faceDataInfo.getLockIds(),faceDataInfo.getCreDate(),faceDataInfo.getOrderId(),faceDataInfo.getFaceData()});
            //设置事务成功完成
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            //结束事务
            db.endTransaction();
        }
    }

    //人脸识别相关  查询所有
    public List<FaceDataInfo> queryAllFaceData(){
//        ArrayList<FaceDataInfo> list = new ArrayList<FaceDataInfo>();
        Cursor cursor = db.rawQuery("select rid,userId,communityId,state,lockIds,creDate,orderId,faceData from RE_FACEDATA",null);//faceId,
        while (cursor.moveToNext()){
            FaceDataInfo faceDataInfo = new FaceDataInfo();
            faceDataInfo.rid = cursor.getInt(0);
//            faceDataInfo.faceId = cursor.getInt(1);
            faceDataInfo.userId = cursor.getInt(1);
            faceDataInfo.communityId = cursor.getInt(2);
            faceDataInfo.state = cursor.getString(3);
            faceDataInfo.lockIds = cursor.getInt(4);
            faceDataInfo.creDate = cursor.getString(5);
            faceDataInfo.orderId = cursor.getInt(6);
            faceDataInfo.faceData = cursor.getString(7);
            faceDataList.add(faceDataInfo);
        }
        cursor.close();
        return faceDataList;
    }


    //删除faceData   根据rid
    public boolean deleteFaceDataById(int rid) {
        //删除SQL语句
        try {
            String sql = "delete from RE_FACEDATA where rid ="+rid;
            //执行SQL语句
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    //删除facedata
    public boolean deleteAllFaceData(){
        String  sql = "delete from RE_FACEDATA";
        try {
            db.execSQL(sql, new Object[]{});
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updateFaceDataById(int rid, int communityId, String faceData){//int faceId,
        try {
            String sql = "update RE_FACEDATA set faceData = " + faceData + " where rid = " + rid +  " and communityId = " + communityId;//" and faceId = " + faceId +
            Log.e("update FaceData",sql);
            db.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



//    //人脸识别相关  查询所有
//    public List<FaceInfo> queryAllFace(){
//        ArrayList<FaceInfo> list = new ArrayList<FaceInfo>();
//        Cursor cursor = db.rawQuery("select rid,faceId,userId,communityId,lockId,creDate,facepicpath,faceData,pictureUrl from RE_FACE",null);
//        while (cursor.moveToNext()){
//            FaceInfo faceInfo = new FaceInfo();
//            faceInfo.rid = cursor.getInt(0);
//            faceInfo.faceId = cursor.getInt(1);
//            faceInfo.userId = cursor.getInt(2);
//            faceInfo.communityId = cursor.getInt(3);
//            faceInfo.lockId = cursor.getInt(4);
//            faceInfo.creDate = cursor.getString(5);
////            faceInfo.order = cursor.getInt(6);
//            faceInfo.facepicpath = cursor.getString(6);
//            faceInfo.faceData = cursor.getString(7);
////            faceInfo.upDate = cursor.getString(9);
//            faceInfo.pictureUrl = cursor.getString(8);
//            faceList.add(faceInfo);
//        }
//        cursor.close();
//        return list;
//    }



        protected void initTestFinger() {
        File[] files = new File[0];
        String SDCard = Environment.getExternalStorageDirectory() + "";
        String dir = SDCard + "/" + DeviceConfig.LOCAL_FILE_PATH + "2";
        File path = new File(dir);
        if (path.isDirectory()) {
            files = path.listFiles();
        }
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            fileName = fileName.substring(0, fileName.indexOf("."));
            int userId = Integer.parseInt(fileName);
            int lockId = 0;
            int lockIndex = userId;
            byte[] data = initTestFinger(files[i]);
            writeFinger(lockId, lockIndex, userId, 0, data);
        }
    }

    protected byte[] initTestFinger(File file) {
        byte[] fingerData = new byte[SZ_FP_TEMPLATE_SIZE];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fingerData, 0, SZ_FP_TEMPLATE_SIZE);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fingerData;
    }

    protected void writeFinger(int lockId, int lockIndex, int userId, int employeeId, String finger) {
        byte[] fingerData = convertData(finger);
        writeFinger(lockId, lockIndex, userId, employeeId, fingerData);
    }

    protected void writeFinger(int lockId, int lockIndex, int userId, int employeeId, byte[] fingerData) {
        try {
            db.execSQL("INSERT INTO RE_FINGER(lockId,lockIndex,userId,employeeId,finger)"
                    + " VALUES(?, ?, ?, ?, ?)", new Object[]{lockId,
                    lockIndex, userId, employeeId, fingerData});
        } finally {
        }
        FingerData fData = new FingerData();
        fData.userId = userId;
        fData.employeeId = employeeId;
        fData.finger = fingerData;
        fingerList.add(fData);
    }

    protected void updateFinger(int lockId, int lockIndex, int userId, int employeeId, String finger) {
        try {
            byte[] fingerData = convertData(finger);
            db.execSQL("UPDATE RE_FINGER set finger=? where lockId=? and userId=? and employeeId=?", new Object[]{fingerData, lockId, userId, employeeId});
            removeFingerFromList(userId, employeeId);
            FingerData fData = new FingerData();
            fData.userId = userId;
            fData.employeeId = employeeId;
            fData.finger = fingerData;
            fingerList.add(fData);
        } finally {
        }
    }

    protected void removeFingerFromList(int userId, int employeeId) {
        int length = fingerList.size();
        for (int i = 0; i < length; i++) {
            FingerData fingerData = fingerList.get(i);
            if (fingerData.userId == userId && fingerData.employeeId == employeeId) {
                fingerList.remove(i);
                break;
            }
        }
    }

    protected void removeFinger(int lockId, int lockIndex, int userId, int employeeId) {
        db.execSQL("DELETE FROM RE_FINGER where lockId=? and userId=? and employeeId=?", new Object[]{lockId, userId, employeeId});
        removeFingerFromList(userId, employeeId);
    }

    protected void clearFinger() {
        db.execSQL("DELETE FROM RE_FINGER", new Object[]{});
        fingerList.clear();
    }



    protected void clearCard() {
        db.execSQL("DELETE FROM RE_CARD", new Object[]{});
    }

    public boolean checkCardAvailable(String cardNo) {
        Cursor cursor = db.rawQuery("select count(*) as cardNum from RE_CARD where cardNo=?", new String[]{cardNo});
        boolean result = false;
        if (cursor.moveToFirst()) {
            int cardNum = cursor.getInt(cursor.getColumnIndex("cardNum"));
            if (cardNum > 0) {
                result = true;
            }
        }
        cursor.close();
        return result;
    }

    public int getFingerNum() {
        return fingerList.size();
//        Cursor cursor = db.rawQuery("select count(*) as fingerNum from RE_FINGER",new String[]{});
//        int fingerNum=0;
//        if(cursor.moveToFirst()) {
//            fingerNum = cursor.getInt(cursor.getColumnIndex("fingerNum"));
//        }
//        cursor.close();
//        return fingerNum;
    }

//    private List<byte[]> getFingers(int from, int length){
//        String sql=null;
//        if(from==0&&length==0){
//            sql="select finger from RE_FINGER";
//        }else{
//            sql="select finger from RE_FINGER LIMIT ? OFFSET ?";
//        }
//
//        Cursor cursor = db.rawQuery(sql,new String[]{});
//        List<byte[]> fingerList=new ArrayList<byte[]>();
//        while(cursor.moveToNext())
//        {
//            byte[] fingerData= cursor.getBlob(0);
//            fingerList.add(fingerData);
//        }
//        cursor.close();
//        return fingerList;
//    }

    public FingerData checkFinger(byte[] thisFinger, IFingerCheck iFingerCheck, int limit, int offset) {
        int listLength = getFingerNum();
        FingerData thisFingerData = null;
        try {
            for (int i = 0; i < limit; i++) {
                if (iFingerCheck.isFingerChecking()) {
                    int index = offset + i;
                    if (index < listLength) {
                        FingerData fingerData = fingerList.get(index);
                        if (fingerData.finger != null) {
                            if (iFingerCheck.checkFinger(thisFinger, fingerData.finger)) {
                                thisFingerData = fingerData;
                                break;
                            }
                        } else {
                            Log.v("SqlUtil", "------>showing image<-------" + fingerData.userId + "------" + index);  //空指纹下标
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
        }
        return thisFingerData;
    }



//    public int checkFinger(byte[] thisFinger,IFingerCheck iFingerCheck,int limit,int offset){
//        String sql="select finger,userId from RE_FINGER LIMIT ? OFFSET ?";
//        Cursor cursor = db.rawQuery(sql,new String[]{String.valueOf(limit),String.valueOf(offset)});
//        List<byte[]> fingerList=new ArrayList<byte[]>();
//        int findUserId=0;
//        try {
//            while (cursor.moveToNext() && iFingerCheck.isFingerChecking()) {
//                byte[] fingerData = cursor.getBlob(0);
//                int userId=cursor.getInt(1);
//                if (iFingerCheck.checkFinger(thisFinger, fingerData)) {
//                    findUserId =userId;
//                    break;
//                }
//            }
//        }catch(Exception e){
//        }
//        cursor.close();
//        return findUserId;
//    }
}

class SqlHelper extends SQLiteOpenHelper {

    public SqlHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                     int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public SqlHelper(Context context) {
        super(context, SqlUtil.DATABASE_FILE_NAME, null, SqlUtil.DATABASE_VERSION);
    }

    protected void createDatabase(SQLiteDatabase db) {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("CREATE TABLE IF NOT EXISTS RE_CARD (");
        stringBuffer.append("lockId INT NOT NULL,");
        stringBuffer.append("lockIndex INT NOT NULL,");
        stringBuffer.append("cardNo TEXT)");

        // 执行创建表的SQL语句
        try {
            db.execSQL(stringBuffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


        //创建指纹表信息
        stringBuffer = new StringBuffer();

        stringBuffer.append("CREATE TABLE IF NOT EXISTS RE_FINGER (");
        stringBuffer.append("lockId INT NOT NULL,");
        stringBuffer.append("lockIndex INT NOT NULL,");
        stringBuffer.append("userId INT NOT NULL,");
        stringBuffer.append("employeeId INT NOT NULL,");
        stringBuffer.append("finger BLOB)");

        // 执行创建表的SQL语句
        try {
            db.execSQL(stringBuffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        //创建  face 信息表    先铭
//        StringBuffer stringBufferface = new StringBuffer();
//        stringBufferface.append("CREATE TABLE IF NOT EXISTS RE_FACE(");
//        stringBufferface.append("rid INT NOT NULL,");
//        stringBufferface.append("faceId INT NOT NULL,");
//        stringBufferface.append("userId INT NOT NULL,");
//        stringBufferface.append("communityId INT NOT NULL,");
//        stringBufferface.append("lockId INT NOT NULL,");
//        stringBufferface.append("creDate TEXT NOT NULL,");
////        stringBufferface.append("order INT NOT NULL,");//order  字段需要改 不然创建失败
//        stringBufferface.append("facepicpath TEXT NOT NULL,");
//        stringBufferface.append("faceData TEXT,");
////        stringBufferface.append("upDate TEXT NOT NULL,");//upDate  字段需要改 不然创建失败
//        stringBufferface.append("pictureUrl TEXT NOT NULL)");
//        Log.e("创建RE_FACE","CREATE TABLE IF NOT EXISTS RE_FACE(............)");
//        // 执行创建表的SQL语句
//        try {
//            db.execSQL(stringBufferface.toString());
//            Log.e("创建RE_FACE","创建RE_FACE表成功"+stringBufferface.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("创建RE_FACE","创建RE_FACE表失败"+stringBufferface.toString());
//        }

        //创建  face 信息表    罗工
        StringBuffer stringFace = new StringBuffer();

        stringFace.append("CREATE TABLE IF NOT EXISTS RE_FACEDATA(");
        stringFace.append("rid INT NOT NULL,");
//        stringFace.append("faceId INT NOT NULL,");
        stringFace.append("userId INT NOT NULL,");
        stringFace.append("communityId INT NOT NULL,");
        stringFace.append("state TEXT NOT NULL,");
        stringFace.append("lockIds INT NOT NULL,");
        stringFace.append("creDate TEXT NOT NULL,");
        stringFace.append("orderId INT NOT NULL,");//order  字段需要改 不然创建失败
        stringFace.append("faceData TEXT)");

        Log.e("创建RE_FACEDATA","CREATE TABLE IF NOT EXISTS RE_FACEDATA(............)");
        // 执行创建表的SQL语句
        try {
            db.execSQL(stringFace.toString());
            Log.e("创建RE_FACEDATA","创建RE_FACEDATA表成功"+stringFace.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("创建RE_FACEDATA","创建RE_FACEDATA表失败"+stringFace.toString());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 构建创建表的SQL语句（可以从SQLite Expert工具的DDL粘贴过来加进StringBuffer中）
        createDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            StringBuffer stringBuffer = new StringBuffer();

            stringBuffer.append("ALTER TABLE RE_FINGER MODIFY finger BLOB;");

            // 执行创建表的SQL语句
            try {
                sqLiteDatabase.execSQL(stringBuffer.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (oldVersion == 2) {
            StringBuffer stringBuffer = new StringBuffer();

            stringBuffer.append("ALTER TABLE RE_FINGER Add column employeeId int;");

            // 执行创建表的SQL语句
            try {
                sqLiteDatabase.execSQL(stringBuffer.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        !/


    }
}
