package cn.trinea.android.common.entity;

import java.io.Serializable;

/**
 * Created by lenovo on 2018/1/14.
 */

public class FaceInfo implements Serializable {

    /**
     * rid : 2
     * faceId : 2
     * userId : 350
     * communityId : 47
     * lockId : 173
     * creDate : 2018-01-11T15:35:13.000Z
     * order : 0
     * facepicpath : \repository\face\557d53155e3df417607bef28968e6f36.jpg
     * faceData :
     * upDate : 2018-01-11T15:35:13.000Z
     * pictureUrl : 192.168.0.102:80/repository/face/557d53155e3df417607bef28968e6f36.jpg
     */

    public int rid;
    public int faceId;
    public int userId;
    public int communityId;
    public int lockId;
    public String creDate;
//    public int order;
    public String facepicpath;
    public String faceData;
//    public String upDate;
    public String pictureUrl;

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

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

    public int getLockId() {
        return lockId;
    }

    public void setLockId(int lockId) {
        this.lockId = lockId;
    }

    public String getCreDate() {
        return creDate;
    }

    public void setCreDate(String creDate) {
        this.creDate = creDate;
    }

//    public int getOrder() {
//        return order;
//    }
//
//    public void setOrder(int order) {
//        this.order = order;
//    }

    public String getFacepicpath() {
        return facepicpath;
    }

    public void setFacepicpath(String facepicpath) {
        this.facepicpath = facepicpath;
    }

    public String getFaceData() {
        return faceData;
    }

    public void setFaceData(String faceData) {
        this.faceData = faceData;
    }

//    public String getUpDate() {
//        return upDate;
//    }
//
//    public void setUpDate(String upDate) {
//        this.upDate = upDate;
//    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
