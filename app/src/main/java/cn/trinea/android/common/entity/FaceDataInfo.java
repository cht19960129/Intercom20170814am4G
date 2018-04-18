package cn.trinea.android.common.entity;

import java.io.Serializable;

/**
 * Created by lenovo on 2018/1/18.
 */

public class FaceDataInfo implements Serializable{


    /**
     * rid : 24
     * faceId : 11
     * userId : 1
     * communityId : 31
     * state : N
     * lockIds : 49
     * creDate : 2018-01-18T02:40:44.000Z
     * order : 0
     * faceData : nFm0STArdIv
     */

    public int rid;
//    public int faceId;
    public int userId;
    public int communityId;
    public String state;
    public int lockIds;
    public String creDate;
    public int orderId;
    public String faceData;

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

//    public int getFaceId() {
//        return faceId;
//    }
//
//    public void setFaceId(int faceId) {
//        this.faceId = faceId;
//    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCommunityId() {
        return communityId;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getLockIds() {
        return lockIds;
    }

    public void setLockIds(int lockIds) {
        this.lockIds = lockIds;
    }

    public String getCreDate() {
        return creDate;
    }

    public void setCreDate(String creDate) {
        this.creDate = creDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int order) {
        this.orderId = orderId;
    }

    public String getFaceData() {
        return faceData;
    }

    public void setFaceData(String faceData) {
        this.faceData = faceData;
    }
}
