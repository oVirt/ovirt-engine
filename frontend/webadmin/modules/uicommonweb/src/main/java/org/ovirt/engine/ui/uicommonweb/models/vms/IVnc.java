package org.ovirt.engine.ui.uicommonweb.models.vms;

@SuppressWarnings("unused")
public interface IVnc
{
    String getTitle();

    void setTitle(String value);

    String getHost();

    void setHost(String value);

    int getPort();

    void setPort(int value);

    String getPassword();

    void setPassword(String value);

    void Connect();
}
