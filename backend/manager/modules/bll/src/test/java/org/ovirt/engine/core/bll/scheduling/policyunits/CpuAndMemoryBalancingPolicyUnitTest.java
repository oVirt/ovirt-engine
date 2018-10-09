package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;

public class CpuAndMemoryBalancingPolicyUnitTest extends AbstractPolicyUnitTest {

    @Mock
    protected ClusterDao clusterDao;
    @Mock
    protected VmDao vmDao;
    @Mock
    protected VmStatisticsDao vmStatisticsDao;

    protected Cluster cluster = new Cluster();

    protected void initMocks(CpuAndMemoryBalancingPolicyUnit unit,
            Map<Guid, VDS> hosts,
            Map<Guid, VM> vms) throws ParseException {

        doReturn(TIME_FORMAT.parse("2015-01-01 12:00:00")).when(unit).getTime();

        for (Guid guid: hosts.keySet()) {
            doReturn(vmsOnAHost(vms.values(), guid)).when(vmDao).getAllRunningForVds(guid);
        }
        for (Map.Entry<Guid, VM> vm: vms.entrySet()) {
            doReturn(vm.getValue()).when(vmDao).get(vm.getKey());
        }

        for (Map.Entry<Guid, VM> entry : vms.entrySet()) {
            doReturn(entry.getValue().getStatisticsData()).when(vmStatisticsDao).get(entry.getKey());
        }
    }

    private List<VM> vmsOnAHost(Collection<VM> vms, Guid host) {
        List<VM> result = new ArrayList<>();
        for (VM vm: vms) {
            if (vm.getRunOnVds().equals(host)) {
                result.add(vm);
            }
        }
        return result;
    }

    protected void assertBalanceResult(Guid expectedVm, Collection<Guid> expectedHosts, BalanceResult result) {
        assertTrue(result.isValid());
        assertEquals(expectedVm, result.getVmToMigrate());
        assertThat(result.getCandidateHosts()).containsOnlyElementsOf(expectedHosts);
    }
}
