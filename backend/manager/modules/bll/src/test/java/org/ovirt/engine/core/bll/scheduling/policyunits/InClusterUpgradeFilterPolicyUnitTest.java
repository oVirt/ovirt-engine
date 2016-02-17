package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class InClusterUpgradeFilterPolicyUnitTest extends AbstractPolicyUnitTest {

    @ClassRule
    public static MockConfigRule configRule = new MockConfigRule(
            MockConfigRule.mockConfig(ConfigValues.MaxSchedulerWeight, Integer.MAX_VALUE)
    );

    @Mock
    private VdsDynamicDao vdsDynamicDao;

    private InClusterUpgradeFilterPolicyUnit inClusterUpgradeFilterPolicyUnit;

    private VM runningVm;

    private VDS newEnoughHost;
    private VDS tooOldHost;
    private VDS currentHost;

    @Before
    public void setUp() {

        injectorRule.bind(VdsDynamicDao.class, vdsDynamicDao);
        inClusterUpgradeFilterPolicyUnit = new InClusterUpgradeFilterPolicyUnit(null, null);
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
        assertThat(filter(runningVm, tooOldHost, newEnoughHost, additionalNewHost),
                hasItems(newEnoughHost, additionalNewHost));
        assertThat(filter(runningVm, tooOldHost, newEnoughHost, additionalNewHost),
                hasSize(2));
    }

    @Test
    public void shouldKeepCurrentOsVersionHosts() {
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasItems(newEnoughHost, currentHost));
    }

    @Test
    public void shouldKeepSameMajorWithOlderMinor() {
        newEnoughHost.setHostOs("RHEL - 6.0 - 1.el6");
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasItems(newEnoughHost, currentHost));
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasSize(2));
    }

    @Test
    public void shouldKeepSameMajorWithNewerMinor() {
        newEnoughHost.setHostOs("RHEL - 6.3 - 1.el6");
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasItems(newEnoughHost, currentHost));
        assertThat(filter(runningVm, newEnoughHost, currentHost), hasSize(2));
    }

    @Test
    public void shouldDetectNeverStartedVM() {
        final VM newVM = new VM();
        assertThat(filter(newVM, tooOldHost, newEnoughHost), hasItems(newEnoughHost, tooOldHost));
    }

    @Test
    public void shouldDetectMissingCurrentOsInformation() {
        currentHost.setHostOs(null);
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasItems(tooOldHost, newEnoughHost));
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), hasSize(2));
    }

    @Test
    public void shouldDetectMissingOsOnTargetHosts() {
        newEnoughHost.setHostOs(null);
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), is(empty()));
    }

    @Test
    public void shouldDetectInvalidOsOnTargetHosts() {
        newEnoughHost.setHostOs("7.3");
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), is(empty()));
    }

    @Test
    public void shouldDetectDifferentOS() {
        newEnoughHost.setHostOs("Fedora - 23 - 1.f23");
        assertThat(filter(runningVm, tooOldHost, newEnoughHost), is(empty()));
    }

    private VDS newHost(String version) {
        VDS host = new VDS();
        host.setId(Guid.newGuid());
        host.setHostOs(version);
        return host;
    }

    private List<VDS> filter(final VM vm, final VDS... hosts) {
        return inClusterUpgradeFilterPolicyUnit.filter(new VDSGroup(), Arrays.asList(hosts),
                vm,
                null,
                mock(PerHostMessages.class));
    }
}
