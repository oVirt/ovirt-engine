package org.ovirt.engine.core.bll.host;

import static org.junit.Assert.assertEquals;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

public class HostUpgradeManagerTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.PackageNamesForCheckUpdate, "4.0", Arrays.asList("a", "b", "c")),
            mockConfig(ConfigValues.UserPackageNamesForCheckUpdate, Arrays.asList("b", "c", "d", "", null)),
            mockConfig(ConfigValues.OvirtNodePackageNamesForCheckUpdate, Collections.singletonList("e"))
    );

    @Test
    public void testGetPackagesForCheckUpdate() throws Exception {
        Collection<String> expectedPackages = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        Collection<String> actualPackages = HostUpgradeManager.getPackagesForCheckUpdate(VDSType.VDS, Version.v4_0);
        assertEquals(expectedPackages, actualPackages);
    }

    @Test
    public void testGetOvirtNodePackagesForCheckUpdate() throws Exception {
        Collection<String> expectedPackages = new HashSet<>(Collections.singletonList("e"));
        Collection<String> actualPackages = HostUpgradeManager.getPackagesForCheckUpdate(VDSType.oVirtNode, Version.v4_0);
        assertEquals(expectedPackages, actualPackages);
    }
}

