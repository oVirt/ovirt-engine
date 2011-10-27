package org.ovirt.engine.core.bll.command.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Config.class })
public class StorageDomainSpaceCheckerTest {

    public StorageDomainSpaceCheckerTest() {
        mockStatic(Config.class);
    }

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

    private void mockConfig(final int lowGB, final int lowPct) {
        when(Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB)).thenReturn(lowGB);
        when(Config.<Integer> GetValue(ConfigValues.FreeSpaceLow)).thenReturn(lowPct);
    }

    final class SpaceTestSettings {
        public int diskSpaceUsed;
        public int diskSpaceFree;
        public int spaceThresholdGB;
        public int spaceThresholdPct;
    }
}
