package org.ovirt.engine.core.utils.hostinstall;

public interface IVdsInstallWrapper {
    boolean ConnectToServer(String server);

    boolean ConnectToServer(String server, String rootPassword);

    boolean ConnectToServer(String server, String rootPassword, long timeout);

    boolean ConnectToServer(String server, String certPath, String password);

    boolean DownloadFile(String source, String destination);

    void InitCallback(IVdsInstallCallBack callback);

    boolean RunSSHCommand(String command);

    boolean UploadFile(String source, String destination);

    void wrapperShutdown();
}
