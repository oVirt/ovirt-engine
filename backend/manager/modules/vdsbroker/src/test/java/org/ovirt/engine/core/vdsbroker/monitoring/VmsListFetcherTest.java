package org.ovirt.engine.core.vdsbroker.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
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

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void changedVms(VmTestPairs data) {
        //given
        stubCalls(data);
        //when
        assertTrue(vmsListFetcher.fetch());
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.dbVm().getStatus() != data.vdsmVm().getVmDynamic().getStatus());
        //then
        assertEquals(1, vmsListFetcher.getChangedVms().size());
        assertSame(vmsListFetcher.getChangedVms().get(0).getFirst(), data.dbVm().getDynamicData());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void stableVms(VmTestPairs data) {
        //given
        stubCalls(data);
        //when
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.dbVm() != null && data.dbVm().getStatus() == data.vdsmVm().getVmDynamic().getStatus());
        //then
        assertTrue(vmsListFetcher.fetch());
        assertTrue(vmsListFetcher.getChangedVms().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void lastVmListNotIncludingExternalVm(VmTestPairs data) {
        //given
        stubCalls(data);
        //when
        assertTrue(vmsListFetcher.fetch());
        /* assume non external VM */
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.dbVm() != null);
        //then
        verify(vdsManager).setLastVmsList(vdsManagerArgumentCaptor.capture());
        assertEquals(data.vdsmVm().getVmDynamic(), vdsManagerArgumentCaptor.getValue().get(0));
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void externalVmAreNotSavedAsLastVm(VmTestPairs data) {
     //given
        stubCalls(data);
        //when
        assertTrue(vmsListFetcher.fetch());
        /* assume external VM */
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.dbVm() == null);
        //then
        verify(vdsManager).setLastVmsList(vdsManagerArgumentCaptor.capture());
        assertEquals(0, vdsManagerArgumentCaptor.getValue().size());
    }

    @ParameterizedTest
    @EnumSource(VmTestPairs.class)
    public void callToVDSMFailed(VmTestPairs data) {
        // given
        stubFailedCalls();
        assertFalse(vmsListFetcher.fetch());
    }

    private void stubCalls(VmTestPairs data) {
        when(resourceManager.runVdsCommand(eq(VDSCommandType.List), any())).thenReturn(getVdsReturnValue(data.vdsmVm()));
        if (data.dbVm() != null) {
            when(vmDynamicDao.getAllRunningForVds(VmTestPairs.SRC_HOST_ID)).
                    thenReturn(Collections.singletonList(data.dbVm().getDynamicData()));
        }
        if (data.vdsmVm() != null) {
            when(resourceManager.runVdsCommand(eq(VDSCommandType.GetVmStats), any()))
                    .thenReturn(getStatsReturnValue(data.vdsmVm()));
        }
    }

    private VDSReturnValue getVdsReturnValue(VdsmVm vdsmVm) {
        VDSReturnValue value = new VDSReturnValue();
        value.setSucceeded(true);
        if (vdsmVm != null) {
            value.setReturnValue(
                    Collections.singletonMap(
                            vdsmVm.getVmDynamic().getId(), vdsmVm));
        } else {
            value.setReturnValue(Collections.emptyMap());
        }
        return value;
    }

    private VDSReturnValue getStatsReturnValue(VdsmVm vdsmVm) {
        VDSReturnValue value = new VDSReturnValue();
        value.setSucceeded(true);
        if (vdsmVm != null) {
            value.setReturnValue(vdsmVm);
        } else {
            value.setReturnValue(null);
        }
        return value;

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
