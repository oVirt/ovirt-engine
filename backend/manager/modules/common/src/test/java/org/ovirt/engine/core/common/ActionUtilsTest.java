package org.ovirt.engine.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
public class ActionUtilsTest {

    public static Stream<Arguments> canExecute() {
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

        return Stream.of(
                Arguments.of(upVm, ActionType.MigrateVm, true),
                Arguments.of(downVm, ActionType.MigrateVm, false),
                Arguments.of(upVds, ActionType.RefreshHostCapabilities, true),
                Arguments.of(downVds, ActionType.RefreshHostCapabilities, false),
                Arguments.of(upStorageDomain, ActionType.DeactivateStorageDomainWithOvfUpdate, true),
                Arguments.of(downStorageDomain, ActionType.DetachStorageDomainFromPool, false),
                Arguments.of(new StoragePool(), ActionType.UpdateStoragePool, true)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void canExecute(BusinessEntityWithStatus<?, ?> toTest, ActionType action, boolean result) {
        assertEquals(result,
                ActionUtils.canExecute(Collections.<BusinessEntityWithStatus<?, ?>> singletonList(toTest),
                        toTest.getClass(),
                        action));
    }

}
