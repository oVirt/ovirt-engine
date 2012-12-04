package org.ovirt.engine.core.bll.command.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigRule;

public class StorageDomainSpaceCheckerTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Test
    public void enoughSpaceAndPct() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 6;
        settings.diskSpaceUsed = 4;
        settings.spaceThresholdGB = 5;
        settings.spaceThresholdPct = 10;
        storage_domains domain = setupForSpaceTest(settings);
        assertTrue(StorageDomainSpaceChecker.isBelowThresholds(domain));
    }

    @Test
    public void notEnoughSpace() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 4;
        settings.diskSpaceUsed = 6;
        settings.spaceThresholdGB = 5;
        settings.spaceThresholdPct = 10;
        storage_domains domain = setupForSpaceTest(settings);
        assertFalse(StorageDomainSpaceChecker.isBelowThresholds(domain));
    }

    @Test
    public void tooLowPct() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 6;
        settings.diskSpaceUsed = 4;
        settings.spaceThresholdGB = 5;
        settings.spaceThresholdPct = 70;
        storage_domains domain = setupForSpaceTest(settings);
        assertFalse(StorageDomainSpaceChecker.isBelowThresholds(domain));
    }

    @Test
    public void equalSpaceThreshold() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 5;
        settings.diskSpaceUsed = 5;
        settings.spaceThresholdGB = 5;
        settings.spaceThresholdPct = 10;
        storage_domains domain = setupForSpaceTest(settings);
        assertFalse(StorageDomainSpaceChecker.isBelowThresholds(domain));
    }

    @Test
    public void equalPctThreshold() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 5;
        settings.diskSpaceUsed = 5;
        settings.spaceThresholdGB = 1;
        settings.spaceThresholdPct = 50;
        storage_domains domain = setupForSpaceTest(settings);
        assertFalse(StorageDomainSpaceChecker.isBelowThresholds(domain));
    }

    @Test
    public void zeroDiskSize() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 0;
        settings.diskSpaceUsed = 0;
        settings.spaceThresholdGB = 5;
        settings.spaceThresholdPct = 10;
        storage_domains domain = setupForSpaceTest(settings);
        assertFalse(StorageDomainSpaceChecker.isBelowThresholds(domain));
    }

    @Test
    public void GBThresholdLessThanZero() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 2;
        settings.diskSpaceUsed = 0;
        settings.spaceThresholdGB = -5;
        settings.spaceThresholdPct = 10;
        storage_domains domain = setupForSpaceTest(settings);
        assertTrue(StorageDomainSpaceChecker.isBelowThresholds(domain));
    }

    @Test
    public void PctThresholdLessThanZero() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 10;
        settings.diskSpaceUsed = 0;
        settings.spaceThresholdGB = 5;
        settings.spaceThresholdPct = -10;
        storage_domains domain = setupForSpaceTest(settings);
        assertTrue(StorageDomainSpaceChecker.isBelowThresholds(domain));
    }

    @Test
    public void PctThresholdMoreThan100() {
        SpaceTestSettings settings = new SpaceTestSettings();
        settings.diskSpaceFree = 10;
        settings.diskSpaceUsed = 0;
        settings.spaceThresholdGB = 5;
        settings.spaceThresholdPct = 110;
        storage_domains domain = setupForSpaceTest(settings);
        assertFalse(StorageDomainSpaceChecker.isBelowThresholds(domain));
    }

    public storage_domains setupForSpaceTest(final SpaceTestSettings settings) {
        storage_domain_dynamic dynamic = new storage_domain_dynamic();
        dynamic.setavailable_disk_size(settings.diskSpaceFree);
        dynamic.setused_disk_size(settings.diskSpaceUsed);
        storage_domains domain = new storage_domains();
        domain.setStorageDynamicData(dynamic);
        mockConfig(settings.spaceThresholdGB, settings.spaceThresholdPct);
        return domain;
    }

    private static void mockConfig(final int lowGB, final int lowPct) {
        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, lowGB);
        mcr.mockConfigValue(ConfigValues.FreeSpaceLow, lowPct);
    }

    final class SpaceTestSettings {
        public int diskSpaceUsed;
        public int diskSpaceFree;
        public int spaceThresholdGB;
        public int spaceThresholdPct;
    }
}
