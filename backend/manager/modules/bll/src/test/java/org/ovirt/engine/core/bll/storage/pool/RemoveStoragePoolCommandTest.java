package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;

public class RemoveStoragePoolCommandTest extends BaseCommandTest {

    private static RemoveStoragePoolCommand<StoragePoolParametersBase> createCommand(StoragePoolParametersBase param) {
        return new RemoveStoragePoolCommand<>(param, null);
    }

    @Test
    public void emptyDomainList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        List<StorageDomain> domainsList = new ArrayList<>();
        assertTrue(cmd.validateDomainsInMaintenance(domainsList));
    }

    @Test
    public void onlyMaintenanceDomainInList() {
        StoragePoolParametersBase param = new StoragePoolParametersBase();
        RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
        assertTrue(cmd.validateDomainsInMaintenance(createSingleDomainList(StorageDomainStatus.Maintenance)));
    }

    @Test
    public void allButMaintenance() {
        Arrays.stream(StorageDomainStatus.values())
                .filter(s -> s != StorageDomainStatus.Maintenance)
                .forEach(s -> {
                    StoragePoolParametersBase param = new StoragePoolParametersBase();
                    RemoveStoragePoolCommand<StoragePoolParametersBase> cmd = createCommand(param);
                    List<StorageDomain> domainsList = createSingleDomainList(s);
                    domainsList.add(createDomain(StorageDomainStatus.Maintenance));
                    assertFalse(cmd.validateDomainsInMaintenance(domainsList));
                });
    }

    private List<StorageDomain> createSingleDomainList(StorageDomainStatus status) {
        List<StorageDomain> domains = new LinkedList<>();
        domains.add(createDomain(status));
        return domains;
    }

    private StorageDomain createDomain(StorageDomainStatus status) {
        StorageDomain domain = new StorageDomain();
        domain.setStatus(status);
        return domain;
    }
}
