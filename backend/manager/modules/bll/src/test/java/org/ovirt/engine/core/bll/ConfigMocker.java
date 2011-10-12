package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

/**
 * A class that provides for static mocking of Config through the use of Powermock. mockStatic can only be called once
 * per class and cannot be called from a static scope. Because of this, the mocking methods cannot be declared as static
 * and an instance of this class needs to be created. Consumers of this class need to use the Powermock PrepareForTest
 * annotation to prepare Config for static mocking.
 */
public class ConfigMocker {

    public ConfigMocker() {
        mockStatic(Config.class);
    }

    public void mockConfigLowDiskSpace(final int minimumFreeSpace) {
        when(Config.GetValue(ConfigValues.FreeSpaceCriticalLowInGB)).thenReturn(minimumFreeSpace);
    }

    public void mockLimitNumberOfNetworkInterfaces(Boolean retVal) {
        when(Config.GetValue(eq(ConfigValues.LimitNumberOfNetworkInterfaces), any(String.class))).thenReturn(retVal);
    }

    public void mockConfigLowDiskPct(final int pctOfSpaceRequired) {
        when(Config.GetValue(ConfigValues.FreeSpaceLow)).thenReturn(pctOfSpaceRequired);
    }

    public void mockOVirtISOsRepositoryPath(final String path) {
        when(Config.resolveOVirtISOsRepositoryPath()).thenReturn(path);
    }

    public void mockConfigOvirtIsoPrefix(final String oVirtIsoPrefix) {
        when(Config.GetValue(ConfigValues.OvirtIsoPrefix)).thenReturn(oVirtIsoPrefix);
    }

    public void mockConfigOvirtInitialSupportedIsoVersion(final String oVirtIsoVersion) {
        when(Config.GetValue(ConfigValues.OvirtInitialSupportedIsoVersion)).thenReturn(oVirtIsoVersion);
    }

}
