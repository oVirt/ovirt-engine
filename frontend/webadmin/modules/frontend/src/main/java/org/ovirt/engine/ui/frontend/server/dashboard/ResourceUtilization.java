package org.ovirt.engine.ui.frontend.server.dashboard;

import java.util.ArrayList;
import java.util.List;

public class ResourceUtilization implements Utilization {
    private List<UtilizedEntity> vms = new ArrayList<>();
    private List<UtilizedEntity> hosts = new ArrayList<>();

    public List<UtilizedEntity> getVms() {
        return vms;
    }

    public void addVm(UtilizedEntity vm) {
        this.vms.add(vm);
    }

    public void addHost(UtilizedEntity host) {
        this.hosts.add(host);
    }

    public void addResource(UtilizedEntity resource) {
        addHost(resource);
    }

    public List<UtilizedEntity> getHosts() {
        return hosts;
    }

    public void clearHosts() {
        hosts.clear();
    }

    public void clearVms() {
        vms.clear();
    }
}
