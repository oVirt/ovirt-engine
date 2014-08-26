package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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


public class GetCommandsCompatibilityVersionsQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, GetCommandsCompatibilityVersionsQuery<VdcQueryParametersBase>> {

    private ActionGroupDAO actionGroupDaoMock;
    private static final Version RUN_VM_VERSION = Version.v3_0;
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
        ActionVersionMap runVm = new ActionVersionMap();
        runVm.setaction_type(VdcActionType.RunVm);
        runVm.setcluster_minimal_version(RUN_VM_VERSION.toString());
        runVm.setstorage_pool_minimal_version(RUN_VM_VERSION.toString());
        ActionVersionMap addVmFromSnapshot = new ActionVersionMap();
        addVmFromSnapshot.setaction_type(VdcActionType.AddVmFromSnapshot);
        addVmFromSnapshot.setcluster_minimal_version(ADD_VM_FROM_SNAPSHOT_VERSION.toString());
        addVmFromSnapshot.setstorage_pool_minimal_version(ADD_VM_FROM_SNAPSHOT_VERSION.toString());
        List<ActionVersionMap> entriesFromDb = Arrays.asList(runVm, addVmFromSnapshot);
        doReturn(entriesFromDb).when(actionGroupDaoMock).getAllActionVersionMap();
        queryToRun.execute();
        VdcQueryReturnValue returnValue = queryToRun.getQueryReturnValue();
        assertNotNull(returnValue);
        assertNotNull(returnValue.getReturnValue());
        @SuppressWarnings("unchecked")
        Map<VdcActionType, CommandVersionsInfo> resultMap =
                (Map<VdcActionType, CommandVersionsInfo>) returnValue.getReturnValue();
        assertEquals(2, resultMap.size());
        verifyEntry(resultMap, VdcActionType.RunVm, RUN_VM_VERSION);
        verifyEntry(resultMap, VdcActionType.AddVmFromSnapshot, ADD_VM_FROM_SNAPSHOT_VERSION);


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
