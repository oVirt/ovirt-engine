package org.ovirt.engine.ui.frontend.server.dashboard;

public class GlobalUtilizationCpuSummary extends GlobalUtilizationResourceSummary {
    public double getTotal() {
        return 100; // Always return 100% for total.
    }

}
