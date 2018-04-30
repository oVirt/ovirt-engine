package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.KdumpFlowStatus;
import org.ovirt.engine.core.common.businessentities.VdsKdumpStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@link VdsKdumpStatusDao} tests
 */
public class VdsKdumpStatusDaoTest extends BaseDaoTestCase<VdsKdumpStatusDao> {
    /**
     * Test getting status for existing VDS
     */
    @Test
    public void getForExistingVds() {
        VdsKdumpStatus expected = new VdsKdumpStatus();
        expected.setVdsId(FixturesTool.HOST_ID);
        expected.setStatus(KdumpFlowStatus.DUMPING);
        expected.setAddress("[\"192.168.122.18\", 2222]");

        VdsKdumpStatus found = dao.get(expected.getVdsId());

        assertNotNull(found);
        assertEquals(expected, found);
    }

    /**
     * Test getting status for non existent VDS
     */
    @Test
    public void getForNonExistentVds() {
        VdsKdumpStatus found = dao.get(
                new Guid("11111111-2222-3333-4444-555555555555")
        );

        assertNull(found);
    }

    /**
     * Test creating new status record
     */
    @Test
    public void createStatusForVds() {
        VdsKdumpStatus newVdsKdumpStatus = new VdsKdumpStatus();
        newVdsKdumpStatus.setVdsId(FixturesTool.HOST_WITH_NO_VFS_CONFIGS_ID);
        newVdsKdumpStatus.setStatus(KdumpFlowStatus.DUMPING);
        newVdsKdumpStatus.setAddress("[\"192.168.122.16\", 1111]");

        dao.update(newVdsKdumpStatus);

        VdsKdumpStatus found = dao.get(newVdsKdumpStatus.getVdsId());

        assertNotNull(found);
        assertEquals(newVdsKdumpStatus, found);
    }

    /**
     * Test creating new status record
     */
    @Test
    public void createStatusForIp() {
        VdsKdumpStatus newVdsKdumpStatus = new VdsKdumpStatus();
        newVdsKdumpStatus.setStatus(KdumpFlowStatus.DUMPING);
        newVdsKdumpStatus.setAddress("[\"10.35.110.10\", 1111]");

        dao.updateForIp("10.35.110.10", newVdsKdumpStatus);
        newVdsKdumpStatus.setVdsId(FixturesTool.VDS_RHEL6_NFS_SPM);

        VdsKdumpStatus found = dao.get(newVdsKdumpStatus.getVdsId());

        assertNotNull(found);
        assertEquals(newVdsKdumpStatus, found);
    }

    /**
     * Test updating existing status record
     */
    @Test
    public void updateStatusForVds() {
        VdsKdumpStatus existing = new VdsKdumpStatus();
        existing.setVdsId(FixturesTool.HOST_ID);
        existing.setStatus(KdumpFlowStatus.FINISHED);
        existing.setAddress("[\"192.168.122.25\", 4444]");

        dao.update(existing);

        VdsKdumpStatus found = dao.get(existing.getVdsId());

        assertNotNull(found);
        assertEquals(existing, found);
    }

    /**
     * Test updating existing status record for IP address
     */
    @Test
    public void updateStatusForIp() {
        VdsKdumpStatus existing = new VdsKdumpStatus();
        existing.setStatus(KdumpFlowStatus.FINISHED);
        existing.setAddress("[\"10.35.110.10\", 4444]");

        dao.updateForIp("10.35.110.10", existing);

        existing.setVdsId(FixturesTool.VDS_RHEL6_NFS_SPM);
        VdsKdumpStatus found = dao.get(existing.getVdsId());

        assertNotNull(found);
        assertEquals(existing, found);
    }

    /**
     * Test removing finished status for existing VDS
     */
    @Test
    public void removeForExistingVds() {
        Guid vdsId = new Guid("11111111-1111-1111-1111-111111111112");

        dao.remove(vdsId);
        VdsKdumpStatus found = dao.get(vdsId);

        assertNull(found);
    }

    /**
     * Test removing finished status for non existent VDS
     */
    @Test
    public void removeForNonExistentVds() {
        dao.remove(
                new Guid("11111111-2222-3333-4444-555555555555")
        );
    }

    /**
     * Test getting all VDSs with unfinished kdump status
     */
    @Test
    public void getAllUnfinishedKdumpStatus() {
        List<VdsKdumpStatus> result = dao.getAllUnfinishedVdsKdumpStatus();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
