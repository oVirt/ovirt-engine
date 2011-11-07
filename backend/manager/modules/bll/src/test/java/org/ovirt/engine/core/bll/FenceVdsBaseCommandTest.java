package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, StopVdsCommand.class })
public class FenceVdsBaseCommandTest {

    @Mock
    DbFacade dbFacade;

    @Mock
    VmDAO vmDAO;

    @Mock
    VdsDAO vdsDAO;

    public FenceVdsBaseCommandTest() {
        mockStatic(DbFacade.class);
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void mockDbFacadeAndDAO() {
        List<VM> list = new ArrayList<VM>();
        when(DbFacade.getInstance()).thenReturn(dbFacade);
        when(dbFacade.getVmDAO()).thenReturn(vmDAO);
        when(dbFacade.getVdsDAO()).thenReturn(vdsDAO);
        when(vmDAO.getAllRunningForVds(any(Guid.class))).thenReturn(list);
        when(vdsDAO.get(any(Guid.class))).thenReturn(null);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void canDoActionStopCommandFailWithInvalidHostTest() {
        StopVdsCommand<FenceVdsActionParameters> command =
                spy(new StopVdsCommand<FenceVdsActionParameters>(createParameters()));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);

    }

    @Test
    public void canDoActionStartCommandFailWithInvalidHostTest() {
        StartVdsCommand<FenceVdsActionParameters> command =
                spy(new StartVdsCommand<FenceVdsActionParameters>(createParameters()));
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);

    }

    protected FenceVdsActionParameters createParameters() {
        final FenceVdsActionParameters p =
                new FenceVdsActionParameters(Guid.NewGuid(), FenceActionType.Stop);
        return p;
    }

}
