package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

/** A test class for the {@link MultipleStorageDomainsValidator} class. */
@RunWith(MockitoJUnitRunner.class)
public class MultipleStorageDomainsValidatorTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.FreeSpaceCriticalLowInGB, 10));

    @Mock
    private StorageDomainDAO dao;

    private Guid spId;

    private Guid sdId1;
    private Guid sdId2;

    private StorageDomain domain1;
    private StorageDomain domain2;

    private MultipleStorageDomainsValidator validator;

    @Before
    public void setUp() {
        spId = Guid.newGuid();

        sdId1 = Guid.newGuid();
        sdId2 = Guid.newGuid();

        domain1 = new StorageDomain();
        domain1.setId(sdId1);

        domain2 = new StorageDomain();
        domain2.setId(sdId2);

        when(dao.getForStoragePool(sdId1, spId)).thenReturn(domain1);
        when(dao.getForStoragePool(sdId2, spId)).thenReturn(domain2);

        validator = spy(new MultipleStorageDomainsValidator(spId, Arrays.asList(sdId1, sdId2)));
        doReturn(dao).when(validator).getStorageDomainDAO();
    }

    @Test
    public void testAllDomainsExistAndActiveAllActive() {
        domain1.setStatus(StorageDomainStatus.Active);
        domain2.setStatus(StorageDomainStatus.Active);
        assertTrue("Both domains should be active", validator.allDomainsExistAndActive().isValid());
    }

    @Test
    public void testAllDomainsExistAndActiveOneInactive() {
        domain1.setStatus(StorageDomainStatus.Active);
        domain2.setStatus(StorageDomainStatus.Inactive);
        ValidationResult result = validator.allDomainsExistAndActive();
        assertFalse("One domain should not be active", result.isValid());
        assertEquals("Wrong validation error",
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                result.getMessage());
    }

    @Test
    public void testAllDomainsWithinThresholdAllOk() {
        domain1.getStorageDynamicData().setAvailableDiskSize(15);
        domain2.getStorageDynamicData().setAvailableDiskSize(15);
        assertTrue("Both domains should be within space threshold", validator.allDomainsWithinThresholds().isValid());
    }

    @Test
    public void testAllDomainsWithinThresholdsOneLacking() {
        domain1.getStorageDynamicData().setAvailableDiskSize(15);
        domain2.getStorageDynamicData().setAvailableDiskSize(7);
        ValidationResult result = validator.allDomainsWithinThresholds();
        assertFalse("domain2 should not be within thresholds", result.isValid());
        assertEquals("Wrong validation error",
                VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                result.getMessage());
    }
}
