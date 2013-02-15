package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;

public class RemoveStoragePoolCommandTest {

    private static RemoveStoragePoolCommand<StoragePoolParametersBase> createCommand(StoragePoolParametersBase param) {
        return new RemoveStoragePoolCommand<StoragePoolParametersBase>(param);
    }

    @Test
    public void testEmptyDomainList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<StorageDomain>();
        List<StorageDomain> listReturned = cmd.getActiveOrLockedDomainList(domainsList);
        assertTrue(listReturned.isEmpty());
    }

    /**
     * Test when Active domain is in the Data Center
     */
    @Test
    public void testActiveDomainInList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<StorageDomain>();
        StorageDomain tempStorageDomains = new StorageDomain();
        tempStorageDomains.setStatus(StorageDomainStatus.Active);
        domainsList.add(tempStorageDomains);
        List<StorageDomain> listReturned = cmd.getActiveOrLockedDomainList(domainsList);
        assertTrue(!listReturned.isEmpty());
    }

    /**
     * Test when there is locked domain is in the Data Center.
     */
    @Test
    public void testLockedDomainInList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<StorageDomain>();
        StorageDomain tempStorageDomains = new StorageDomain();
        tempStorageDomains.setStatus(StorageDomainStatus.Locked);
        domainsList.add(tempStorageDomains);
        List<StorageDomain> listReturned = cmd.getActiveOrLockedDomainList(domainsList);
        assertTrue(!listReturned.isEmpty());
    }

    /**
     * Test when there is locked domain and active is in the Data Center.
     */
    @Test
    public void testLockedAndActiveDomainInList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<StorageDomain>();

        // Add first locked storage
        StorageDomain tempStorageDomains = new StorageDomain();
        tempStorageDomains.setStatus(StorageDomainStatus.Locked);
        domainsList.add(tempStorageDomains);

        // Add second active storage
        tempStorageDomains.setStatus(StorageDomainStatus.Active);
        domainsList.add(tempStorageDomains);

        List<StorageDomain> listReturned = cmd.getActiveOrLockedDomainList(domainsList);
        assertTrue(listReturned.size() == 2);
    }

    /**
     * Test when there is in active domain.
     */
    @Test
    public void testInActiveDomainInList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<StorageDomain>();

        // Add first locked storage
        StorageDomain tempStorageDomains = new StorageDomain();
        tempStorageDomains.setStatus(StorageDomainStatus.InActive);
        domainsList.add(tempStorageDomains);

        List<StorageDomain> listReturned = cmd.getActiveOrLockedDomainList(domainsList);
        assertTrue(listReturned.isEmpty());
    }
}
