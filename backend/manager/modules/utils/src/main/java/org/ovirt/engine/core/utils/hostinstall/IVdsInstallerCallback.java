package org.ovirt.engine.core.utils.hostinstall;

public interface IVdsInstallerCallback {
    void addError(String error);

    void addMessage(String message);

    void connected();

    void endTransfer();

    void failed(String error);
}
