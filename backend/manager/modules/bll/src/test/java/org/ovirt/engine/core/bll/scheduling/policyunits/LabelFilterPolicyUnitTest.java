package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LabelDao;

@ExtendWith(MockitoExtension.class)
public class LabelFilterPolicyUnitTest {
    @Mock
    LabelDao labelDao;

    @InjectMocks
    LabelFilterPolicyUnit unit = new LabelFilterPolicyUnit(null, null);

    private Cluster cluster;
    private VM vm;
    private VDS host1;
    private VDS host2;
    private List<VDS> hosts;

    @BeforeEach
    public void setUp() {
        cluster = new Cluster();
        cluster.setId(Guid.newGuid());

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
    public void testSimple() {
        Label red = new LabelBuilder()
                .entities(vm, host1)
                .build();

        List<Label> labels = Collections.singletonList(red);
        doReturn(labels).when(labelDao).getAllByEntityIds(any());

        assertThat(unit.filter(cluster, hosts, vm, new HashMap<>(), new PerHostMessages()))
                .contains(host1)
                .doesNotContain(host2);
    }

    @Test
    public void testEmpty() {
        Label red = new LabelBuilder()
                .entities(vm, host1)
                .build();

        Label blue = new LabelBuilder()
                .entities(vm, host2)
                .build();

        List<Label> labels = Arrays.asList(red, blue);
        doReturn(labels).when(labelDao).getAllByEntityIds(any());

        assertThat(unit.filter(cluster, hosts, vm, new HashMap<>(), new PerHostMessages()))
                .isEmpty();
    }

    @Test
    public void testHostExtra() {
        Label red = new LabelBuilder()
                .entities(vm, host1)
                .build();

        Label blue = new LabelBuilder()
                .entities(host1, host2)
                .build();

        List<Label> labels = Arrays.asList(red, blue);
        doReturn(labels).when(labelDao).getAllByEntityIds(any());

        assertThat(unit.filter(cluster, hosts, vm, new HashMap<>(), new PerHostMessages()))
                .contains(host1)
                .doesNotContain(host2);
    }
}
