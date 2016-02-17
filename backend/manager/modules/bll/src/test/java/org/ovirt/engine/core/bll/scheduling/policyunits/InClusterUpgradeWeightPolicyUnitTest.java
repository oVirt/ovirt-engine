package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit.BAD_WEIGHT;
import static org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit.BEST_WEIGHT;
import static org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit.BETTER_WEIGHT;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class InClusterUpgradeWeightPolicyUnitTest extends AbstractPolicyUnitTest {

    @ClassRule
    public static MockConfigRule configRule = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.MaxSchedulerWeight, Integer.MAX_VALUE)
    );

    @Mock
    private VdsDynamicDao vdsDynamicDao;

    private InClusterUpgradeWeightPolicyUnit inClusterUpgradeWeightPolicyUnit;

    private VM runningVm;

    private VDS newEnoughHost;
    private VDS tooOldHost;
    private VDS currentHost;

    @Before
    public void setUp() {
        injectorRule.bind(VdsDynamicDao.class, vdsDynamicDao);
        inClusterUpgradeWeightPolicyUnit =  new InClusterUpgradeWeightPolicyUnit(null, null);
        newEnoughHost = newHost("RHEL - 7.2 - 1.el7");
        tooOldHost = newHost("RHEL - 5.0 - 1.el5");
        currentHost = newHost("RHEL - 6.1 - 1.el6");

        runningVm = new VM();
        runningVm.setRunOnVds(currentHost.getId());
        when(vdsDynamicDao.get(eq(currentHost.getId()))).thenReturn(currentHost.getDynamicData());
    }

    @Test
    public void shouldFilterTooOldHost() {
        final VDS additionalNewHost = newHost("RHEL - 7.2 - 1.el7");
        assertThat(filter(runningVm, tooOldHost, newEnoughHost, additionalNewHost), hasItems(
                        weight(tooOldHost, BAD_WEIGHT),
                        weight(newEnoughHost, BEST_WEIGHT),
                        weight(additionalNewHost, BEST_WEIGHT))
        );
        assertThat(filter(runningVm, tooOldHost, newEnoughHost, additionalNewHost), hasSize(3));
    }

    @Test
    public void shouldKeepCurrentOsVersionHosts() {
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasItems(
                        weight(newEnoughHost, BEST_WEIGHT),
                        weight(currentHost, BETTER_WEIGHT))
        );
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasSize(2));
    }

    @Test
    public void shouldKeepSameMajorWithOlderMinor() {
        newEnoughHost.setHostOs("RHEL - 6.0 - 1.el6");
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasItems(
                        weight(newEnoughHost, BETTER_WEIGHT),
                        weight(currentHost, BETTER_WEIGHT))
        );
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasSize(2));
    }

    @Test
    public void shouldKeepSameMajorWithNewerMinor() {
        newEnoughHost.setHostOs("RHEL - 6.3 - 1.el6");
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasItems(
                        weight(newEnoughHost, BETTER_WEIGHT),
                        weight(currentHost, BETTER_WEIGHT))
        );
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasSize(2));
    }

    @Test
    public void shouldWeightNewestHostsBetterOnVmStarts() {
        final VM newVM = new VM();
        assertThat(filter(newVM, newEnoughHost, tooOldHost), hasItems(
                        weight(tooOldHost, BAD_WEIGHT),
                        weight(newEnoughHost, BEST_WEIGHT))
        );
        assertThat(filter(newVM, tooOldHost, newEnoughHost), hasSize(2));
    }

    @Test
    public void shouldWeightNothingOnVmStartWithDifferenOsFamilies() {
        VDS fedoraHost = newHost("Fedora - 23 - 1.fc23");
        final VM newVM = new VM();
        assertThat(filter(newVM, tooOldHost, newEnoughHost, fedoraHost), hasItems(
                weight(tooOldHost, BEST_WEIGHT),
                weight(newEnoughHost, BEST_WEIGHT),
                weight(fedoraHost, BEST_WEIGHT))
        );
        assertThat(filter(newVM, tooOldHost, newEnoughHost, fedoraHost), hasSize(3));
    }

    @Test
    public void shouldWeightNothingWithAllHostsInvalidOnVmStart() {
        VDS invalidHost = newHost("RHEL - - 1.fc23");
        final VM newVM = new VM();
        assertThat(filter(newVM, invalidHost, invalidHost), hasItems(
                weight(invalidHost, BEST_WEIGHT),
                weight(invalidHost, BEST_WEIGHT))
        );
        assertThat(filter(newVM, invalidHost, invalidHost), hasSize(2));
    }

    @Test
    public void shouldWeightOnlyValidHostsOnVmStart() {
        VDS invalidHost = newHost("RHEL - - 1.fc23");
        final VM newVM = new VM();
        assertThat(filter(newVM, invalidHost, newEnoughHost), hasItems(
                weight(invalidHost, BAD_WEIGHT),
                weight(newEnoughHost, BEST_WEIGHT))
        );
        assertThat(filter(newVM, invalidHost, newEnoughHost), hasSize(2));
    }

    @Test
    public void shouldDetectMissingCurrentOsInformation() {
        currentHost.setHostOs(null);
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasItems(
                        weight(tooOldHost, BEST_WEIGHT),
                        weight(newEnoughHost, BEST_WEIGHT))
        );
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasSize(2));
    }

    @Test
    public void shouldDetectMissingOsOnTargetHosts() {
        newEnoughHost.setHostOs(null);
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasItems(
                        weight(tooOldHost, BAD_WEIGHT),
                        weight(newEnoughHost, BAD_WEIGHT))
        );
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasSize(2));
    }

    @Test
    public void shouldDetectInvalidOsOnTargetHosts() {
        newEnoughHost.setHostOs("7.3");
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasItems(
                        weight(tooOldHost, BAD_WEIGHT),
                        weight(newEnoughHost, BAD_WEIGHT))
        );
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasSize(2));
    }

    @Test
    public void shouldDetectDifferentOS() {
        newEnoughHost.setHostOs("Fedora - 23 - 1.f23");
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasItems(
                        weight(tooOldHost, BAD_WEIGHT),
                        weight(newEnoughHost, BAD_WEIGHT))
        );
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasSize(2));
    }

    private VDS newHost(String version) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setHostOs(version);
        return host;
    }

    private List<Pair<Guid, Integer>> filter(final VM vm, final VDS... hosts) {
        return inClusterUpgradeWeightPolicyUnit.score(new VDSGroup(), Arrays.asList(hosts),
                vm,
                null);
    }

    private static Pair<Guid, Integer> weight(final VDS host, int weight) {
        return new Pair<>(host.getId(), weight);
    }
}
