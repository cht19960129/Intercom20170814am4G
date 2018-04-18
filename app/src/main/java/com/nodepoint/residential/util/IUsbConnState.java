package com.nodepoint.residential.util;

public interface IUsbConnState {
    void onUsbConnected();

	void onUsbPermissionDenied();

	void onDeviceNotFound();
}
