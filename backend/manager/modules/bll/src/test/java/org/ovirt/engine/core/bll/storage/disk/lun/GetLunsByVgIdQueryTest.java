package org.ovirt.engine.core.bll.storage.disk.lun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetLunsByVgIdQueryTest extends AbstractQueryTest<GetLunsByVgIdParameters, GetLunsByVgIdQuery<? extends GetLunsByVgIdParameters>> {
    private static final String[] GUIDS = {
            "11111111-1111-1111-1111-111111111111",
            "22222222-2222-2222-2222-222222222222",
            "33333333-3333-3333-3333-333333333333"
    };

    private static final String VG_ID = Guid.newGuid().toString();
    private static final Guid VDS_ID = Guid.newGuid();

    private static final String ADDRESS = "foo.bar.com";
    private static final String PORT = "123456";
    private static final String[] IQNS = { ADDRESS + ":1", ADDRESS + ":2", ADDRESS + ":3" };
    private static final String PHYSICAL_DEVICE_FIELD = "sda";
    private static final String DUMMY_LUN_ID = BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX+"89871115-e64d-4754-bacd-556cc249761b";
    @Mock
    private VDSBrokerFrontend vdsBrokerFrontendMock;
    @Mock
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;
    @Mock
    private StorageServerConnectionDao storageServerConnectionDao;
    @Mock
    private LunDao lunDao;

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
        when(getQueryParameters().getId()).thenReturn(VDS_ID);

        expectGetLunsForVg(VG_ID, withDummyLun);
        expectGetDeviceList();

        expectGetLunsMap();
        expectGetConnections();

        getQuery().setInternalExecution(true);
        getQuery().executeQueryCommand();

        checkReturnValue();
    }

    private void expectGetLunsForVg(String vgId, boolean withDummyLun) {
        when(lunDao.getAllForVolumeGroup(vgId)).thenReturn(setUpLuns(withDummyLun));
    }

    private void expectGetDeviceList() {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(setUpLunsFromDeviceList());
        when(vdsBrokerFrontendMock.runVdsCommand(eq(VDSCommandType.GetDeviceList), any())).thenReturn(returnValue);
    }

    private void expectGetLunsMap() {
        for (String GUID : GUIDS) {
            expectGetLunsMap(GUID, GUID);
        }
    }

    private void expectGetLunsMap(String lunId, String cnxId) {
        List<LUNStorageServerConnectionMap> ret = new ArrayList<>();
        LUNStorageServerConnectionMap map = new LUNStorageServerConnectionMap();
        map.setLunId(lunId);
        map.setStorageServerConnection(cnxId);
        ret.add(map);
        when(storageServerConnectionLunMapDao.getAll(lunId)).thenReturn(ret);
    }

    private void expectGetConnections() {
        for (int i = 0; i < GUIDS.length; i++) {
            when(storageServerConnectionDao.get(GUIDS[i])).thenReturn(setUpConnection(i));
        }
    }

    private static List<LUNs> setUpLuns(boolean withDummyLun) {
        List<LUNs> luns = new ArrayList<>();
        for (String GUID : GUIDS) {
            LUNs lun = new LUNs();
            lun.setLUNId(GUID);
            luns.add(lun);
        }
        if (withDummyLun) {
            LUNs dummyLun = new LUNs();
            dummyLun.setLUNId(DUMMY_LUN_ID);
            luns.add(dummyLun);
        }
        return luns;
    }

    private static List<LUNs> setUpLunsFromDeviceList() {
        List<LUNs> luns = setUpLuns(false);
        luns.forEach(l -> l.setPathsDictionary(Collections.singletonMap(PHYSICAL_DEVICE_FIELD, true)));
        return luns;
    }

    private static StorageServerConnections setUpConnection(int idx) {
        return new StorageServerConnections(ADDRESS, GUIDS[idx], IQNS[idx], null,
                StorageType.ISCSI, null, PORT, null);
    }

    @SuppressWarnings("unchecked")
    private void checkReturnValue() {
        assertNotNull(getQuery().getQueryReturnValue().getReturnValue());
        List<LUNs> luns = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(GUIDS.length, luns.size());
        for (int i = 0; i < GUIDS.length; i++) {
            LUNs lun = luns.get(i);
            assertNotNull(lun);
            assertEquals(GUIDS[i], lun.getLUNId());
            assertNotNull(lun.getLunConnections());
            assertEquals(1, lun.getLunConnections().size());
            StorageServerConnections cnx = lun.getLunConnections().get(0);
            assertEquals(ADDRESS, cnx.getConnection());
            assertEquals(PORT, cnx.getPort());
            assertEquals(GUIDS[i], cnx.getId());
            assertEquals(IQNS[i], cnx.getIqn());
            assertEquals(StorageType.ISCSI, cnx.getStorageType());
            assertNotNull(lun.getPathsDictionary());
            assertEquals(1, lun.getPathsDictionary().size());
        }
    }
}
