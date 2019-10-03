package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;

@ExtendWith(MockitoExtension.class)
class VmLeasesReadyFilterPolicyUnitTest {

    @Mock
    ResourceManager resourceManager;

    @Mock
    VdsManager host1VdsManager;

    @Mock
    VdsManager host2VdsManager;

    @InjectMocks
    VmLeasesReadyFilterPolicyUnit unit = new VmLeasesReadyFilterPolicyUnit(null, null);

    private Cluster cluster;
    private SchedulingContext context;
    private VM vm;
    private VDS host1;
    private VDS host2;
    private List<VDS> hosts;

    @BeforeEach
    public void setUp() {
        cluster = new Cluster();
        cluster.setId(Guid.newGuid());

        context = new SchedulingContext(cluster, Collections.emptyMap());

        vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterId(cluster.getId());

        host1 = new VDS();
        host1.setId(Guid.newGuid());
        host1.setClusterId(cluster.getId());

        host2 = new VDS();
        host2.setId(Guid.newGuid());
        host2.setClusterId(cluster.getId());

        hosts = Arrays.asList(host1, host2);
    }

    @Test
    public void testVmWithoutLease() {
        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).containsAll(hosts);
    }

    @Test
    public void testVmWithLeaseAndHostsInitialized() {
        Guid leaseStorageDomainId = Guid.newGuid();
        vm.setLeaseStorageDomainId(leaseStorageDomainId);

        host1.setDomains(prepareDomainsData(true, leaseStorageDomainId, 1));
        host2.setDomains(prepareDomainsData(true, leaseStorageDomainId, 1));

        setUpMocks();

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).containsAll(hosts);
    }

    @Test
    public void testVmWithLeaseAndHostsUnInitialized() {
        Guid leaseStorageDomainId = Guid.newGuid();
        vm.setLeaseStorageDomainId(leaseStorageDomainId);

        host1.setDomains(prepareDomainsData(false, leaseStorageDomainId, 1));
        host2.setDomains(prepareDomainsData(false, leaseStorageDomainId, 1));

        setUpMocks();

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .doesNotContainAnyElementsOf(hosts);
    }

    @Test
    public void testVmWithLeaseAndHostsNoData() {
        Guid leaseStorageDomainId = Guid.newGuid();
        vm.setLeaseStorageDomainId(leaseStorageDomainId);

        host1.setDomains(null);
        host2.setDomains(null);

        setUpMocks();

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .doesNotContainAnyElementsOf(hosts);
    }

    @Test
    public void testVmWithLeaseAndHostsInitializedManyDomains() {
        Guid leaseStorageDomainId = Guid.newGuid();
        vm.setLeaseStorageDomainId(leaseStorageDomainId);

        host1.setDomains(prepareDomainsData(true, leaseStorageDomainId, 3));
        host2.setDomains(prepareDomainsData(true, leaseStorageDomainId, 3));

        setUpMocks();

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .containsAll(hosts);
    }

    @Test
    public void testVmWithLeaseAndHostsUnInitializedManyDomains() {
        Guid leaseStorageDomainId = Guid.newGuid();
        vm.setLeaseStorageDomainId(leaseStorageDomainId);

        host1.setDomains(prepareDomainsData(false, leaseStorageDomainId, 3));
        host2.setDomains(prepareDomainsData(false, leaseStorageDomainId, 3));

        setUpMocks();

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .doesNotContainAnyElementsOf(hosts);
    }

    @Test
    public void testVmWithLeaseSanlockInitializedOnOneHost() {
        Guid leaseStorageDomainId = Guid.newGuid();
        vm.setLeaseStorageDomainId(leaseStorageDomainId);

        host1.setDomains(prepareDomainsData(true, leaseStorageDomainId, 1));
        host2.setDomains(prepareDomainsData(false, leaseStorageDomainId, 1));

        setUpMocks();

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .contains(host1)
                .doesNotContain(host2);
    }

    private ArrayList<VDSDomainsData> prepareDomainsData(boolean isSanlockInitialized,
            Guid leaseStorageDomainId,
            int numberOfStorageDoamins) {
        ArrayList<VDSDomainsData> domainsData = new ArrayList<>();
        IntStream.range(0, numberOfStorageDoamins).forEach(index -> {
            VDSDomainsData vdsDomainsData = new VDSDomainsData();
            vdsDomainsData.setDomainId(index == 0 ? leaseStorageDomainId : Guid.newGuid());
            vdsDomainsData.setAcquired(isSanlockInitialized);
            domainsData.add(vdsDomainsData);
        });
        return domainsData;
    }

    private void setUpMocks() {
        doReturn(host1VdsManager).when(resourceManager).getVdsManager(host1.getId());
        doReturn(host2VdsManager).when(resourceManager).getVdsManager(host2.getId());
        doReturn(host1.getDomains()).when(host1VdsManager).getDomains();
        doReturn(host2.getDomains()).when(host2VdsManager).getDomains();
    }
}
