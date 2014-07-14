package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;

import org.junit.Test;
import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Agents;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostedEngine;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.SSH;
import org.ovirt.engine.api.model.User;
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
        inverse.setProtocol(to.getProtocol());
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
        assertEquals(host.getCpu().getTopology().getCores(), Integer.valueOf(2));
        assertEquals(host.getCpu().getTopology().getSockets(), Integer.valueOf(3));
        assertEquals(host.getCpu().getTopology().getThreads(), Integer.valueOf(2));
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
        assertEquals(host.getSummary().getTotal(), Integer.valueOf(2));
        assertEquals(host.getSummary().getActive(), Integer.valueOf(1));
        assertEquals(host.getSummary().getMigrating(), Integer.valueOf(1));
    }

    @Test
    public void testMemoryMapping() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setPhysicalMemMb(4000);
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getMemory());
        assertEquals(host.getMemory(), Long.valueOf(4194304000L));
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
        assertEquals(Long.valueOf(host.getOs().getVersion().getMajor()), Long.valueOf(17));
        assertEquals(Long.valueOf(host.getOs().getVersion().getMinor()), Long.valueOf(1));
    }

    @Test
    public void testVersion() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setVersion(new RpmVersion("vdsm-4.10.0-10.fc17", "vdsm-", true));
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getVersion());
        assertEquals(Long.valueOf(host.getVersion().getMajor()), Long.valueOf(4));
        assertEquals(Long.valueOf(host.getVersion().getMinor()), Long.valueOf(10));
        assertEquals(Long.valueOf(host.getVersion().getRevision()), Long.valueOf(0));
        assertEquals(Long.valueOf(host.getVersion().getBuild()), Long.valueOf(0));
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
        vds.setManagementIp(ip[0]);
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
            assertTrue(agent.isConcurrent());
            assertEquals(agent.getOptions().getOptions().get(0).getName(), "secure");
            assertEquals(agent.getOptions().getOptions().get(0).getValue(), "true");
            if (i > 0) {
                assertEquals(agent.isConcurrent(), true);
            }
            i++;
        }
    }

    @Test
    public void testUpdatePowerManagementHost() {
        String[] ip = { "1.1.1.111", "1.1.1.112"};
        PowerManagement powerMgmt = new PowerManagement();
        powerMgmt.setAddress(ip[1]);
        powerMgmt.setEnabled(true);
        powerMgmt.setPassword("passwd");
        powerMgmt.setType("apc");
        powerMgmt.setUsername("user");
        powerMgmt.setKdumpDetection(true);

        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setManagementIp(ip[0]);
        vdsStatic.setPmEnabled(true);
        vdsStatic.setPmPassword("passwd");
        vdsStatic.setPmPort(123);
        vdsStatic.setPmType("apc");
        vdsStatic.setPmUser("user");
        vdsStatic.setPmKdumpDetection(false);

        VdsStatic mappedVdsStatic = HostMapper.map(powerMgmt, vdsStatic);
        assertEquals(mappedVdsStatic.getManagementIp(), ip[1]);
        assertEquals(mappedVdsStatic.isPmEnabled(), true);
        assertEquals(mappedVdsStatic.getPmPassword(), "passwd");
        assertEquals(mappedVdsStatic.getPmType(), "apc");
        assertEquals(mappedVdsStatic.getPmUser(), "user");
        assertTrue(mappedVdsStatic.isPmKdumpDetection());
    }

    @Test
    public void testUpdateSshHost() {
        SSH sshConf = new SSH();
        sshConf.setPort(22);
        sshConf.setUser(new User());
        sshConf.getUser().setUserName("root");
        sshConf.setFingerprint("1234");

        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setSshUsername("root");
        vdsStatic.setSshPort(22);
        vdsStatic.setSshKeyFingerprint("1234");

        VdsStatic mappedVdsStatic = HostMapper.map(sshConf, vdsStatic);
        assertEquals(mappedVdsStatic.getSshPort(), 22);
        assertEquals(mappedVdsStatic.getSshKeyFingerprint(), "1234");
        assertEquals(mappedVdsStatic.getSshUsername(), "root");
    }

    @Test
    public void testUpdatePowerManagementHostFromAgents() {
        String[] ip = { "1.1.1.111", "1.1.1.112"};
        PowerManagement powerMgmt = new PowerManagement();
        powerMgmt.setAddress(ip[0]);
        powerMgmt.setEnabled(true);
        powerMgmt.setPassword("passwd");
        powerMgmt.setType("apc");
        powerMgmt.setUsername("user");
        powerMgmt.setKdumpDetection(true);

        Agent agent = new Agent();
        agent.setOrder(1);
        agent.setAddress(ip[1]);
        Agents agents = new Agents();
        agents.getAgents().add(agent);
        powerMgmt.setAgents(agents);

        VdsStatic vdsStatic = new VdsStatic();
        vdsStatic.setManagementIp(ip[0]);
        vdsStatic.setPmEnabled(true);
        vdsStatic.setPmPassword("passwd");
        vdsStatic.setPmPort(123);
        vdsStatic.setPmType("apc");
        vdsStatic.setPmUser("user");
        vdsStatic.setPmKdumpDetection(false);

        VdsStatic mappedVdsStatic = HostMapper.map(powerMgmt, vdsStatic);
        assertEquals(mappedVdsStatic.getManagementIp(), ip[1]);
        assertEquals(mappedVdsStatic.isPmEnabled(), true);
        assertEquals(mappedVdsStatic.getPmPassword(), "passwd");
        assertEquals(mappedVdsStatic.getPmType(), "apc");
        assertEquals(mappedVdsStatic.getPmUser(), "user");
        assertTrue(mappedVdsStatic.isPmKdumpDetection());
    }

    @Test
    public void testLibvirtVersion() {
        VDS vds = new VDS();
        vds.setId(Guid.Empty);
        vds.setLibvirtVersion(new RpmVersion("libvirt-0.9.10-21.el6_3.4", "libvirt-", true));
        Host host = HostMapper.map(vds, (Host) null);
        assertNotNull(host.getLibvirtVersion());
        assertEquals(Long.valueOf(host.getLibvirtVersion().getMajor()), Long.valueOf(0));
        assertEquals(Long.valueOf(host.getLibvirtVersion().getMinor()), Long.valueOf(9));
        assertEquals(Long.valueOf(host.getLibvirtVersion().getRevision()), Long.valueOf(0));
        assertEquals(Long.valueOf(host.getLibvirtVersion().getBuild()), Long.valueOf(10));
        assertEquals(host.getLibvirtVersion().getFullVersion(), "libvirt-0.9.10-21.el6_3.4");
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
        assertEquals(hostedEngine.isConfigured(), Boolean.TRUE);
        assertEquals(hostedEngine.isActive(), Boolean.FALSE);
        assertEquals(hostedEngine.getScore(), Integer.valueOf(123));
        assertEquals(hostedEngine.isGlobalMaintenance(), Boolean.TRUE);
        assertEquals(hostedEngine.isLocalMaintenance(), Boolean.FALSE);
    }
}
