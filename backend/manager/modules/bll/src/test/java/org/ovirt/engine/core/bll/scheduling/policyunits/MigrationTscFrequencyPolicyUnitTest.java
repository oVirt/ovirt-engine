package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.plugins.MemberAccessor;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MigrationTscFrequencyPolicyUnitTest {

    private PendingResourceManager pendingResourceManager = new PendingResourceManager();
    private final MemberAccessor accessor = Plugins.getMemberAccessor();

    @InjectMocks
    public MigrationTscFrequencyPolicyUnit underTest = new MigrationTscFrequencyPolicyUnit(null, pendingResourceManager);
    private PerHostMessages messages;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        messages = new PerHostMessages();
        VdsDao dao = mock(VdsDao.class);
        VDS vds = mock(VDS.class);
        when(vds.getTscFrequency()).thenReturn("12345.6789");
        when(vds.getTscFrequencyIntegral()).thenReturn("12345");
        when(dao.get(any())).thenReturn(vds);
        setDao(dao);
    }

    @AfterEach
    void tearDown() throws NoSuchFieldException, IllegalAccessException {
        setDao(null);
    }

    @Test
    void testNoHPVm() {
        VM vm = mock(VM.class);
        when(vm.getVmType()).thenReturn(VmType.Server);
        List<VDS> hosts = Arrays.asList(mock(VDS.class), mock(VDS.class), mock(VDS.class));

        List<VDS> filtered = underTest.filter(null, hosts, vm, messages);

        assertEquals(hosts.size(), filtered.size());
    }

    @Test
    void testHostsVMNoTscFreq() throws NoSuchFieldException, IllegalAccessException {
        VM vm = mock(VM.class);
        when(vm.getVmType()).thenReturn(VmType.HighPerformance);
        when(vm.getUseTscFrequency()).thenReturn(true);
        VdsDao dao = mock(VdsDao.class);
        VDS vds = mock(VDS.class);
        when(vds.getTscFrequency()).thenReturn(null);
        when(dao.get(any())).thenReturn(vds);
        setDao(dao);
        List<VDS> hosts = Arrays.asList(mock(VDS.class), mock(VDS.class), mock(VDS.class));

        List<VDS> filtered = underTest.filter(null, hosts, vm, messages);

        assertEquals(hosts.size(), filtered.size());
    }

    @Test
    void testVmNotRunning() {
        VM vm = mock(VM.class);
        when(vm.getVmType()).thenReturn(VmType.HighPerformance);
        when(vm.getUseTscFrequency()).thenReturn(true);
        when(vm.getRunOnVds()).thenReturn(null);

        VDS host1 = mock(VDS.class);
        List<VDS> hosts = Arrays.asList(host1);
        when(host1.getId()).thenReturn(Guid.newGuid());
        when(host1.getTscFrequencyIntegral()).thenReturn(null);

        List<VDS> filtered = underTest.filter(null, hosts, vm, messages);

        assertEquals(1, filtered.size());
        assertEquals(0, messages.getMessages().size());
    }

    @Test
    void testHostsWithoutTscFrequency() {
        VM vm = mock(VM.class);
        when(vm.getVmType()).thenReturn(VmType.HighPerformance);
        when(vm.getUseTscFrequency()).thenReturn(true);
        when(vm.getRunOnVds()).thenReturn(Guid.newGuid());

        VDS host1 = mock(VDS.class);
        VDS host2 = mock(VDS.class);
        VDS host3 = mock(VDS.class);
        List<VDS> hosts = Arrays.asList(host1, host2, host3);
        when(host1.getId()).thenReturn(Guid.newGuid());
        when(host2.getId()).thenReturn(Guid.newGuid());
        when(host3.getId()).thenReturn(Guid.newGuid());
        when(host1.getTscFrequencyIntegral()).thenReturn(null);
        when(host2.getTscFrequencyIntegral()).thenReturn(null);
        when(host3.getTscFrequencyIntegral()).thenReturn(null);

        List<VDS> filtered = underTest.filter(null, hosts, vm, messages);

        assertEquals(0, filtered.size());
        assertEquals(3, messages.getMessages().size());
    }

    @Test
    void testHostsWithTscFrequency() {
        VM vm = mock(VM.class);
        when(vm.getVmType()).thenReturn(VmType.HighPerformance);
        when(vm.getUseTscFrequency()).thenReturn(true);

        VDS host1 = mock(VDS.class);
        VDS host2 = mock(VDS.class);
        VDS host3 = mock(VDS.class);
        List<VDS> hosts = Arrays.asList(host1, host2, host3);
        when(host1.getId()).thenReturn(Guid.newGuid());
        when(host2.getId()).thenReturn(Guid.newGuid());
        when(host3.getId()).thenReturn(Guid.newGuid());
        when(host1.getTscFrequencyIntegral()).thenReturn("12345");
        when(host2.getTscFrequencyIntegral()).thenReturn("12345");
        when(host3.getTscFrequencyIntegral()).thenReturn("12345");

        List<VDS> filtered = underTest.filter(null, hosts, vm, messages);

        assertEquals(3, filtered.size());
        assertEquals(0, messages.getMessages().size());
    }

    @Test
    void testHostsWithDifferentTscFrequency() {
        VM vm = mock(VM.class);
        when(vm.getVmType()).thenReturn(VmType.HighPerformance);
        when(vm.getUseTscFrequency()).thenReturn(true);
        when(vm.getRunOnVds()).thenReturn(Guid.newGuid());

        VDS host1 = mock(VDS.class);
        VDS host2 = mock(VDS.class);
        VDS host3 = mock(VDS.class);
        List<VDS> hosts = Arrays.asList(host1, host2, host3);
        when(host1.getId()).thenReturn(Guid.newGuid());
        when(host2.getId()).thenReturn(Guid.newGuid());
        when(host3.getId()).thenReturn(Guid.newGuid());
        when(host1.getTscFrequencyIntegral()).thenReturn("12345");
        when(host2.getTscFrequencyIntegral()).thenReturn("12350");
        when(host3.getTscFrequencyIntegral()).thenReturn("12346");

        List<VDS> filtered = underTest.filter(null, hosts, vm, messages);

        assertEquals(2, filtered.size());
        assertSame(host1, filtered.get(0));
        assertSame(host3, filtered.get(1));
        assertEquals(1, messages.getMessages().size());
    }

    private void setDao(VdsDao dao) throws NoSuchFieldException, IllegalAccessException {
        Field propField = MigrationTscFrequencyPolicyUnit.class.getDeclaredField("vdsDao");
        accessor.set(propField, underTest, dao);
    }
}
