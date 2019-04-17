package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDynamicDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InClusterUpgradeFilterPolicyUnitTest {
    @Mock
    private VdsDynamicDao vdsDynamicDao;

    @InjectMocks
    private final InClusterUpgradeFilterPolicyUnit inClusterUpgradeFilterPolicyUnit =
            new InClusterUpgradeFilterPolicyUnit(null, null);

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
        assertThat(filter(runningVm, tooOldHost, newEnoughHost, additionalNewHost))
                .containsOnly(newEnoughHost, additionalNewHost);
    }

    @Test
    public void shouldKeepCurrentOsVersionHosts() {
        assertThat(filter(runningVm, newEnoughHost, currentHost)).contains(newEnoughHost, currentHost);
    }

    @Test
    public void shouldKeepSameMajorWithOlderMinor() {
        newEnoughHost.setHostOs("RHEL - 6.0 - 1.el6");
        assertThat(filter(runningVm, newEnoughHost, currentHost)).containsExactly(newEnoughHost, currentHost);
    }

    @Test
    public void shouldKeepSameMajorWithNewerMinor() {
        newEnoughHost.setHostOs("RHEL - 6.3 - 1.el6");
        assertThat(filter(runningVm, newEnoughHost, currentHost)).containsExactly(newEnoughHost, currentHost);
    }

    @Test
    public void shouldDetectNeverStartedVM() {
        final VM newVM = new VM();
        assertThat(filter(newVM, tooOldHost, newEnoughHost)).contains(newEnoughHost, tooOldHost);
    }

    @Test
    public void shouldDetectMissingCurrentOsInformation() {
        currentHost.setHostOs(null);
        assertThat(filter(runningVm, tooOldHost, newEnoughHost)).containsOnly(tooOldHost, newEnoughHost);
    }

    @Test
    public void shouldDetectMissingOsOnTargetHosts() {
        newEnoughHost.setHostOs(null);
        assertThat(filter(runningVm, tooOldHost, newEnoughHost)).isEmpty();
    }

    @Test
    public void shouldDetectInvalidOsOnTargetHosts() {
        newEnoughHost.setHostOs("7.3");
        assertThat(filter(runningVm, tooOldHost, newEnoughHost)).isEmpty();
    }

    @Test
    public void shouldDetectDifferentOS() {
        newEnoughHost.setHostOs("Fedora - 23 - 1.f23");
        assertThat(filter(runningVm, tooOldHost, newEnoughHost)).isEmpty();
    }

    private VDS newHost(String version) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setHostOs(version);
        return host;
    }

    private List<VDS> filter(final VM vm, final VDS... hosts) {
        return inClusterUpgradeFilterPolicyUnit.filter(new SchedulingContext(new Cluster(), Collections.emptyMap()),
                Arrays.asList(hosts),
                vm,
                mock(PerHostMessages.class));
    }
}
