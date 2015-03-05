package org.ovirt.engine.core.common.action.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.compat.Guid;

public class UpdateGlusterHostPubKeyToSlaveParameters extends IdParameters {

    private static final long serialVersionUID = 1L;

    private String remoteUserName;
    private List<String> pubKeys = new ArrayList<>();

    public UpdateGlusterHostPubKeyToSlaveParameters() {
        super();
    }

    public UpdateGlusterHostPubKeyToSlaveParameters(Guid vdsId, List<String> pubKeys) {
        this(vdsId, pubKeys, null);
    }

    public UpdateGlusterHostPubKeyToSlaveParameters(Guid vdsId, List<String> pubKeys, String remoteUserName) {
        super(vdsId);
        this.remoteUserName = remoteUserName;
        this.pubKeys = pubKeys;
    }

    public String getRemoteUserName() {
        return remoteUserName;
    }

    public void setRemoteUserName(String remoteUserName) {
        this.remoteUserName = remoteUserName;
    }

    public List<String> getPubKeys() {
        return pubKeys;
    }

    public void setPubKeys(List<String> pubKeys) {
        this.pubKeys = pubKeys;
    }
}
