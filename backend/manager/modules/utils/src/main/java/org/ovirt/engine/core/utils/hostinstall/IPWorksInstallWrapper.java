package org.ovirt.engine.core.utils.hostinstall;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

//Doron Fediuck: IPWorksInstallWrapper class is not being used at all.  I kept it as a backup until we have full functional alternative by Apache, and I still wait for internal scp & fingerprint implementation.

public class IPWorksInstallWrapper implements IVdsInstallWrapper {
    private IVdsInstallCallBack callback;
    Session session = null;

    public IPWorksInstallWrapper() {
    }

    private boolean _do_connect(String server, Credentials creds) {
        boolean returnValue = true;
        try {
            JSch jsch = new JSch();
            if (creds.getCertPath() != null) {
                log.debug("Using Public Key Authentication.");
                jsch.addIdentity(creds.getCertPath(), creds.getPassphrase());
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            session = jsch.getSession(creds.getUsername(), server, 22);
            session.setOutputStream(output);
            session.setUserInfo(creds);
            session.connect();
            if (log.isDebugEnabled()) {
                log.debug(output.toString());
            }
            callbackConnected();
        } catch (JSchException e) {
            callbackFailed(e.getMessage());
            returnValue = false;
            log.error("Could not connect to server " + server, e);
        }
        return returnValue;
    }

    protected void callbackAddError(String message) {
        if (haveCallback()) {
            callback.AddError(message);
        }
    }

    protected void callbackAddMessage(String message) {
        if (haveCallback()) {
            callback.AddMessage(message);
        }
    }

    protected void callbackConnected() {
        if (haveCallback()) {
            // TODO: Remove comment when Apache MINA support Finger-Print

            // callback.AddMessage(String.format(
            // "<BSTRAP component='RHEV_INSTALL' status='OK' message='Connected to Host %s with SSH Key Fingerprint: %s'/>",
            // session.getHost(),
            // session.getFingerprint()));
            callback.Connected();
        }
    }

    protected void callbackEndTransfer() {
        if (haveCallback()) {
            callback.EndTransfer();
        }
    }

    protected void callbackFailed(String message) {
        if (haveCallback()) {
            callback.Failed(message);
        }
    }

    public final boolean ConnectToServer(String server) {
        return ConnectToServer(server, Config.resolveCertificatePath(),
                Config.<String> GetValue(ConfigValues.CertificatePassword));
    }

    public final boolean ConnectToServer(String server, String rootPassword) {
        Credentials creds = new Credentials();
        creds.setPassword(rootPassword);
        creds.setUsername("root");
        return _do_connect(server, creds);
    }

    public final boolean ConnectToServer(String server, String certPath, String password) {
        Credentials creds = new Credentials();
        creds.setPassphrase(password);
        creds.setCertPath(certPath);
        creds.setUsername("root");
        return _do_connect(server, creds);
    }

    public final boolean DownloadFile(String source, String destination) {
        log.info(String.format("Downloading file %s to %s on %s", source, destination, session.getHost()));
        boolean returnValue = true;
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.get(source, destination);
            String output = null;
            int exitStatus = 0;
            exitStatus = channel.getExitStatus();
            if (exitStatus > 0) {
                returnValue = false;
                callbackAddError("Exit Status from transfer: " + exitStatus);
                log.error(output);
            } else {
                callbackEndTransfer();
            }
        } catch (Exception e) {
            callbackAddError(e.getMessage());
            log.error(e);
            returnValue = false;
        }
        return returnValue;
    }

    protected boolean haveCallback() {
        return callback != null;
    }

    public final void InitCallback(IVdsInstallCallBack callback) {
        this.callback = callback;
    }

    protected String readInput(InputStream input) {
        StringBuilder builder = new StringBuilder();
        byte[] tmp = new byte[1024];
        try {
            while (input.available() > 0) {
                int i = input.read(tmp, 0, 1024);
                if (i < 0)
                    break;
                builder.append(new String(tmp, 0, i));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    public final boolean RunSSHCommand(String command) {
        boolean returnValue = true;
        log.info(String.format("Invoking %s on %s", command, session.getHost()));
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream result = channel.getInputStream();
            InputStream error = channel.getErrStream();
            channel.connect();
            int exitStatus = 0;
            while (true) {
                String sshMessage = readInput(result);
                if (sshMessage != null && sshMessage.length() > 0) {
                    log.debug(sshMessage);
                    callbackAddMessage(sshMessage);
                }

                String errorSshMessage = readInput(error);
                if (errorSshMessage != null && errorSshMessage.length() > 0) {
                    log.debug(errorSshMessage);
                    callbackAddError(errorSshMessage);
                }
                if (channel.isClosed()) {
                    exitStatus = channel.getExitStatus();
                    log.debug("exit-status: " + exitStatus);
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
            if (exitStatus != 0) {
                returnValue = false;
                String errorString = readInput(error);
                callbackAddError(errorString);
                log.error(errorString);
            }
        } catch (Exception e) {
            callbackFailed(e.getMessage());
            log.error("Error running command " + command, e);
            returnValue = false;
        }
        return returnValue;
    }

    public final boolean UploadFile(String source, String destination) {
        log.info(String.format("Uploading file %s to %s on %s", source, destination, session.getHost()));
        File file = new File(source);
        boolean returnValue = true;
        if (file.exists()) {
            try {
                ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();
                channel.put(source, destination);
                String output = null;
                int exitStatus = 0;
                exitStatus = channel.getExitStatus();
                if (exitStatus > 0) {
                    returnValue = false;
                    callbackAddError("Exit Status from transfer: " + exitStatus);
                    log.error(output);
                } else {
                    callbackEndTransfer();
                }
            } catch (Exception e) {
                callbackAddError(e.getMessage());
                log.error(e);
                returnValue = false;
            }
        } else {
            this.callbackFailed(String.format("Upload failed. File: %s not exist", source));
            log.error("File to upload does not exist: " + source);
            returnValue = false;
        }
        return returnValue;
    }

    private static Log log = LogFactory.getLog(IPWorksInstallWrapper.class);

    @Override
    public void wrapperShutdown() {
        throw new UnsupportedOperationException("This class should not be used, and have no support in installation actions.");
    }

    /**
     * The methods does not implement the timeout.
     */
    @Override
    public boolean ConnectToServer(String server, String rootPassword, long timeout) {
        return ConnectToServer(server, rootPassword);
    }
}
