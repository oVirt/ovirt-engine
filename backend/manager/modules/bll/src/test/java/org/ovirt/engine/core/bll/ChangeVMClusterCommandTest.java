package org.ovirt.engine.core.bll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;

@RunWith(MockitoJUnitRunner.class)
public class ChangeVMClusterCommandTest {

    @Spy
    ChangeVMClusterCommand<ChangeVMClusterParameters> underTest =
            new ChangeVMClusterCommand(new ChangeVMClusterParameters(), null);

    @Before
    public void setup() {
        // override the instrumented class type set by Mockito runner
        when(underTest.getActionType()).thenReturn(VdcActionType.ChangeVMCluster);
    }

    @Test
    public void canRunForHostedEngine() throws Exception {
        // given hosted engine VM
        VM hostedEngine = new VM();
        hostedEngine.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        when(underTest.getVm()).thenReturn(hostedEngine);

        assertThat(underTest.canRunActionOnNonManagedVm()).isTrue();

    }
}
