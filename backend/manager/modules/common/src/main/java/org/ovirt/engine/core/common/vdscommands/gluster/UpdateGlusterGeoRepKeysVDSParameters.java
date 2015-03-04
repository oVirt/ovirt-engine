package org.ovirt.engine.core.common.vdscommands.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class UpdateGlusterGeoRepKeysVDSParameters extends VdsIdVDSCommandParametersBase {
    private String remoteUserName = "root";
    private List<String> geoRepPubKeys = new ArrayList<>();

    public UpdateGlusterGeoRepKeysVDSParameters() {

    }

    public UpdateGlusterGeoRepKeysVDSParameters(Guid vdsId, List<String> geoRepPubKeys) {
        super(vdsId);
        this.geoRepPubKeys = geoRepPubKeys;
    }

    public UpdateGlusterGeoRepKeysVDSParameters(Guid vdsId, List<String> geoRepPubKeys, String remoteUserName) {
        super(vdsId);
        this.remoteUserName = remoteUserName;
        this.geoRepPubKeys = geoRepPubKeys;
    }

    public String getRemoteUserName() {
        return remoteUserName;
    }

    public void setRemoteUserName(String remoteUserName) {
        this.remoteUserName = remoteUserName;
    }

    public List<String> getGeoRepPubKeys() {
        return geoRepPubKeys;
    }

    public void setGeoRepPubKeys(List<String> geoRepPubKeys) {
        this.geoRepPubKeys = geoRepPubKeys;
    }
}
