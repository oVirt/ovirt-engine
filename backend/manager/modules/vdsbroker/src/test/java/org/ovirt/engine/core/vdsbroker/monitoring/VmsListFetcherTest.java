package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmsListFetcherTest {

    VmsListFetcher vmsListFetcher;

    @Mock
    VdsManager vdsManager;
    @Mock
    @InjectedMock
    public ResourceManager resourceManager;
    @Mock
    @InjectedMock
    public VmDynamicDao vmDynamicDao;
    @Captor
    ArgumentCaptor<List<VmDynamic>> vdsManagerArgumentCaptor;

    @BeforeEach
    public void setup() {
        VDS vds = new VDS();
        vds.setId(VmTestPairs.SRC_HOST_ID);
        when(vdsManager.getCopyVds()).thenReturn(vds);
        when(vdsManager.getVdsId()).thenReturn(vds.getId());
        vmsListFetcher = new VmsListFetcher(vdsManager);
    }

    //TODO: positive test

    @Test
    public void callToVDSMFailed() {
        // given
        stubFailedCalls();
        assertFalse(vmsListFetcher.fetch());
    }

    private void stubFailedCalls() {
        when(resourceManager.runVdsCommand(any(), any())).thenReturn(getFailedVdsReturnValue());
    }

    private VDSReturnValue getFailedVdsReturnValue() {
        VDSReturnValue value = new VDSReturnValue();
        value.setSucceeded(false);
        return value;
    }

}
