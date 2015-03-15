package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.CommandAssertUtils.checkSucceeded;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDAO;

public class GetLunsByVgIdTest extends AbstractQueryTest<GetLunsByVgIdParameters, GetLunsByVgIdQuery<? extends GetLunsByVgIdParameters>> {
    private static final Guid[] GUIDS = new Guid[] {
            new Guid("11111111-1111-1111-1111-111111111111"),
            new Guid("22222222-2222-2222-2222-222222222222"),
            new Guid("33333333-3333-3333-3333-333333333333"),
    };

    private static final String VG_ID = Guid.newGuid().toString();
    private static final Guid VDS_ID = Guid.newGuid();

    private static final String ADDRESS = "foo.bar.com";
    private static final String PORT = "123456";
    private static final String[] IQNS = new String[] { ADDRESS + ":1", ADDRESS + ":2", ADDRESS + ":3" };
    private static final String PHYSICAL_DEVICE_FIELD = "sda";
    private static final String DUMMY_LUN_ID = BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX+"89871115-e64d-4754-bacd-556cc249761b";
    private VDSBrokerFrontend vdsBrokerFrontendMock;
    @Mock
    StorageServerConnectionLunMapDAO storageServerConnectionLunMapDAO;
    @Mock
    StorageServerConnectionDAO storageServerConnectionDAO;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        prepareMocks();
        setUpStorageServerConnectionLunMapDAO();
        setUpStorageServerConnectionDAO();
    }

    @Test
    public void testQuery() {
        commonTestFlow(false);
    }

    @Test
    public void testQueryDummyLun() {
        commonTestFlow(true);
    }

    private void commonTestFlow(boolean withDummyLun) {
        when(getQueryParameters().getVgId()).thenReturn(VG_ID);
        when(getQueryParameters().getVdsId()).thenReturn(VDS_ID);

        expectGetLunsForVg(VG_ID, withDummyLun);
        expectGetDeviceList();

        expectGetLunsMap(storageServerConnectionLunMapDAO);
        expectGetConnections(storageServerConnectionDAO);

        getQuery().setInternalExecution(true);
        getQuery().executeCommand();

        checkSucceeded(getQuery(), true);
        checkReturnValue(getQuery());
    }

    private void prepareMocks() {
        vdsBrokerFrontendMock = mock(VDSBrokerFrontend.class);
        doReturn(vdsBrokerFrontendMock).when(getQuery()).getVdsBroker();
    }

    protected StorageServerConnectionDAO setUpStorageServerConnectionDAO() {
        storageServerConnectionDAO = mock(StorageServerConnectionDAO.class);
        when(getDbFacadeMockInstance().getStorageServerConnectionDao()).thenReturn(storageServerConnectionDAO);
        return storageServerConnectionDAO;
    }

    protected StorageServerConnectionLunMapDAO setUpStorageServerConnectionLunMapDAO() {
        storageServerConnectionLunMapDAO = mock(StorageServerConnectionLunMapDAO.class);
        when(getDbFacadeMockInstance().getStorageServerConnectionLunMapDao()).thenReturn(storageServerConnectionLunMapDAO);
        return storageServerConnectionLunMapDAO;
    }

    protected void expectGetLunsForVg(String vgId, boolean withDummyLun) {
        LunDAO dao = mock(LunDAO.class);
        when(getDbFacadeMockInstance().getLunDao()).thenReturn(dao);
        when(dao.getAllForVolumeGroup(vgId)).thenReturn(setUpLuns(withDummyLun));
    }

    protected void expectGetDeviceList() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(setUpLunsFromDeviceList());
        when(vdsBrokerFrontendMock.RunVdsCommand(eq(VDSCommandType.GetDeviceList),
                any(GetDeviceListVDSCommandParameters.class))).thenReturn(returnValue);
    }

    protected void expectGetLunsMap(StorageServerConnectionLunMapDAO dao) {
        for (int i = 0; i < GUIDS.length; i++) {
            expectGetLunsMap(dao, GUIDS[i].toString(), GUIDS[i].toString());
        }
    }

    protected void expectGetLunsMap(StorageServerConnectionLunMapDAO dao, String lunId, String cnxId) {
        List<LUN_storage_server_connection_map> ret = new ArrayList<LUN_storage_server_connection_map>();
        LUN_storage_server_connection_map map = new LUN_storage_server_connection_map();
        map.setLunId(lunId);
        map.setstorage_server_connection(cnxId);
        ret.add(map);
        when(dao.getAll(lunId)).thenReturn(ret);
    }

    protected void expectGetConnections(StorageServerConnectionDAO dao) {
        for (int i = 0; i < GUIDS.length; i++) {
            when(dao.get(GUIDS[i].toString())).thenReturn(setUpConnection(i));
        }
    }

    protected List<LUNs> setUpLuns(boolean withDummyLun) {
        List<LUNs> luns = new ArrayList<LUNs>();
        for (int i = 0; i < GUIDS.length; i++) {
            LUNs lun = new LUNs();
            lun.setLUN_id(GUIDS[i].toString());
            luns.add(lun);
        }
        if (withDummyLun) {
            LUNs dummyLun = new LUNs();
            dummyLun.setLUN_id(DUMMY_LUN_ID);
            luns.add(dummyLun);
        }
        return luns;
    }

    protected List<LUNs> setUpLunsFromDeviceList() {
        List<LUNs> luns = setUpLuns(false);
        for (int i = 0; i < luns.size(); i++) {
            HashMap<String, Boolean> pathsDictionary = new HashMap<String, Boolean>();
            pathsDictionary.put(PHYSICAL_DEVICE_FIELD, true);
            luns.get(i).setPathsDictionary(pathsDictionary);
        }
        return luns;
    }

    protected StorageServerConnections setUpConnection(int idx) {
        return new StorageServerConnections(ADDRESS, GUIDS[idx].toString(), IQNS[idx], null,
                StorageType.ISCSI, null, PORT, null);
    }

    @SuppressWarnings("unchecked")
    protected void checkReturnValue(QueriesCommandBase<?> query) {
        assertNotNull(query.getQueryReturnValue().getReturnValue());
        assertTrue(List.class.isInstance(query.getQueryReturnValue().getReturnValue()));
        List<LUNs> luns = (List<LUNs>) query.getQueryReturnValue().getReturnValue();
        assertEquals(GUIDS.length, luns.size());
        for (int i = 0; i < GUIDS.length; i++) {
            LUNs lun = luns.get(i);
            assertNotNull(lun);
            assertEquals(GUIDS[i].toString(), lun.getLUN_id());
            assertNotNull(lun.getLunConnections());
            assertEquals(1, lun.getLunConnections().size());
            StorageServerConnections cnx = lun.getLunConnections().get(0);
            assertEquals(ADDRESS, cnx.getconnection());
            assertEquals(PORT, cnx.getport());
            assertEquals(GUIDS[i].toString(), cnx.getid());
            assertEquals(IQNS[i], cnx.getiqn());
            assertEquals(StorageType.ISCSI, cnx.getstorage_type());
            assertNotNull(lun.getPathsDictionary());
            assertEquals(1, lun.getPathsDictionary().size());
        }
    }
}
