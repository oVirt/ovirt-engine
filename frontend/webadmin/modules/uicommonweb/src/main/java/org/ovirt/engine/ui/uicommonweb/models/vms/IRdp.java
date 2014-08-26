package org.ovirt.engine.ui.uicommonweb.models.vms;


public interface IRdp {

    String getAddress();

    void setAddress(String value);

    String getGuestID();

    void setGuestID(String value);

    boolean getUseLocalDrives();

    void setUseLocalDrives(boolean value);

    boolean getEnableCredSspSupport();

    void connect();
}
