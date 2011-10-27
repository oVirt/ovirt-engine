package org.ovirt.engine.core.utils.hostinstall;

public interface IVdsInstallCallBack {
    void AddError(String error);

    void AddMessage(String message);

    void Connected();

    void EndTransfer();

    void Failed(String error);
}
