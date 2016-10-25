package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.DcStatusMap;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.GlusterVolumeStatusMap;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.HostStatusMap;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.StorageStatusMap;
import org.ovirt.engine.ui.frontend.server.dashboard.maps.VmStatusMap;

public class Inventory {
    private InventoryStatus dc;
    private InventoryStatus cluster;
    private InventoryStatus host;
    private InventoryStatus storage;
    private InventoryStatus vm;
    private InventoryStatus event;
    private InventoryStatus volume;

    public InventoryStatus getDc() {
        return dc;
    }

    public void setDc(InventoryStatus dc) {
        this.dc = dc;
        for (DcStatusMap status: DcStatusMap.values()) {
            List<String> statusValues = Arrays.asList(status.getStringValues());
            dc.setStatusValues(status.name().toLowerCase(), statusValues);
        }
    }

    public InventoryStatus getCluster() {
        return cluster;
    }

    public void setCluster(InventoryStatus cluster) {
        this.cluster = cluster;
    }

    public InventoryStatus getHost() {
        return host;
    }

    public void setHost(InventoryStatus host) {
        this.host = host;
        for (HostStatusMap status: HostStatusMap.values()) {
            List<String> statusValues = Arrays.asList(status.getStringValues());
            host.setStatusValues(status.name().toLowerCase(), statusValues);
        }
    }

    public InventoryStatus getStorage() {
        return storage;
    }

    public void setStorage(InventoryStatus storage) {
        this.storage = storage;
        for (StorageStatusMap status: StorageStatusMap.values()) {
            List<String> statusValues = Arrays.asList(status.getStringValues());
            storage.setStatusValues(status.name().toLowerCase(), statusValues);
        }
    }

    public InventoryStatus getVm() {
        return vm;
    }

    public void setVm(InventoryStatus vm) {
        this.vm = vm;
        for (VmStatusMap status: VmStatusMap.values()) {
            List<String> statusValues = Arrays.asList(status.getStringValues());
            vm.setStatusValues(status.name().toLowerCase(), statusValues);
        }
    }

    public InventoryStatus getEvent() {
        return event;
    }

    public void setEvent(InventoryStatus event) {
        for (AuditLogSeverity severity: AuditLogSeverity.values()) {
            event.setStatusValues(severity.name().toLowerCase(), Arrays.asList(severity.name().toLowerCase()));
        }
        this.event = event;
    }

    public InventoryStatus getVolume() {
        return volume;
    }

    public void setVolume(InventoryStatus volume) {
        this.volume = volume;
        for (GlusterVolumeStatusMap status : GlusterVolumeStatusMap.values()) {
            List<String> statusValues = Arrays.asList(status.getStringValues());
            volume.setStatusValues(status.name().toLowerCase(), statusValues);
        }
    }
}
