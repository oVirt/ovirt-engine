package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddVmFromTemplateCommandTest extends AddVmCommandTestBase<AddVmFromTemplateCommand<AddVmParameters>> {

    private static final int MAX_PCI_SLOTS = 26;
    private List<StorageDomain> storageDomains;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.MaxVmNameLength, 64),
                MockConfigDescriptor.of(ConfigValues.SupportedClusterLevels,
                        new HashSet<>(Collections.singletonList(new Version(3, 0))))
        );
    }

    @Override
    protected AddVmFromTemplateCommand<AddVmParameters> createCommand() {
        return new AddVmFromTemplateCommand<>(new AddVmParameters(vm), null);
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        doNothing().when(cmd).initTemplateDisks();
        doReturn(true).when(cmd).checkNumberOfMonitors();
        doReturn(true).when(cmd).validateCustomProperties(any());
        initCommandMethods();
        mockStorageDomainDaoGetAllForStoragePool();

        cmd.init();
    }

    protected void mockStorageDomainDaoGetAllForStoragePool() {
        storageDomains =
                Arrays.asList(createStorageDomain(STORAGE_DOMAIN_ID_1), createStorageDomain(STORAGE_DOMAIN_ID_2));
        when(sdDao.getAllForStoragePool(any())).thenReturn(storageDomains);
    }

    @Test
    public void validateSpaceAndThreshold() {
        mockGetAllSnapshots();
        assertTrue(cmd.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForClonedDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(any());
    }

    @Test
    public void validateSpaceNotEnough() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(any());
        mockGetAllSnapshots();
        assertFalse(cmd.validateSpaceRequirements());
        //The following is mocked to fail, should happen only once.
        verify(storageDomainValidator).hasSpaceForClonedDisks(any());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(any());
    }

    @Test
    public void validateSpaceNotWithinThreshold() {
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).isDomainWithinThresholds();
        assertFalse(cmd.validateSpaceRequirements());
    }

    @Test
    public void create10GBVmWith11GbAvailableAndA5GbBuffer() {
        doReturn(true).when(cmd).areParametersLegal();
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
        assertTrue(cmd.verifySourceDomains());
    }

    @Test
    public void diskImagesOnAnyApplicableDomainsNoValidDomainsForAllDisks() {
        storageDomains.forEach(sd -> sd.setStatus(StorageDomainStatus.Inactive));
        assertFalse(cmd.verifySourceDomains());
    }

    @Test
    public void diskImagesOnAnyApplicableDomainsNoValidDomainsForOneDisk() {
        storageDomains.get(0).setStatus(StorageDomainStatus.Maintenance);
        assertFalse(cmd.verifySourceDomains());
    }
}
