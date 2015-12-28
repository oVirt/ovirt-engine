package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;

public class CpuAndMemoryBalancingPolicyUnitTest extends AbstractPolicyUnitTest {
    protected  <T extends CpuAndMemoryBalancingPolicyUnit> T mockUnit(
            Class<T> unitType,
            Cluster cluster,
            Map<Guid, VDS> hosts,
            Map<Guid, VM> vms)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException, ParseException {
        T unit = spy(unitType.getConstructor(PolicyUnit.class, PendingResourceManager.class)
                .newInstance(null, null));

        // mock current time
        doReturn(TIME_FORMAT.parse("2015-01-01 12:00:00")).when(unit).getTime();

        // mock cluster Dao
        ClusterDao clusterDao = mock(ClusterDao.class);
        doReturn(clusterDao).when(unit).getClusterDao();
        doReturn(cluster).when(clusterDao).get(any(Guid.class));

        // mock host Dao
        VdsDao vdsDao = mock(VdsDao.class);
        doReturn(vdsDao).when(unit).getVdsDao();
        doReturn(new ArrayList(hosts.values())).when(vdsDao).getAllForCluster(any(Guid.class));

        // mock VM Dao
        VmDao vmDao = mock(VmDao.class);
        doReturn(vmDao).when(unit).getVmDao();
        for (Guid guid: hosts.keySet()) {
            doReturn(vmsOnAHost(vms.values(), guid)).when(vmDao).getAllRunningForVds(guid);
        }

        return unit;
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
}
