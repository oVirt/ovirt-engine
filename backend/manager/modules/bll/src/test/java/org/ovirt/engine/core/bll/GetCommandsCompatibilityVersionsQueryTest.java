package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionVersionMap;
import org.ovirt.engine.core.common.queries.CommandVersionsInfo;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ActionGroupDAO;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


public class GetCommandsCompatibilityVersionsQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, GetCommandsCompatibilityVersionsQuery<VdcQueryParametersBase>> {

    private ActionGroupDAO actionGroupDaoMock;
    private static final Version RUN_VM_VERSION = Version.v2_2;
    private static final Version ADD_VM_FROM_SNAPSHOT_VERSION = Version.v3_1;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpDAOMocks();
    }
    @Test
    public void testExecuteQueryCommand() {
        GetCommandsCompatibilityVersionsQuery<VdcQueryParametersBase> queryToRun = getQuery();
        List<ActionVersionMap> entriesFromDb =
                Arrays.asList(new ActionVersionMap(VdcActionType.RunVm, RUN_VM_VERSION, RUN_VM_VERSION),
                        new ActionVersionMap(VdcActionType.AddVmFromSnapshot,
                                ADD_VM_FROM_SNAPSHOT_VERSION,
                                ADD_VM_FROM_SNAPSHOT_VERSION));
        doReturn(entriesFromDb).when(actionGroupDaoMock).getAllActionVersionMap();
        queryToRun.Execute();
        VdcQueryReturnValue returnValue = queryToRun.getQueryReturnValue();
        assertNotNull(returnValue);
        assertNotNull(returnValue.getReturnValue());
        @SuppressWarnings("unchecked")
        Map<VdcActionType, CommandVersionsInfo> resultMap =
                (Map<VdcActionType, CommandVersionsInfo>) returnValue.getReturnValue();
        assertEquals(2, resultMap.size());
        verifyEntry(resultMap, VdcActionType.RunVm, RUN_VM_VERSION);
        verifyEntry(resultMap, VdcActionType.AddVmFromSnapshot,ADD_VM_FROM_SNAPSHOT_VERSION);


    }

    protected void verifyEntry(Map<VdcActionType, CommandVersionsInfo> resultMap,
            VdcActionType actionType,
            Version version) {
        CommandVersionsInfo runVmEntry = resultMap.get(actionType);
        assertNotNull(runVmEntry);
        assertEquals(version, runVmEntry.getClusterVersion());
    }

    private void setUpDAOMocks() {
        // Mock the DAOs
        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        actionGroupDaoMock = mock(ActionGroupDAO.class);
        when(dbFacadeMock.getActionGroupDao()).thenReturn(actionGroupDaoMock);
    }
}
