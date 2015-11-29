package org.ovirt.engine.core.bll.host;

import static org.junit.Assert.assertEquals;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigRule;

public class HostUpgradeManagerTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.PackageNamesForCheckUpdate, Arrays.asList("a", "b", "c")),
            mockConfig(ConfigValues.UserPackageNamesForCheckUpdate, Arrays.asList("b", "c", "d", "", null))
    );

    @Test
    public void testGetPackagesForCheckUpdate() throws Exception {
        Collection<String> expectedPackages = new HashSet<>(Arrays.asList("a", "b", "c", "d"));
        Collection<String> actualPackages = HostUpgradeManager.getPackagesForCheckUpdate();
        assertEquals(expectedPackages, actualPackages);
    }
}

