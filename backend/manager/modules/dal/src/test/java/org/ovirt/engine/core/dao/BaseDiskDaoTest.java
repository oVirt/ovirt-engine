package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.compat.Guid;

/**
 * Unit tests to validate {@link BaseDiskDao}.
 */
public class BaseDiskDaoTest extends BaseGenericDaoTestCase<Guid, BaseDisk, BaseDiskDao> {

    private static final Guid EXISTING_DISK_ID = new Guid("1b26a52b-b60f-44cb-9f46-3ef333b04a34");
    private static final int TOTAL_DISKS = 11;

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return TOTAL_DISKS;
    }

    @Override
    protected BaseDisk generateNewEntity() {
        BaseDisk d = new BaseDisk();
        d.setId(Guid.newGuid());
        d.setWipeAfterDelete(true);
        d.setPropagateErrors(PropagateErrors.Off);
        d.setDiskAlias("DiskName");
        d.setSgio(ScsiGenericIO.FILTERED);
        return d;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setDiskAlias("Other_Alias");
    }

    @Override
    protected Guid getExistingEntityId() {
        return EXISTING_DISK_ID;
    }

    @Test
    public void existsForExistingDisk() {
        assertTrue(dao.exists(EXISTING_DISK_ID));
    }

    @Test
    public void existsForNonExistingDisk() {
        assertFalse(dao.exists(Guid.Empty));
    }

    @Test
    public void getBaseDiskByAlias() {
        assertEquals(0, dao.getDisksByAlias("No such disk alias").size());
        assertTrue(dao.getDisksByAlias("New Disk Alias Name").size() > 1);
    }
}
