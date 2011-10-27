package org.ovirt.engine.core.utils.hostinstall;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.UserInfo;

public class Credentials implements UserInfo {
    private static Log log = LogFactory.getLog(Credentials.class);
    private String password, passphrase, certPath, username;

    public Credentials() {
    }

    public String getCertPath() {
        return certPath;
    }

    @Override
    public String getPassphrase() {
        return passphrase;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean promptPassphrase(String arg0) {
        log.debug(arg0);
        return true;
    }

    @Override
    public boolean promptPassword(String arg0) {
        log.debug(arg0);
        return true;
    }

    @Override
    public boolean promptYesNo(String arg0) {
        log.debug(arg0);
        return true;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void showMessage(String arg0) {
        log.debug(arg0);
    }
}
