package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;

public class RemoveStoragePoolCommandTest extends BaseCommandTest {

    private static RemoveStoragePoolCommand<StoragePoolParametersBase> createCommand(StoragePoolParametersBase param) {
        return new RemoveStoragePoolCommand<>(param, null);
    }

    @Test
    public void testEmptyDomainList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<>();
        assertTrue(cmd.validateDomainsInMaintenance(domainsList));
    }

    @Test
    public void testMaintenanceDomainInList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<>();
        StorageDomain tempStorageDomains = new StorageDomain();
        tempStorageDomains.setStatus(StorageDomainStatus.Maintenance);
        domainsList.add(tempStorageDomains);
        assertTrue(cmd.validateDomainsInMaintenance(domainsList));
    }

    /**
     * Test when Active domain is in the Data Center
     */
    @Test
    public void testActiveDomainInList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<>();
        StorageDomain tempStorageDomains = new StorageDomain();
        tempStorageDomains.setStatus(StorageDomainStatus.Active);
        domainsList.add(tempStorageDomains);
        assertFalse(cmd.validateDomainsInMaintenance(domainsList));
    }

    private void testBusyDomainInList(StorageDomainStatus status) {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<>();
        StorageDomain tempStorageDomains = new StorageDomain();
        tempStorageDomains.setStatus(status);
        domainsList.add(tempStorageDomains);
        assertFalse(cmd.validateDomainsInMaintenance(domainsList));
    }

    /**
     * Test removal with locked domain in the Data Center.
     */
    @Test
    public void testLockedDomainInList() {
        testBusyDomainInList(StorageDomainStatus.Locked);
    }

    /**
     * Test removal with moving to maintenance domain in the Data Center.
     */
    @Test
    public void testPreparingForMaintenanceDomainInList() {
        testBusyDomainInList(StorageDomainStatus.PreparingForMaintenance);
    }

    private void testBusyAndActiveDomainInList(StorageDomainStatus status) {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<>();

        // Add first locked storage
        StorageDomain tempStorageDomains = new StorageDomain();
        tempStorageDomains.setStatus(status);
        domainsList.add(tempStorageDomains);

        // Add second active storage
        tempStorageDomains.setStatus(StorageDomainStatus.Active);
        domainsList.add(tempStorageDomains);

        assertFalse(cmd.validateDomainsInMaintenance(domainsList));
    }

    /**
     * Test removal with locked and active domains in the Data Center.
     */
    @Test
    public void testLockedAndActiveDomainInList() {
        testBusyAndActiveDomainInList(StorageDomainStatus.Locked);
    }

    /**
     * Test removal with moving to maintenance and active domains in the Data Center.
     */
    @Test
    public void testPreparingForMaintenanceAndActiveDomainInList() {
        testBusyAndActiveDomainInList(StorageDomainStatus.PreparingForMaintenance);
    }

    /**
     * Test when there is in active domain.
     */
    @Test
    public void testInactiveDomainInList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<>();

        // Add first locked storage
        StorageDomain tempStorageDomains = new StorageDomain();
        tempStorageDomains.setStatus(StorageDomainStatus.Inactive);
        domainsList.add(tempStorageDomains);

        assertFalse(cmd.validateDomainsInMaintenance(domainsList));
    }
}
