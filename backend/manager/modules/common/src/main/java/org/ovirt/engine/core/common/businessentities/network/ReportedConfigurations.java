package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Reported configuration related to sole network.
 */
public class ReportedConfigurations implements Serializable {
    private static final long serialVersionUID = -6086888024266749566L;

    private  boolean networkInSync;

    /*
     * all reported configurations, with flag whether each configuration is in sync or not.
     */
    private List<ReportedConfiguration> reportedConfigurationList = new ArrayList<>();

    public <T> ReportedConfigurations add(ReportedConfigurationType type, T actual, T expected, boolean inSync) {
        String actualValue = actual == null ? null : actual.toString();
        String expectedValue = expected == null ? null : expected.toString();
        reportedConfigurationList.add(new ReportedConfiguration(type, actualValue, expectedValue, inSync));
        return this;
    }

    public List<ReportedConfiguration> getReportedConfigurationList() {
        return reportedConfigurationList;
    }


    /**
     * all network configuration is in sync with host.
     */
    public boolean isNetworkInSync() {
        return networkInSync;
    }

    public void setNetworkInSync(boolean networkInSync) {
        this.networkInSync = networkInSync;
    }

}
