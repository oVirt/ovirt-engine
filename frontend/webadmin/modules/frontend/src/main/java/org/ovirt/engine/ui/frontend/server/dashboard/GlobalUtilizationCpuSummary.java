package org.ovirt.engine.ui.frontend.server.dashboard;

public class GlobalUtilizationCpuSummary extends GlobalUtilizationResourceSummary {
    public double getTotal() {
        double result = super.getTotal();
        if (result != 0) {
            result = 100; // Always return 100% for total.
        }
        return result;
    }

}
