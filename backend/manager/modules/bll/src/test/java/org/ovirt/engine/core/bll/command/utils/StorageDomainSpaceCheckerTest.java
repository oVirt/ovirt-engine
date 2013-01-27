package org.ovirt.engine.core.bll.command.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigRule;

public class StorageDomainSpaceCheckerTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Test
    public void notEnoughSpace() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 4;
        settings.diskSpaceUsed = 6;
        settings.spaceThresholdGB = 5;
        storage_domains domain = setupForSpaceTest(settings);
        assertFalse(StorageDomainSpaceChecker.isWithinThresholds(domain));
    }

    @Test
    public void equalSpaceThreshold() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 5;
        settings.diskSpaceUsed = 5;
        settings.spaceThresholdGB = 5;
        storage_domains domain = setupForSpaceTest(settings);
        assertFalse(StorageDomainSpaceChecker.isWithinThresholds(domain));
    }

    @Test
    public void zeroDiskSize() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 0;
        settings.diskSpaceUsed = 0;
        settings.spaceThresholdGB = 5;
        storage_domains domain = setupForSpaceTest(settings);
        assertFalse(StorageDomainSpaceChecker.isWithinThresholds(domain));
    }

    @Test
    public void GBThresholdLessThanZero() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 2;
        settings.diskSpaceUsed = 0;
        settings.spaceThresholdGB = -5;
        storage_domains domain = setupForSpaceTest(settings);
        assertTrue(StorageDomainSpaceChecker.isWithinThresholds(domain));
    }

    public storage_domains setupForSpaceTest(final SpaceTestSettings settings) {
        StorageDomainDynamic dynamic = new StorageDomainDynamic();
        dynamic.setavailable_disk_size(settings.diskSpaceFree);
        dynamic.setused_disk_size(settings.diskSpaceUsed);
        storage_domains domain = new storage_domains();
        domain.setStorageDynamicData(dynamic);
        mockConfig(settings.spaceThresholdGB);
        return domain;
    }

    private static void mockConfig(final int lowGB) {
        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, lowGB);
    }

    final class SpaceTestSettings {
        public int diskSpaceUsed;
        public int diskSpaceFree;
        public int spaceThresholdGB;
    }
}
