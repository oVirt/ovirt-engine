package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VMStatus;

public class SystemStatisticsDaoImplTest extends BaseDaoTestCase {
    private static final int NUM_OF_VM_IN_FIXTURES_WITH_STATUS_MIGRATING_FROM = 2;
    private static final int NUM_OF_USERS_IN_FIXTURES = 4;

    @Inject
    private SystemStatisticsDao underTest;

    @Test
    public void testGetSystemStatisticsValueWithSpecifiedStatus() {
        int numOfVmWithStatusMigratingFrom =
                underTest.getSystemStatisticsValue("VM", Integer.toString(VMStatus.MigratingFrom.getValue()));
        assertEquals(NUM_OF_VM_IN_FIXTURES_WITH_STATUS_MIGRATING_FROM, numOfVmWithStatusMigratingFrom);
    }

    @Test
    public void testGetSystemStatisticsValueWithoutSpecifiedStatus() {
        int numOfUsers = underTest.getSystemStatisticsValue("User", "");
        assertEquals(NUM_OF_USERS_IN_FIXTURES, numOfUsers);
    }
}
