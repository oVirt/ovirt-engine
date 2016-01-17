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
import org.ovirt.engine.core.dao.ActionGroupDao;


public class GetCommandsCompatibilityVersionsQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, GetCommandsCompatibilityVersionsQuery<VdcQueryParametersBase>> {

    private ActionGroupDao actionGroupDaoMock;
    private static final Version RUN_VM_VERSION = Version.v3_0;
    private static final Version ADD_VM_FROM_SNAPSHOT_VERSION = Version.v3_1;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setUpDaoMocks();
    }
    @Test
    public void testExecuteQueryCommand() {
        GetCommandsCompatibilityVersionsQuery<VdcQueryParametersBase> queryToRun = getQuery();
        ActionVersionMap runVm = new ActionVersionMap();
        runVm.setActionType(VdcActionType.RunVm);
        runVm.setClusterMinimalVersion(RUN_VM_VERSION.toString());
        runVm.setStoragePoolMinimalVersion(RUN_VM_VERSION.toString());
        ActionVersionMap addVmFromSnapshot = new ActionVersionMap();
        addVmFromSnapshot.setActionType(VdcActionType.AddVmFromSnapshot);
        addVmFromSnapshot.setClusterMinimalVersion(ADD_VM_FROM_SNAPSHOT_VERSION.toString());
        addVmFromSnapshot.setStoragePoolMinimalVersion(ADD_VM_FROM_SNAPSHOT_VERSION.toString());
        List<ActionVersionMap> entriesFromDb = Arrays.asList(runVm, addVmFromSnapshot);
        doReturn(entriesFromDb).when(actionGroupDaoMock).getAllActionVersionMap();
        queryToRun.execute();
        VdcQueryReturnValue returnValue = queryToRun.getQueryReturnValue();
        assertNotNull(returnValue);
        assertNotNull(returnValue.getReturnValue());
        @SuppressWarnings("unchecked")
        Map<VdcActionType, CommandVersionsInfo> resultMap = returnValue.getReturnValue();
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

    private void setUpDaoMocks() {
        // Mock the Daos
        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        actionGroupDaoMock = mock(ActionGroupDao.class);
        when(dbFacadeMock.getActionGroupDao()).thenReturn(actionGroupDaoMock);
    }
}
