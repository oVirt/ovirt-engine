package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostedEngine;
import org.ovirt.engine.api.model.PmProxies;
import org.ovirt.engine.api.model.PmProxyType;
import org.ovirt.engine.api.model.Ssh;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
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
        from.getSpm().setPriority(3);
        from.getPowerManagement().setPmProxies(new PmProxies());
        return from;
    }

    @Override
    protected VDS getInverse(VdsStatic to) {
        VDS inverse = new VDS();
        inverse.setId(to.getId());
        inverse.setVdsName(to.getName());
        inverse.setHostName(to.getHostName());
        inverse.setClusterId(to.getClusterId());
        inverse.setPort(to.getPort());
        inverse.setSshKeyFingerprint(to.getSshKeyFingerprint());
        inverse.setHostProviderId(to.getHostProviderId());
        inverse.setSshPort(to.getSshPort());
        inverse.setSshUsername(to.getSshUsername());
        inverse.setVdsSpmPriority(to.getVdsSpmPriority());
        inverse.setConsoleAddress(to.getConsoleAddress());
        inverse.setComment(to.getComment());
        return inverse;
    }

    @Override
    protected void verify(Host model, Host transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getComment(), transform.getComment());
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getCluster());
        assertEquals(model.getCluster().getId(), transform.getCluster().getId());
        assertEquals(model.getAddress(), transform.getAddress());
        assertEquals(model.getPort(), transform.getPort());
        assertEquals(model.getSpm().getPriority(), transform.getSpm().getPriority());
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
        assertEquals(Integer.valueOf(2), host.getCpu().getTopology().getCores());
        assertEquals(Integer.valueOf(3), host.getCpu().getTopology().getSockets());
        assertEquals(Integer.valueOf(2), host.getCpu().getTopology().getThreads());
        assertEquals("some cpu model", host.getCpu().getName());
        assertEquals(new BigDecimal(5.5), host.getCpu().getSpeed());
    }

    @Test
    public void testVmSummaryMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setVmCount(2);
        vds.setVmActive(1);
        vds.setVmMigrating(1);
        Host host = HostMapper.map(vds, (Host) null);
        assertEquals(Integer.valueOf(2), host.getSummary().getTotal());
        assertEquals(Integer.valueOf(1), host.getSummary().getActive());
        assertEquals(Integer.valueOf(1), host.getSummary().getMigrating());
    }

    @Test
    public void testMemoryMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setPhysicalMemMb(4000);
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getMemory());
        assertEquals(Long.valueOf(4194304000L), host.getMemory());
    }

    @Test
    public void testMaxSchedulingMemory() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setPhysicalMemMb(4000);
        vds.setMemCommited(1000);
        vds.setMaxVdsMemoryOverCommit(150);
        vds.setReservedMem(65);
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
        assertEquals("Fedora", host.getOs().getType());
        assertEquals("17 - 1", host.getOs().getVersion().getFullVersion());
        assertEquals(Long.valueOf(17), Long.valueOf(host.getOs().getVersion().getMajor()));
        assertEquals(Long.valueOf(1), Long.valueOf(host.getOs().getVersion().getMinor()));
    }

    @Test
    public void testVersion() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setVersion(new RpmVersion("vdsm-4.10.0-10.fc17", "vdsm-", true));
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getVersion());
        assertEquals(Long.valueOf(4), Long.valueOf(host.getVersion().getMajor()));
        assertEquals(Long.valueOf(10), Long.valueOf(host.getVersion().getMinor()));
        assertEquals(Long.valueOf(0), Long.valueOf(host.getVersion().getRevision()));
        assertEquals(Long.valueOf(0), Long.valueOf(host.getVersion().getBuild()));
        assertEquals("vdsm-4.10.0-10.fc17", host.getVersion().getFullVersion());
    }

    @Test
    public void testPmProxyPreferences() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setFenceProxySources(Arrays.asList(FenceProxySourceType.CLUSTER, FenceProxySourceType.DC));
        Host host = HostMapper.map(vds, (Host) null);
        assertEquals(2, host.getPowerManagement().getPmProxies().getPmProxies().size());
        assertEquals(PmProxyType.CLUSTER, host.getPowerManagement().getPmProxies().getPmProxies().get(0).getType());
        assertEquals(PmProxyType.DC, host.getPowerManagement().getPmProxies().getPmProxies().get(1).getType());
    }

    @Test
    public void testUpdateSshHost() {
        Ssh sshConf = new Ssh();
        sshConf.setPort(22);
        sshConf.setUser(new User());
        sshConf.getUser().setUserName("root");
        sshConf.setFingerprint("1234");

        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setSshUsername("root");
        vdsStatic.setSshPort(22);
        vdsStatic.setSshKeyFingerprint("1234");

        VdsStatic mappedVdsStatic = HostMapper.map(sshConf, vdsStatic);
        assertEquals(22, mappedVdsStatic.getSshPort());
        assertEquals("1234", mappedVdsStatic.getSshKeyFingerprint());
        assertEquals("root", mappedVdsStatic.getSshUsername());
    }

    @Test
    public void testLibvirtVersion() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setLibvirtVersion(new RpmVersion("libvirt-0.9.10-21.el6_3.4", "libvirt-", true));
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getLibvirtVersion());
        assertEquals(Long.valueOf(0), Long.valueOf(host.getLibvirtVersion().getMajor()));
        assertEquals(Long.valueOf(9), Long.valueOf(host.getLibvirtVersion().getMinor()));
        assertEquals(Long.valueOf(0), Long.valueOf(host.getLibvirtVersion().getRevision()));
        assertEquals(Long.valueOf(10), Long.valueOf(host.getLibvirtVersion().getBuild()));
        assertEquals("libvirt-0.9.10-21.el6_3.4", host.getLibvirtVersion().getFullVersion());
    }

    @Test
    public void testHostedEngineMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setHighlyAvailableIsConfigured(true);
        vds.setHighlyAvailableIsActive(false);
        vds.setHighlyAvailableScore(123);
        vds.setHighlyAvailableGlobalMaintenance(true);
        vds.setHighlyAvailableLocalMaintenance(false);
        HostedEngine hostedEngine = HostMapper.map(vds, (HostedEngine) null);
        assertNotNull(hostedEngine);
        assertEquals(Boolean.TRUE, hostedEngine.isConfigured());
        assertEquals(Boolean.FALSE, hostedEngine.isActive());
        assertEquals(Integer.valueOf(123), hostedEngine.getScore());
        assertEquals(Boolean.TRUE, hostedEngine.isGlobalMaintenance());
        assertEquals(Boolean.FALSE, hostedEngine.isLocalMaintenance());
    }

    @Test
    public void testDevicePassthroughMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setHostDevicePassthroughEnabled(true);
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host);
        assertNotNull(host.getDevicePassthrough());
        assertTrue(host.getDevicePassthrough().isEnabled());
    }
}
