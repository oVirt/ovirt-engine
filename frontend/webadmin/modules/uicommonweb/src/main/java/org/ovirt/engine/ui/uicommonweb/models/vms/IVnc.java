package org.ovirt.engine.ui.uicommonweb.models.vms;

public interface IVnc {

    void setVncHost(String host);
    void setVncPort(String port);
    void setTicket(String ticket);
    void setTitle(String title);

    void invokeClient();

}
