package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;

public class CpuPinningPolicyUnitTest {

    @ClassRule
    public static MockConfigRule configRule =
            new MockConfigRule(
                    MockConfigRule.mockConfig(ConfigValues.MaxSchedulerWeight, Integer.MAX_VALUE)
            );

    private VDS hostWithCpus;

    private VDS hostWithoutCpus;

    private VM vm;

    private final CpuPinningPolicyUnit policyUnit = new CpuPinningPolicyUnit(null, null);

    private PerHostMessages perHostMessages;

    private Cluster cluster;

    @Before
    public void setUp() {
        hostWithCpus = new VDS();
        hostWithCpus.setId(Guid.newGuid());
        hostWithCpus.setOnlineCpus("0, 1, 2,3, 5");

        hostWithoutCpus = new VDS();
        hostWithoutCpus.setId(Guid.newGuid());
        hostWithoutCpus.setOnlineCpus(null);

        perHostMessages = new PerHostMessages();

        vm = new VM();
        vm.setId(Guid.newGuid());
        cluster = new Cluster();
    }

    @Test
    public void shouldHandleEmptyHostList() {
        final List<VDS> filteredHost = policyUnit.filter(cluster, new ArrayList<>(), vm, null, mock(PerHostMessages.class));
        assertThat(filteredHost, is(empty()));
        assertThat(messages(), is(empty()));
    }

    /**
     * No online CPU is equivalent to no information available
     */
    @Test
    public void shouldConsiderHostsWithNoOnlineCpus(){
        hostWithoutCpus.setOnlineCpus("");
        assertThat(filter(), hasItem(hostWithoutCpus));
        assertThat(messages(), is(empty()));
    }

    /**
     * No information about online cpus is available
     */
    @Test
    public void shouldConsiderHostWithNoCpuData(){
        hostWithoutCpus.setOnlineCpus(null);
        assertThat(filter(), hasItem(hostWithoutCpus));
        assertThat(messages(), is(empty()));
    }

    @Test
    public void shouldReturnAllHostWithoutPinningRequirements() {
        assertThat(filter(), containsInAnyOrder(hostWithCpus, hostWithoutCpus));
        assertThat(messages(), is(empty()));
    }

    @Test
    public void shouldDetectOfflineCpu() {
        vm.setCpuPinning("1#0_2#4");
        assertThat(filter(), not(hasItem(hostWithCpus)));
        assertThat(messages(), hasSize(2));
    }

    @Test
    public void shouldExcludeHostWithoutCpuInformation(){
        vm.setCpuPinning("1#0_2#4");
        assertThat(filter(), not(hasItem(hostWithoutCpus)));
    }

    @Test
    public void shouldHandleExcludedCpu(){
        vm.setCpuPinning("1#3-5,^4");
        assertThat(filter(), hasItem(hostWithCpus));
        assertThat(messages(), hasSize(1));
    }

    @Test
    public void shouldDetectNonExistentCpu() {
        vm.setCpuPinning("1#6");
        assertThat(filter(), not(hasItem(hostWithCpus)));
        assertThat(messages(), hasSize(2));
    }

    @Test
    public void shouldFindSuitableHost() {
        vm.setCpuPinning("1#0_2#3");
        assertThat(filter(), hasItem(hostWithCpus));
        assertThat(messages(), hasSize(1));
    }

    private List<VDS> filter() {
        return policyUnit.filter(cluster, Arrays.asList(hostWithCpus, hostWithoutCpus), vm, null, perHostMessages);
    }

    private Collection<List<String>> messages() {
        return perHostMessages.getMessages().values();
    }

}
