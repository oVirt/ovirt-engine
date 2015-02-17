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

    public ReportedConfigurations add(ReportedConfigurationType type, String value, boolean inSync) {
        reportedConfigurationList.add(new ReportedConfiguration(type, value, inSync));
        return this;
    }

    public ReportedConfigurations add(ReportedConfigurationType type, Integer value, boolean inSync) {
        reportedConfigurationList.add(new ReportedConfiguration(type, value == null ? "null" : value.toString(), inSync));
        return this;
    }

    public ReportedConfigurations add(ReportedConfigurationType type, boolean value, boolean inSync) {
        reportedConfigurationList.add(new ReportedConfiguration(type, Boolean.toString(value), inSync));
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
