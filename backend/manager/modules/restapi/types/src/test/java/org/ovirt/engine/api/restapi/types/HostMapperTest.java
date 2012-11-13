package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;

import org.junit.Test;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;

public class HostMapperTest extends AbstractInvertibleMappingTest<Host, VdsStatic, VDS> {

    public HostMapperTest() {
        super(Host.class, VdsStatic.class, VDS.class);
    }

    @Override
    protected Host postPopulate(Host from) {
        while (from.getPort() == 0) {
            from.setPort(MappingTestHelper.rand(65535));
        }
        from.getStorageManager().setPriority(3);
        return from;
    }

    @Override
    protected VDS getInverse(VdsStatic to) {
        VDS inverse = new VDS();
        inverse.setId(to.getId());
        inverse.setvds_name(to.getvds_name());
        inverse.sethost_name(to.gethost_name());
        inverse.setvds_group_id(to.getvds_group_id());
        inverse.setport(to.getport());
        inverse.setVdsSpmPriority(to.getVdsSpmPriority());
        return inverse;
    }

    @Override
    protected void verify(Host model, Host transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getCluster());
        assertEquals(model.getCluster().getId(), transform.getCluster().getId());
        assertEquals(model.getAddress(), transform.getAddress());
        assertEquals(model.getPort(), transform.getPort());
        assertEquals(model.getStorageManager().getPriority(), transform.getStorageManager().getPriority());
    }

    @Test
    public void testCpuMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setcpu_cores(6);
        vds.setcpu_sockets(3);
        vds.setcpu_model("some cpu model");
        vds.setcpu_speed_mh(5.5);
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getCpu());
        assertEquals(new Integer(host.getCpu().getTopology().getCores()), new Integer(2));
        assertEquals(new Integer(host.getCpu().getTopology().getSockets()), new Integer(3));
        assertEquals(host.getCpu().getName(), "some cpu model");
        assertEquals(host.getCpu().getSpeed(), new BigDecimal(5.5));
    }

    @Test
    public void testVmSummaryMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setvm_count(2);
        vds.setvm_active(1);
        vds.setvm_migrating(1);
        Host host = HostMapper.map(vds, (Host) null);
        assertEquals(host.getSummary().getTotal(), new Integer(2));
        assertEquals(host.getSummary().getActive(), new Integer(1));
        assertEquals(host.getSummary().getMigrating(), new Integer(1));
    }

    @Test
    public void testMemoryMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setphysical_mem_mb(4000);
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getMemory());
        assertEquals(new Long(host.getMemory()), new Long(4194304000L));
    }

    @Test
    public void testMaxSchedulingMemory() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setphysical_mem_mb(4000);
        vds.setmem_commited(1000);
        vds.setmax_vds_memory_over_commit(150);
        vds.setreserved_mem(65);
        vds.calculateFreeVirtualMemory();
        Host host = HostMapper.map(vds, (Host) null);
        long vdsValue = (long) vds.getMaxSchedulingMemory();
        Long hostValue = host.getMaxSchedulingMemory() / HostMapper.BYTES_IN_MEGABYTE;

        assertTrue(vdsValue > 0 && hostValue > 0 && vdsValue == hostValue);

    }

    @Test
    public void testHostOs() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.sethost_os("Fedora - 17 - 1");
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getOs());
        assertTrue(host.getOs().isSetVersion());
        assertEquals(host.getOs().getType(), "Fedora");
        assertEquals(host.getOs().getVersion().getFullVersion(), "17 - 1");
        assertEquals(new Long(host.getOs().getVersion().getMajor()), new Long(17));
        assertEquals(new Long(host.getOs().getVersion().getMinor()), new Long(1));
    }

    @Test
    public void testVersion() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setVersion(new RpmVersion("vdsm-4.10.0-10.fc17", "vdsm-", true));
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getVersion());
        assertEquals(new Long(host.getVersion().getMajor()), new Long(4));
        assertEquals(new Long(host.getVersion().getMinor()), new Long(10));
        assertEquals(new Long(host.getVersion().getRevision()), new Long(0));
        assertEquals(new Long(host.getVersion().getBuild()), new Long(0));
        assertEquals(host.getVersion().getFullVersion(), "vdsm-4.10.0-10.fc17");
    }
}
