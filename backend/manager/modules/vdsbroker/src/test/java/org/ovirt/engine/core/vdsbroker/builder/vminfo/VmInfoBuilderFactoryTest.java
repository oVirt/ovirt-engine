package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@ExtendWith(MockitoExtension.class)
public class VmInfoBuilderFactoryTest {

    @Mock
    private  ClusterDao clusterDao;
    @Mock
    private  NetworkClusterDao networkClusterDao;
    @Mock
    private  NetworkDao networkDao;
    @Mock
    private  VdsNumaNodeDao vdsNumaNodeDao;
    @Mock
    private  VmDeviceDao vmDeviceDao;
    @Mock
    private  VmNumaNodeDao vmNumaNodeDao;
    @Mock
    private VmInfoBuildUtils vmInfoBuildUtils;
    @Mock
    private OsRepository osRepository;

    @InjectMocks
    private VmInfoBuilderFactory underTest;

    @Test
    public void testCreateVmInfoBuilder() {
        final VmInfoBuilder actual = underTest.createVmInfoBuilder(new VM(), Guid.newGuid(), new HashMap());
        assertThat(actual, instanceOf(VmInfoBuilderImpl.class));
    }

}
