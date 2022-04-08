package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.mockito.Mock;
import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.bll.scheduling.utils.VdsCpuUnitPinningHelper;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.VmManager;

public class CpuAndMemoryBalancingPolicyUnitTest extends AbstractPolicyUnitTest {

    @Mock
    protected ClusterDao clusterDao;
    @Mock
    protected VmDao vmDao;
    @Mock
    protected ResourceManager resourceManager;

    @Mock
    private VdsCpuUnitPinningHelper vdsCpuUnitPinningHelper;

    @Mock
    private VdsManager vdsManager;

    protected Cluster cluster = new Cluster();

    protected void initMocks(CpuAndMemoryBalancingPolicyUnit unit,
            Map<Guid, VDS> hosts,
            Map<Guid, VM> vms) throws ParseException {

        doReturn(TIME_FORMAT.parse("2015-01-01 12:00:00")).when(unit).getTime();

        doAnswer(invocation -> {
            Collection<Guid> hostIds = invocation.getArgument(0);
            return vms.values().stream()
                    .filter(vm -> hostIds.contains(vm.getRunOnVds()))
                    .collect(Collectors.groupingBy(VM::getRunOnVds));
        }).when(vmDao).getAllRunningForMultipleVds(any(Collection.class));

        for (Map.Entry<Guid, VM> vm: vms.entrySet()) {
            doReturn(vm.getValue()).when(vmDao).get(vm.getKey());

            VmManager vmManagerMock = mock(VmManager.class);
            doReturn(vm.getValue().getStatisticsData()).when(vmManagerMock).getStatistics();
            doReturn(vmManagerMock).when(resourceManager).getVmManager(eq(vm.getKey()), anyBoolean());
        }

        when(resourceManager.getVdsManager(any())).thenReturn(vdsManager);
        when(vdsManager.getCpuTopology()).thenReturn(Collections.emptyList());
    }

    protected void assertBalanceResult(Guid expectedVm, Collection<Guid> expectedHosts, BalanceResult result) {
        assertTrue(result.isValid());
        assertEquals(expectedVm, result.getVmToMigrate());
        assertThat(result.getCandidateHosts()).containsOnlyElementsOf(expectedHosts);
    }
}
