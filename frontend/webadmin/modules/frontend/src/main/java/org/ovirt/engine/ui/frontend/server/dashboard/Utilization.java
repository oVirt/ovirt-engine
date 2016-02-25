package org.ovirt.engine.ui.frontend.server.dashboard;

public interface Utilization {
    void addVm(UtilizedEntity vm);
    void addResource(UtilizedEntity resource);
}
