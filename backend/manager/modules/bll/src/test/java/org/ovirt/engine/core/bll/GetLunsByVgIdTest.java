package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDAO;

public class GetLunsByVgIdTest extends BaseMockitoTest {

    private static final String VG_ID = GUIDS[0].toString();

    private static final String ADDRESS = "foo.bar.com";
    private static final String PORT = "123456";
    private static final String[] IQNS = new String[] { ADDRESS + ":1", ADDRESS + ":2", ADDRESS + ":3" };

    @Test
    public void testQuery() {
        DbFacade db = setUpDB();

        expectGetLunsForVg(db, VG_ID);

        StorageServerConnectionLunMapDAO lunMapDAO = setUpStorageServerConnectionLunMapDAO(db);
        StorageServerConnectionDAO cnxDAO = setUpStorageServerConnectionDAO(db);

        expectGetLunsMap(lunMapDAO);
        expectGetConnections(cnxDAO);

        GetLunsByVgIdQuery query = new GetLunsByVgIdQuery(getParams());
        query.ExecuteCommand();

        checkSucceeded(query, true);
        checkReturnValue(query);
    }

    protected StorageServerConnectionDAO setUpStorageServerConnectionDAO(DbFacade db) {
        StorageServerConnectionDAO dao = mock(StorageServerConnectionDAO.class);
        when(db.getStorageServerConnectionDAO()).thenReturn(dao);
        return dao;
    }

    protected StorageServerConnectionLunMapDAO setUpStorageServerConnectionLunMapDAO(DbFacade db) {
        StorageServerConnectionLunMapDAO dao = mock(StorageServerConnectionLunMapDAO.class);
        when(db.getStorageServerConnectionLunMapDAO()).thenReturn(dao);
        return dao;
    }

    protected void expectGetLunsForVg(DbFacade db, String vgId) {
        LunDAO dao = mock(LunDAO.class);
        when(db.getLunDAO()).thenReturn(dao);
        when(dao.getAllForVolumeGroup(vgId)).thenReturn(setUpLuns());
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

    protected List<LUNs> setUpLuns() {
        List<LUNs> luns = new ArrayList<LUNs>();
        for (int i = 0; i < GUIDS.length; i++) {
            LUNs lun = new LUNs();
            lun.setLUN_id(GUIDS[i].toString());
            luns.add(lun);
        }
        return luns;
    }

    protected storage_server_connections setUpConnection(int idx) {
        return new storage_server_connections(ADDRESS, GUIDS[idx].toString(), IQNS[idx], null,
                                              StorageType.ISCSI, null, PORT, null);
    }

    protected GetLunsByVgIdParameters getParams() {
        return new GetLunsByVgIdParameters(VG_ID);
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
            storage_server_connections cnx = lun.getLunConnections().get(0);
            assertEquals(ADDRESS, cnx.getconnection());
            assertEquals(PORT, cnx.getport());
            assertEquals(GUIDS[i].toString(), cnx.getid());
            assertEquals(IQNS[i], cnx.getiqn());
            assertEquals(StorageType.ISCSI, cnx.getstorage_type());
        }
    }
}
