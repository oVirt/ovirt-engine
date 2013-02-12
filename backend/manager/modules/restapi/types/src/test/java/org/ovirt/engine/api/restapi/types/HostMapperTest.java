package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;

import org.junit.Test;
import org.ovirt.engine.api.model.Agent;
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
        inverse.setVdsName(to.getName());
        inverse.setHostName(to.getHostName());
        inverse.setVdsGroupId(to.getVdsGroupId());
        inverse.setPort(to.getPort());
        inverse.setVdsSpmPriority(to.getVdsSpmPriority());
        inverse.setConsoleAddress(to.getConsoleAddress());
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
        assertEquals(model.getDisplay().getAddress(), transform.getDisplay().getAddress());
    }

    @Test
    public void testCpuMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setCpuCores(6);
        vds.setCpuSockets(3);
        vds.setCpuThreads(12);
        vds.setCpuModel("some cpu model");
        vds.setCpuSpeedMh(5.5);
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getCpu());
        assertEquals(new Integer(host.getCpu().getTopology().getCores()), new Integer(2));
        assertEquals(new Integer(host.getCpu().getTopology().getSockets()), new Integer(3));
        assertEquals(new Integer(host.getCpu().getTopology().getThreads()), new Integer(2));
        assertEquals(host.getCpu().getName(), "some cpu model");
        assertEquals(host.getCpu().getSpeed(), new BigDecimal(5.5));
    }

    @Test
    public void testVmSummaryMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setVmCount(2);
        vds.setVmActive(1);
        vds.setVmMigrating(1);
        Host host = HostMapper.map(vds, (Host) null);
        assertEquals(host.getSummary().getTotal(), new Integer(2));
        assertEquals(host.getSummary().getActive(), new Integer(1));
        assertEquals(host.getSummary().getMigrating(), new Integer(1));
    }

    @Test
    public void testMemoryMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setPhysicalMemMb(4000);
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getMemory());
        assertEquals(new Long(host.getMemory()), new Long(4194304000L));
    }

    @Test
    public void testMaxSchedulingMemory() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setPhysicalMemMb(4000);
        vds.setMemCommited(1000);
        vds.setMaxVdsMemoryOverCommit(150);
        vds.setReservedMem(65);
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
        vds.setHostOs("Fedora - 17 - 1");
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

    @Test
    public void testPmProxyPreferences() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setPmProxyPreferences("cluster,dc");
        Host host = HostMapper.map(vds, (Host) null);
        assertEquals(host.getPowerManagement().getPmProxies().getPmProxy().size(), 2);
        assertTrue(host.getPowerManagement().getPmProxies().getPmProxy().get(0).getType().equalsIgnoreCase("cluster"));
        assertTrue(host.getPowerManagement().getPmProxies().getPmProxy().get(1).getType().equalsIgnoreCase("dc"));
    }

    @Test
    public void testPowerManagementAgents() {
        String[] ip = { "1.1.1.111", "1.1.1.112" };
        int agents = 0;
        int i = 0;
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setpm_enabled(true);
        vds.setManagmentIp(ip[0]);
        vds.setPmType("apc");
        vds.setPmUser("user");
        vds.setPmOptions("secure=true");
        vds.setPmSecondaryConcurrent(true);
        vds.setPmSecondaryIp(ip[1]);
        vds.setPmSecondaryType("apc");
        vds.setPmSecondaryUser("user");
        vds.setPmSecondaryOptions("secure=true");
        Host host = HostMapper.map(vds, (Host) null);
        agents = host.getPowerManagement().getAgents().getAgents().size();
        assertEquals(agents, 2);
        for (Agent agent : host.getPowerManagement().getAgents().getAgents()) {
            assertEquals(host.getPowerManagement().isEnabled(), true);
            assertEquals(agent.getAddress(), ip[i]);
            assertEquals(agent.getType(), "apc");
            assertEquals(agent.getUsername(), "user");
            assertEquals(agent.getOptions().getOptions().get(0).getName(), "secure");
            assertEquals(agent.getOptions().getOptions().get(0).getValue(), "true");
            if (i > 0) {
                assertEquals(agent.isConcurrent(), true);
            }
            i++;
        }
    }

    @Test
    public void testLibvirtVersion() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setLibvirtVersion(new RpmVersion("libvirt-0.9.10-21.el6_3.4", "libvirt-", true));
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getLibvirtVersion());
        assertEquals(new Long(host.getLibvirtVersion().getMajor()), new Long(0));
        assertEquals(new Long(host.getLibvirtVersion().getMinor()), new Long(9));
        assertEquals(new Long(host.getLibvirtVersion().getRevision()), new Long(0));
        assertEquals(new Long(host.getLibvirtVersion().getBuild()), new Long(10));
        assertEquals(host.getLibvirtVersion().getFullVersion(), "libvirt-0.9.10-21.el6_3.4");
    }
}
