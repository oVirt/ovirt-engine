package org.ovirt.engine.core.common;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;

/**
 * A test case for the {@link ActionUtils} class.
 */
@RunWith(Parameterized.class)
public class ActionUtilsTest {

    @Parameterized.Parameters
    public static Object[][] data() {
        VM upVm = new VM();
        upVm.setStatus(VMStatus.Up);

        VM downVm = new VM();
        downVm.setStatus(VMStatus.Down);

        VDS upVds = new VDS();
        upVds.setStatus(VDSStatus.Up);

        VDS downVds = new VDS();
        downVds.setStatus(VDSStatus.Down);

        StorageDomain upStorageDomain = new StorageDomain();
        upStorageDomain.setStatus(StorageDomainStatus.Active);

        StorageDomain downStorageDomain = new StorageDomain();
        downStorageDomain.setStatus(StorageDomainStatus.Inactive);

        return new Object[][] {
                { upVm, ActionType.MigrateVm, true },
                { downVm, ActionType.MigrateVm, false },
                { upVds, ActionType.RefreshHostCapabilities, true },
                { downVds, ActionType.RefreshHostCapabilities, false },
                { upStorageDomain, ActionType.DeactivateStorageDomainWithOvfUpdate, true },
                { downStorageDomain, ActionType.DetachStorageDomainFromPool, false },
                { new StoragePool(), ActionType.UpdateStoragePool, true }
        };
    }

    /** The object to test. */
    @Parameterized.Parameter(0)
    public BusinessEntityWithStatus<?, ?> toTest;

    /** The action to test */
    @Parameterized.Parameter(1)
    public ActionType action;

    /** The expected result. */
    @Parameterized.Parameter(2)
    public boolean result;

    @Test
    public void canExecute() {
        assertEquals(result,
                ActionUtils.canExecute(Collections.<BusinessEntityWithStatus<?, ?>> singletonList(toTest),
                        toTest.getClass(),
                        action));
    }

}
