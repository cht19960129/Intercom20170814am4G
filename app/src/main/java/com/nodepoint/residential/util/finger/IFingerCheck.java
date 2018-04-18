package com.nodepoint.residential.util.finger;

/**
 * Created by simon on 2016/9/28.
 */
public interface IFingerCheck {
    public boolean checkFinger(byte[] thisFinger, byte[] fingerTemplate);
    public boolean isFingerChecking();
}
