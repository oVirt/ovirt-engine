package org.ovirt.engine.core.common;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;

/**
 * A test case for the {@link VdcActionUtils} class.
 */
@RunWith(Parameterized.class)
public class VdcActionUtilsTest {

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

        return new Object[][] {
                { upVm, VdcActionType.MigrateVm, true },
                { downVm, VdcActionType.MigrateVm, false },
                { upVds, VdcActionType.RefreshHostCapabilities, true },
                { downVds, VdcActionType.RefreshHostCapabilities, false },
                { upStorageDomain, VdcActionType.DeactivateStorageDomainWithOvfUpdate, true },
                { new StoragePool(), VdcActionType.UpdateStoragePool, true }
        };
    }

    /** The object to test. */
    @Parameterized.Parameter(0)
    public BusinessEntityWithStatus<?, ?> toTest;

    /** The action to test */
    @Parameterized.Parameter(1)
    public VdcActionType action;

    /** The expected result. */
    @Parameterized.Parameter(2)
    public boolean result;

    @Test
    public void canExecute() {
        assertEquals(result,
                VdcActionUtils.canExecute(Collections.<BusinessEntityWithStatus<?, ?>> singletonList(toTest),
                        toTest.getClass(),
                        action));
    }

}
