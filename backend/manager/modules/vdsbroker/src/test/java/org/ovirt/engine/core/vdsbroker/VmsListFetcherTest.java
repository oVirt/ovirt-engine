package org.ovirt.engine.core.vdsbroker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

import java.util.Collections;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class VmsListFetcherTest {

    VmsListFetcher vmsListFetcher;

    @DataPoints
    public static VmTestPairs[] vms = VmTestPairs.values();

    @Mock
    DbFacade dbFacade;
    @Mock
    VdsManager vdsManager;
    @Mock
    ResourceManager resourceManager;
    @Mock
    VdsDAO vdsDAO;
    @Mock
    private VmDAO vmDAO;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(dbFacade.getVdsDao()).thenReturn(vdsDAO);
        when(dbFacade.getVmDao()).thenReturn(vmDAO);
        VDS vds = new VDS();
        vds.setId(VmTestPairs.SRC_HOST_ID);
        when(vdsManager.getCopyVds()).thenReturn(vds);
        when(vdsManager.getVdsId()).thenReturn(vds.getId());
        vmsListFetcher = new VmsListFetcher(vdsManager, dbFacade, resourceManager);
    }

    @Theory
    public void changedVms(VmTestPairs data) {
        //given
        stubCalls(data);
        //when
        vmsListFetcher.fetch();
        assumeTrue(data.dbVm() != null);
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.dbVm().getStatus() != data.vdsmVm().getVmDynamic().getStatus());
        //then
        Assert.assertTrue(vmsListFetcher.getChangedVms().size() == 1);
        Assert.assertTrue(vmsListFetcher.getChangedVms().get(0).getFirst() == data.dbVm());
    }

    @Theory
    public void stableVms(VmTestPairs data) {
        //given
        stubCalls(data);
        //when
        assumeTrue(data.vdsmVm() != null);
        assumeTrue(data.dbVm() != null && data.dbVm().getStatus() == data.vdsmVm().getVmDynamic().getStatus());
        //then
        vmsListFetcher.fetch();
        Assert.assertTrue(vmsListFetcher.getChangedVms().isEmpty());
    }

    private void stubCalls(VmTestPairs data) {
        when(resourceManager.runVdsCommand(
                eq(VDSCommandType.List),
                any(VdsIdAndVdsVDSCommandParametersBase.class))).
                thenReturn(getVdsReturnValue(data.vdsmVm()));
        if (data.dbVm() != null) {
            when(vmDAO.getAllRunningByVds(VmTestPairs.SRC_HOST_ID)).
                    thenReturn(Collections.singletonMap(data.dbVm().getId(), data.dbVm()));
        }
        if (data.vdsmVm() != null) {
            when(resourceManager.runVdsCommand(
                    eq(VDSCommandType.GetVmStats),
                    any(VdsIdAndVdsVDSCommandParametersBase.class))).
                    thenReturn(getStatsReturnValue(data.vdsmVm()));
        }
    }

    private void stubVmMigratingTo(VmTestPairs data) {
        if (data.dbVm() == null) {
            when(vmDAO.get(data.vdsmVm().getVmDynamic().getId())).
                    thenReturn(new VM());
        }
    }

    private VDSReturnValue getVdsReturnValue(VmInternalData vdsmVm) {
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

    private VDSReturnValue getStatsReturnValue(VmInternalData vdsmVm) {
        VDSReturnValue value = new VDSReturnValue();
        value.setSucceeded(true);
        if (vdsmVm != null) {
            value.setReturnValue(vdsmVm);
        } else {
            value.setReturnValue(null);
        }
        return value;

    }

}
