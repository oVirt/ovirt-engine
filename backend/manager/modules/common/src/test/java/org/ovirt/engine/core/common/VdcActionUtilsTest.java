package org.ovirt.engine.core.common;

import static org.junit.Assert.assertEquals;
import static org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
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

    @Parameters
    public static Collection<Object[]> data() {
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

        return Arrays.asList(new Object[][] {
                { upVm, VdcActionType.MigrateVm, true },
                { downVm, VdcActionType.MigrateVm, false },
                { upVds, VdcActionType.RefreshHostCapabilities, true },
                { downVds, VdcActionType.RefreshHostCapabilities, false },
                { upStorageDomain, VdcActionType.DeactivateStorageDomainWithOvfUpdate, true },
                { downStorageDomain, VdcActionType.DeactivateStorageDomainWithOvfUpdate, false },
                { new StoragePool(), VdcActionType.UpdateStoragePool, true }
        });
    }

    public VdcActionUtilsTest(BusinessEntityWithStatus<?, ?> toTest, VdcActionType action, boolean result) {
        this.toTest = toTest;
        this.action = action;
        this.result = result;
    }

    /** The object to test. */
    private BusinessEntityWithStatus<?, ?> toTest;

    /** The action to test */
    private VdcActionType action;

    /**
     * The expected result.
     */
    private boolean result;

    @Test
    public void canExecute() {
        assertEquals(result,
                VdcActionUtils.canExecute(Collections.<BusinessEntityWithStatus<?, ?>> singletonList(toTest),
                        toTest.getClass(),
                        action));
    }

}
