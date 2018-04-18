package com.nodepoint.residential.gilde;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.nodepoint.residential.DialActivity;
import com.nodepoint.residential.config.DeviceConfig;
import com.nodepoint.residential.util.SqlUtil;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.entity.FaceDataInfo;

/**
 * Created by lenovo on 2018/1/17.
 */

public class PullFaceReceiver extends BroadcastReceiver {
    private static final String TAG = "PullFaceReceiver";

    private Context context;
    SqlUtil sqlUtil = null;

    public void setContext(Context context) {
        this.context = context;
    }

    Handler facehandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.obj.equals("success")){
                ThreadManager.getSlingeThread().execute(new Runnable() {
                    @Override
                    public void run() {
//                        String url = "http://uat.jzwimb.com/app/pcfid/changeFaceComplete";
                        String url = DeviceConfig.SERVER_URL+ "/app/pcfid/changeFaceComplete";
                        HttpClient client = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost(url);
                        //323  82
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
                                        Log.e("提交成功","提交成功");
                                    }else{
                                        Log.e("提交失败","提交失败");
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


    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e("进来了", "进来了PullFaceService");
        final Bundle bundle = intent.getExtras();
        if (intent.getAction().equals("repeating")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    httpGetFaceData(context,bundle);
                }
            }).start();

//            ThreadManager.getSlingeThread().execute(new Runnable() {
//                @Override
//                public void run() {
//                    httpGetFaceData();
//                }
//            });

        }
    }

    public void httpGetFaceData(Context context, Bundle bundle) {
//        String faceUrl = "http://uat.jzwimb.com/app/pcfid/retrieveChangedFaceList?lockIds="+Constant.lockId;
        String faceUrl = DeviceConfig.SERVER_URL+ "/app/pcfid/retrieveChangedFaceList?lockIds="+Constant.lockId;
        Log.e("faceurl",faceUrl);
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
                Log.e("face数据", response.toString());
                JSONObject facejson = new JSONObject(response);
                int code = facejson.getInt("code");
                JSONArray data = facejson.getJSONArray("data");
                if (code == 0) {
                    for (int i = 0; i < data.length(); i++) {
                        Log.e("data的数量", data.length() + "");
                        JSONObject obj = data.getJSONObject(i);
                        int rid = obj.getInt("rid");
//                        int faceId = obj.getInt("faceId");
                        int userId = obj.getInt("userId");
//                        Log.e("userId", userId + "");
                        int communityId = obj.getInt("communityId");
                        String state = obj.getString("state");
                        String lockIds = obj.getString("lockIds");
                        String creDate = obj.getString("creDate");
                        int order = obj.getInt("order");
                        String faceData = obj.getString("faceData");
                        Log.e("faceData数据", faceData.length() + "");
                        byte[] facedata = Base64.decode(faceData, Base64.DEFAULT);
                        if (DialActivity.faceManager.addCharacter(rid, facedata)) {
                            Log.e("addFace", "添加人脸成功" + "rid: " + rid);

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
                            msg.obj = "success";
                            facehandler.sendMessage(msg);



                        } else {
                            Log.e("addFace", "添加人脸失败  rid: " + rid);
                        }
                    }
                } else {
                    Log.e(TAG,"请求失败");
                }
            }else{
                Log.e(TAG,"网络异常...............");
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
