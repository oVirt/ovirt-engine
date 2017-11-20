package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.Strict;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(Strict.class)
public class AddVmFromTemplateCommandTest extends AddVmCommandTestBase<AddVmFromTemplateCommand<AddVmParameters>> {

    private static final int MAX_PCI_SLOTS = 26;

    @Rule
    public MockConfigRule mcr = new MockConfigRule(
        mockConfig(ConfigValues.MaxVmNameLength, 64),
        mockConfig(ConfigValues.ResumeBehaviorSupported, Version.v4_0, false),
        mockConfig(ConfigValues.SupportedClusterLevels, new HashSet<>(Collections.singletonList(new Version(3, 0))))
    );

    @Override
    protected AddVmFromTemplateCommand<AddVmParameters> createCommand() {
        return new AddVmFromTemplateCommand<>(new AddVmParameters(vm), null);
    }

    @Override
    public void setUp() {
        super.setUp();
        doNothing().when(cmd).initTemplateDisks();
        doReturn(true).when(cmd).checkNumberOfMonitors();
        doReturn(true).when(cmd).validateCustomProperties(any(), any());
        initCommandMethods();
        mockStorageDomainDaoGetAllForStoragePool();

        cmd.init();
    }

    protected void mockStorageDomainDaoGetAllForStoragePool() {
        when(sdDao.getAllForStoragePool(any())).thenReturn(Collections.singletonList(createStorageDomain()));
    }

    @Test
    public void validateSpaceAndThreshold() {
        mockGetAllSnapshots();
        assertTrue(cmd.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForClonedDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(any());
    }

    @Test
    public void validateSpaceNotEnough() throws Exception {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(any());
        mockGetAllSnapshots();
        assertFalse(cmd.validateSpaceRequirements());
        //The following is mocked to fail, should happen only once.
        verify(storageDomainValidator).hasSpaceForClonedDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(any());
    }

    @Test
    public void validateSpaceNotWithinThreshold() throws Exception {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).isDomainWithinThresholds();
        assertFalse(cmd.validateSpaceRequirements());
    }

    @Test
    public void create10GBVmWith11GbAvailableAndA5GbBuffer() throws Exception {
        doReturn(true).when(cmd).areParametersLegal(any());
        doReturn(Collections.emptyList()).when(cmd).getVmInterfaces();
        doReturn(Collections.emptyList()).when(cmd).getDiskVmElements();
        mockMaxPciSlots();
        mockGetAllSnapshots();

        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(any());

        ValidateTestUtils.runAndAssertValidateFailure
                (cmd, EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN);
    }

    private void mockMaxPciSlots() {
        doReturn(MAX_PCI_SLOTS).when(osRepository).getMaxPciDevices(anyInt(), any());
    }

    @Test
    public void diskImagesOnAnyApplicableDomainsValidDomains() {
        List<DiskImage> disks = generateDisksList(2);
        Map<Guid, Set<Guid>> validDomainsForDisk = createDiskValidDomainsMap(disks);
        assertThat(cmd.diskImagesOnAnyApplicableDomains(disks,
                validDomainsForDisk,
                Collections.singletonMap(STORAGE_DOMAIN_ID_1, createStorageDomain()),
                EnumSet.of(StorageDomainStatus.Active)),
                isValid());
    }

    @Test
    public void diskImagesOnAnyApplicableDomainsNoValidDomainsForAllDisks() {
        List<DiskImage> disks = generateDisksList(2);
        Map<Guid, Set<Guid>> validDomainsForDisk =
                disks.stream().collect(Collectors.toMap(DiskImage::getId, d -> Collections.emptySet()));
        assertThat(cmd.diskImagesOnAnyApplicableDomains(disks,
                validDomainsForDisk,
                Collections.singletonMap(STORAGE_DOMAIN_ID_1, createStorageDomain()),
                EnumSet.of(StorageDomainStatus.Active)),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NO_VALID_DOMAINS_STATUS_FOR_TEMPLATE_DISKS));
    }

    @Test
    public void diskImagesOnAnyApplicableDomainsNoValidDomainsForOneDisk() {
        DiskImage disk1 = createDiskImage();
        DiskImage disk2 = createDiskImage();
        List<DiskImage> disks = Arrays.asList(disk1, disk2);
        Map<Guid, Set<Guid>> validDomainsForDisk = createDiskValidDomainsMap(Collections.singletonList(disk1));
        validDomainsForDisk.put(disk2.getId(), Collections.emptySet());
        assertThat(cmd.diskImagesOnAnyApplicableDomains(disks,
                validDomainsForDisk,
                Collections.singletonMap(STORAGE_DOMAIN_ID_1, createStorageDomain()),
                EnumSet.of(StorageDomainStatus.Active)),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NO_VALID_DOMAINS_STATUS_FOR_TEMPLATE_DISKS));
    }

    private Map<Guid, Set<Guid>> createDiskValidDomainsMap(List<DiskImage> diskImages) {
        Map<Guid, Set<Guid>> toReturn = new HashMap<>();
        for (DiskImage diskImage : diskImages) {
            toReturn.put(diskImage.getId(), Collections.singleton(diskImage.getStorageIds().get(0)));
        }

        return toReturn;
    }
}
