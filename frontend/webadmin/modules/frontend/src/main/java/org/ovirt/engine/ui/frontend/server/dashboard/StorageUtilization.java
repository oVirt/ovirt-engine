package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.ArrayList;
import java.util.List;

public class StorageUtilization implements Utilization {
    private List<UtilizedEntity> vms = new ArrayList<>();
    private List<UtilizedEntity> storage = new ArrayList<>();

    public List<UtilizedEntity> getVms() {
        return vms;
    }

    public void addVm(UtilizedEntity vm) {
        this.vms.add(vm);
    }

    public void addStorage(UtilizedEntity host) {
        this.storage.add(host);
    }

    public void addResource(UtilizedEntity resource) {
        addStorage(resource);
    }

    public List<UtilizedEntity> getStorage() {
        return storage;
    }

    public void clearStorage() {
        storage.clear();
    }

    public void clearVms() {
        vms.clear();
    }
}
