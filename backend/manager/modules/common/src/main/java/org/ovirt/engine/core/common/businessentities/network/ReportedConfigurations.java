package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reported configuration related to sole network.
 */
public class ReportedConfigurations implements Serializable {
    private static final long serialVersionUID = -6086888024266749566L;

    /*
     * all reported configurations, with flag whether each configuration is in sync or not.
     */
    private List<ReportedConfiguration> reportedConfigurationList = new ArrayList<>();

    public <T> ReportedConfigurations add(ReportedConfigurationType type, T actual, T expected, boolean inSync) {
        String actualValue = Objects.toString(actual, null);
        String expectedValue = Objects.toString(expected, null);
        reportedConfigurationList.add(new ReportedConfiguration(type, actualValue, expectedValue, inSync));
        return this;
    }

    /***
     * The function adds a reported configuration property. Please note that
     * {@link org.ovirt.engine.core.common.businessentities.network.ReportedConfiguration#inSync} will be calculate
     * using {@link java.util.Objects#equals(Object, Object)}.
     */
    public <T> ReportedConfigurations add(ReportedConfigurationType type, T actual, T expected) {
        final boolean inSync = Objects.equals(actual, expected);
        return add(type, actual, expected, inSync);
    }

    public List<ReportedConfiguration> getReportedConfigurationList() {
        return reportedConfigurationList;
    }

    /**
     * all network configuration is in sync with host.
     */
    public boolean isNetworkInSync() {
        for (ReportedConfiguration reportedConfig : reportedConfigurationList) {
            if (!reportedConfig.isInSync()) {
                return false;
            }
        }

        return true;
    }
}
