package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmDAO;

@RunWith(MockitoJUnitRunner.class)
public class FenceVdsBaseCommandTest {
    @Mock
    VmDAO vmDAO;

    @Mock
    VdsDAO vdsDAO;

    StopVdsCommand<FenceVdsActionParameters> command;

    protected FenceVdsActionParameters createParameters() {
        return new FenceVdsActionParameters(Guid.NewGuid(), FenceActionType.Stop);
    }

    @SuppressWarnings("serial")
    @Before
    public void mockDbFacadeAndDAO() {
        MockitoAnnotations.initMocks(this);
        List<VM> list = new ArrayList<VM>();
        when(vmDAO.getAllRunningForVds(any(Guid.class))).thenReturn(list);

        command = new StopVdsCommand<FenceVdsActionParameters>(createParameters()) {
            // These methods are protected in AuditLoggableBase, and thus not accessible for spying.
            @Override
            public VmDAO getVmDAO() {
                return vmDAO;
            }

            @Override
            public VdsDAO getVdsDAO() {
                return vdsDAO;
            }
        };
    }

    @Test
    public void canDoActionStopCommandFailWithInvalidHostTest() {
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);

    }

    @Test
    public void canDoActionStartCommandFailWithInvalidHostTest() {
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command, VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);

    }
}
