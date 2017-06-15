package org.ovirt.engine.core.common.action;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

public class GlusterStorageSyncCommandParameters extends ActionParametersBase {

    private static final long serialVersionUID = 223157376819659037L;
    private Guid storageDomainId;
    private Guid geoRepSessionId;
    private Map<Guid, Guid> vmIdSnapshotIds;
    private DRStep nextStep;

    public DRStep getNextStep() {
        return nextStep;
    }

    public void setNextStep(DRStep nextStep) {
        this.nextStep = nextStep;
    }

    public enum DRStep {
        GEO_REP,
        REMOVE_TMP_SNAPSHOTS
    }

    public GlusterStorageSyncCommandParameters() {
        super();
    }

    public GlusterStorageSyncCommandParameters(Guid storageDomainId, Guid geoRepSessionId) {
        super();
        this.storageDomainId = storageDomainId;
        this.geoRepSessionId = geoRepSessionId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getGeoRepSessionId() {
        return geoRepSessionId;
    }

    public void setGeoRepSessionId(Guid geoRepSessionId) {
        this.geoRepSessionId = geoRepSessionId;
    }

    public Map<Guid, Guid> getVmIdSnapshotIds() {
        return vmIdSnapshotIds;
    }

    public void setVmIdSnapshotIds(Map<Guid, Guid> vmIdsnapshotIds) {
        this.vmIdSnapshotIds = vmIdsnapshotIds;
    }

}
