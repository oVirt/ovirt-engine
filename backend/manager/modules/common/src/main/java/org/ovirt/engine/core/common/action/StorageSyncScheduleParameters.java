package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.gluster.StorageSyncSchedule;
import org.ovirt.engine.core.compat.Guid;

public class StorageSyncScheduleParameters extends ActionParametersBase {

    private static final long serialVersionUID = -1229678351308315047L;

    private StorageSyncSchedule schedule;
    private Guid storageDomainId;
    private Guid geoRepSessionId;

    public StorageSyncScheduleParameters() {
        super();
    }

    public StorageSyncScheduleParameters(StorageSyncSchedule schedule, Guid storageDomainId, Guid geoRepSessionId) {
        this.schedule = schedule;
        this.storageDomainId = storageDomainId;
        this.geoRepSessionId = geoRepSessionId;
    }

    public StorageSyncSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(StorageSyncSchedule schedule) {
        this.schedule = schedule;
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

}
