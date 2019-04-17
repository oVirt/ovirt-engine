package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit.BAD_WEIGHT;
import static org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit.BEST_WEIGHT;
import static org.ovirt.engine.core.bll.scheduling.policyunits.InClusterUpgradeWeightPolicyUnit.BETTER_WEIGHT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDynamicDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InClusterUpgradeWeightPolicyUnitTest {
    @Mock
    private VdsDynamicDao vdsDynamicDao;

    @InjectMocks
    private final InClusterUpgradeWeightPolicyUnit inClusterUpgradeWeightPolicyUnit = new
            InClusterUpgradeWeightPolicyUnit(null, null);

    private VM runningVm;

    private VDS newEnoughHost;
    private VDS tooOldHost;
    private VDS currentHost;

    @BeforeEach
    public void setUp() {
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
        assertThat(filter(runningVm, tooOldHost, newEnoughHost, additionalNewHost)).containsExactly(
                weight(tooOldHost, BAD_WEIGHT),
                weight(newEnoughHost, BEST_WEIGHT),
                weight(additionalNewHost, BEST_WEIGHT)
        );
    }

    @Test
    public void shouldKeepCurrentOsVersionHosts() {
        assertThat(filter(runningVm, newEnoughHost, currentHost)).containsExactly(
                weight(newEnoughHost, BEST_WEIGHT),
                weight(currentHost, BETTER_WEIGHT)
        );
    }

    @Test
    public void shouldKeepSameMajorWithOlderMinor() {
        newEnoughHost.setHostOs("RHEL - 6.0 - 1.el6");
        assertThat(filter(runningVm, newEnoughHost, currentHost)).containsExactly(
                weight(newEnoughHost, BETTER_WEIGHT),
                weight(currentHost, BETTER_WEIGHT)
        );
    }

    @Test
    public void shouldKeepSameMajorWithNewerMinor() {
        newEnoughHost.setHostOs("RHEL - 6.3 - 1.el6");
        assertThat(filter(runningVm, newEnoughHost, currentHost)).containsExactly(
                weight(newEnoughHost, BETTER_WEIGHT),
                weight(currentHost, BETTER_WEIGHT)
        );
    }

    @Test
    public void shouldWeightNewestHostsBetterOnVmStarts() {
        final VM newVM = new VM();
        assertThat(filter(newVM, newEnoughHost, tooOldHost)).contains(
                        weight(tooOldHost, BAD_WEIGHT),
                        weight(newEnoughHost, BEST_WEIGHT));
    }

    @Test
    public void shouldWeightNothingOnVmStartWithDifferenOsFamilies() {
        VDS fedoraHost = newHost("Fedora - 23 - 1.fc23");
        final VM newVM = new VM();
        assertThat(filter(newVM, tooOldHost, newEnoughHost, fedoraHost)).contains(
                weight(tooOldHost, BEST_WEIGHT),
                weight(newEnoughHost, BEST_WEIGHT),
                weight(fedoraHost, BEST_WEIGHT));
        assertThat(filter(newVM, tooOldHost, newEnoughHost, fedoraHost)).hasSize(3);
    }

    @Test
    public void shouldWeightNothingWithAllHostsInvalidOnVmStart() {
        VDS invalidHost = newHost("RHEL - - 1.fc23");
        final VM newVM = new VM();
        assertThat(filter(newVM, invalidHost, invalidHost)).contains(
                weight(invalidHost, BEST_WEIGHT),
                weight(invalidHost, BEST_WEIGHT));
        assertThat(filter(newVM, invalidHost, invalidHost)).hasSize(2);
    }

    @Test
    public void shouldWeightOnlyValidHostsOnVmStart() {
        VDS invalidHost = newHost("RHEL - - 1.fc23");
        final VM newVM = new VM();
        assertThat(filter(newVM, invalidHost, newEnoughHost)).contains(
                weight(invalidHost, BAD_WEIGHT),
                weight(newEnoughHost, BEST_WEIGHT));
        assertThat(filter(newVM, invalidHost, newEnoughHost)).hasSize(2);
    }

    @Test
    public void shouldDetectMissingCurrentOsInformation() {
        currentHost.setHostOs(null);
        assertThat(filter(runningVm, tooOldHost, newEnoughHost)).containsExactly(
                weight(tooOldHost, BEST_WEIGHT),
                weight(newEnoughHost, BEST_WEIGHT)
        );
    }

    @Test
    public void shouldDetectMissingOsOnTargetHosts() {
        newEnoughHost.setHostOs(null);
        assertThat(filter(runningVm, tooOldHost, newEnoughHost)).containsExactly(
                weight(tooOldHost, BAD_WEIGHT),
                weight(newEnoughHost, BAD_WEIGHT)
        );
    }

    @Test
    public void shouldDetectInvalidOsOnTargetHosts() {
        newEnoughHost.setHostOs("7.3");
        assertThat(filter(runningVm, tooOldHost, newEnoughHost)).containsExactly(
                weight(tooOldHost, BAD_WEIGHT),
                weight(newEnoughHost, BAD_WEIGHT)
        );
    }

    @Test
    public void shouldDetectDifferentOS() {
        newEnoughHost.setHostOs("Fedora - 23 - 1.f23");
        assertThat(filter(runningVm, tooOldHost, newEnoughHost)).containsExactly(
                weight(tooOldHost, BAD_WEIGHT),
                weight(newEnoughHost, BAD_WEIGHT)
        );
    }

    private VDS newHost(String version) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setHostOs(version);
        return host;
    }

    private List<Pair<Guid, Integer>> filter(final VM vm, final VDS... hosts) {
        return inClusterUpgradeWeightPolicyUnit.score(new SchedulingContext(new Cluster(), Collections.emptyMap()),
                Arrays.asList(hosts),
                vm);
    }

    private static Pair<Guid, Integer> weight(final VDS host, int weight) {
        return new Pair<>(host.getId(), weight);
    }
}
