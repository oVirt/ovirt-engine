package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.compat.Guid;

public class DiskDaoTest extends BaseReadDaoTestCase<Guid, Disk, DiskDao> {

    private static final int TOTAL_DISK_IMAGES = 3;

    @Override
    protected Guid getExistingEntityId() {
        return new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a34");
    }

    @Override
    protected DiskDao prepareDao() {
        return dbFacade.getDiskDao();
    }

    @Override
    protected Guid generateNonExistingId() {
        return new Guid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_DISK_IMAGES + DiskLunMapDaoTest.TOTAL_DISK_LUN_MAPS;
    }

}
