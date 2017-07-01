package org.ovirt.engine.core.common.queries.hostdeploy;

import java.io.Serializable;

import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class RegisterVdsParameters extends QueryParametersBase implements Serializable {
    private static final long serialVersionUID = 4661626618754048420L;

    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    private void setVdsId(Guid value) {
        privateVdsId = value;
    }

    private String privateVdsHostName;

    public String getVdsHostName() {
        return privateVdsHostName;
    }

    private void setVdsHostName(String value) {
        privateVdsHostName = value;
    }

    private String privateVdsName;

    public String getVdsName() {
        return privateVdsName;
    }

    public void setVdsName(String value) {
        privateVdsName = value;
    }

    private String privateVdsUniqueId;

    public String getVdsUniqueId() {
        return privateVdsUniqueId;
    }

    private void setVdsUniqueId(String value) {
        privateVdsUniqueId = value;
    }

    private int privateVdsPort;

    public int getVdsPort() {
        return privateVdsPort;
    }

    private void setVdsPort(int value) {
        privateVdsPort = value;
    }

    private Guid privateClusterId;

    public Guid getClusterId() {
        return privateClusterId;
    }

    private void setClusterId(Guid value) {
        privateClusterId = value;
    }

    private int privateSSHPort;

    public int getSSHPort() {
        return privateSSHPort;
    }

    public void setSSHPort(int value) {
        privateSSHPort = value;
    }

    String privateSSHFingerprint;

    public String getSSHFingerprint() {
        return privateSSHFingerprint;
    }

    public void setSSHFingerprint(String hostSSHFingerprint) {
        privateSSHFingerprint = hostSSHFingerprint;
    }

    private String privateSSHUser;

    public String getSSHUser() {
        return privateSSHUser;
    }

    public void setSSHUser(String value) {
        privateSSHUser = value;
    }

    public RegisterVdsParameters(Guid vdsId, String vds_host_name, int ssh_port, String ssh_fingerprint,
            String ssh_user, String vds_name, String vds_unique_id, int vds_port, Guid cluster_id) {
        setVdsId(vdsId);
        setVdsHostName(vds_host_name);
        setSSHPort(ssh_port);
        setSSHFingerprint(ssh_fingerprint);
        setSSHUser(ssh_user);
        setVdsName(vds_name);
        setVdsUniqueId(vds_unique_id);
        setVdsPort(vds_port);
        setClusterId(cluster_id);
    }

    public RegisterVdsParameters() {
    }
}
