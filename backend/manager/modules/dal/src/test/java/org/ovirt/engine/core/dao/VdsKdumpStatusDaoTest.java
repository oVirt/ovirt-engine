package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.KdumpFlowStatus;
import org.ovirt.engine.core.common.businessentities.VdsKdumpStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@link VdsKdumpStatusDao} tests
 */
public class VdsKdumpStatusDaoTest extends BaseDaoTestCase {
    /**
     * Test getting status for existing VDS
     */
    @Test
    public void getForExistingVds() {
        VdsKdumpStatus expected = new VdsKdumpStatus();
        expected.setVdsId(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7"));
        expected.setStatus(KdumpFlowStatus.DUMPING);
        expected.setAddress("[\"192.168.122.18\", 2222]");

        VdsKdumpStatus found = dbFacade.getVdsKdumpStatusDao().get(expected.getVdsId());

        assertNotNull(found);
        assertEquals(expected, found);
    }

    /**
     * Test getting status for non existent VDS
     */
    @Test
    public void getForNonExistentVds() {
        VdsKdumpStatus found = dbFacade.getVdsKdumpStatusDao().get(
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
        newVdsKdumpStatus.setVdsId(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e8"));
        newVdsKdumpStatus.setStatus(KdumpFlowStatus.DUMPING);
        newVdsKdumpStatus.setAddress("[\"192.168.122.16\", 1111]");

        dbFacade.getVdsKdumpStatusDao().update(newVdsKdumpStatus);

        VdsKdumpStatus found = dbFacade.getVdsKdumpStatusDao().get(newVdsKdumpStatus.getVdsId());

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

        dbFacade.getVdsKdumpStatusDao().updateForIp("10.35.110.10", newVdsKdumpStatus);
        newVdsKdumpStatus.setVdsId(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6"));

        VdsKdumpStatus found = dbFacade.getVdsKdumpStatusDao().get(newVdsKdumpStatus.getVdsId());

        assertNotNull(found);
        assertEquals(newVdsKdumpStatus, found);
    }

    /**
     * Test updating existing status record
     */
    @Test
    public void updateStatusForVds() {
        VdsKdumpStatus existing = new VdsKdumpStatus();
        existing.setVdsId(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7"));
        existing.setStatus(KdumpFlowStatus.FINISHED);
        existing.setAddress("[\"192.168.122.25\", 4444]");

        dbFacade.getVdsKdumpStatusDao().update(existing);

        VdsKdumpStatus found = dbFacade.getVdsKdumpStatusDao().get(existing.getVdsId());

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

        dbFacade.getVdsKdumpStatusDao().updateForIp("10.35.110.10", existing);

        existing.setVdsId(new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6"));
        VdsKdumpStatus found = dbFacade.getVdsKdumpStatusDao().get(existing.getVdsId());

        assertNotNull(found);
        assertEquals(existing, found);
    }

    /**
     * Test removing finished status for existing VDS
     */
    @Test
    public void removeForExistingVds() {
        Guid vdsId = new Guid("11111111-1111-1111-1111-111111111112");

        dbFacade.getVdsKdumpStatusDao().remove(vdsId);
        VdsKdumpStatus found = dbFacade.getVdsKdumpStatusDao().get(vdsId);

        assertNull(found);
    }

    /**
     * Test removing finished status for non existent VDS
     */
    @Test
    public void removeForNonExistentVds() {
        dbFacade.getVdsKdumpStatusDao().remove(
                new Guid("11111111-2222-3333-4444-555555555555")
        );
    }

    /**
     * Test getting all VDSs with unfinished kdump status
     */
    @Test
    public void getAllUnfinishedKdumpStatus() {
        List<VdsKdumpStatus> result = dbFacade.getVdsKdumpStatusDao().getAllUnfinishedVdsKdumpStatus();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
