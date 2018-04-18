package com.nodepoint.residential.util.finger;

public interface IUsbConnState {
    void onUsbConnected();

	void onUsbPermissionDenied();

	void onDeviceNotFound();
}
