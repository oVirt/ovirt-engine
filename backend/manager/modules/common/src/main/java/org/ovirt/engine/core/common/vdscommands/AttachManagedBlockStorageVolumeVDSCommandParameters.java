package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;

public class AttachManagedBlockStorageVolumeVDSCommandParameters extends VdsIdAndVdsVDSCommandParametersBase {

    private Map<String, Object> connectionInfo;
    private Guid volumeId;
    private Guid storageDomainId;

    public AttachManagedBlockStorageVolumeVDSCommandParameters() {
    }

    public AttachManagedBlockStorageVolumeVDSCommandParameters(VDS vds) {
        super(vds);
    }

    public AttachManagedBlockStorageVolumeVDSCommandParameters(VDS vds,
            Map<String, Object> connectionInfo,
            Guid storageDomainId) {
        super(vds);
        this.connectionInfo = connectionInfo;
        this.storageDomainId = storageDomainId;
    }

    public AttachManagedBlockStorageVolumeVDSCommandParameters(Map<String, Object> connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public Map<String, Object> getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(Map<String, Object> connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}
