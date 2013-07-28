package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

/**
 * Parameter class for Gluster Servers queries
 */
public class GlusterServersQueryParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -3541250057200360191L;

    private String serverName;

    private String password;

    private String fingerprint;

    public GlusterServersQueryParameters() {
    }

    public GlusterServersQueryParameters(String serverName, String password) {
        setServerName(serverName);
        setPassword(password);
    }

    public GlusterServersQueryParameters(String serverName) {
        setServerName(serverName);
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

}
