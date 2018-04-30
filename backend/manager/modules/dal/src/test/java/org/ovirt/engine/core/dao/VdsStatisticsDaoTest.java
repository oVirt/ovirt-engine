package org.ovirt.engine.core.dao;

import org.junit.jupiter.api.Disabled;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.compat.Guid;

public class VdsStatisticsDaoTest extends BaseGenericDaoTestCase<Guid, VdsStatistics, VdsStatisticsDao> {
    @Override
    protected VdsStatistics generateNewEntity() {
        VdsStatistics newStatistics = new VdsStatistics();
        newStatistics.setId(FixturesTool.VDS_JUST_STATIC_ID);
        return newStatistics;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setBootTime(System.currentTimeMillis());
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.VDS_GLUSTER_SERVER2;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 5;
    }

    @Disabled
    @Override
    public void testGetAll() {
        // Not Supported
    }
}
