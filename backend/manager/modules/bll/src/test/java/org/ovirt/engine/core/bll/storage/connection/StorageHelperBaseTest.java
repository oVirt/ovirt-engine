package org.ovirt.engine.core.bll.storage.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;

public class StorageHelperBaseTest {

    @Test
    public void getLunConnectionsForFC() {
        LUNs lun = new LUNs();
        ArrayList<StorageServerConnections> connections = new ArrayList<>();
        lun.setLunConnections(connections);
        Map<StorageType, List<StorageServerConnections>> connectionsByType =
                StorageHelperBase.filterConnectionsByStorageType(lun);
        assertTrue("Map of storage connections should be empty.", connectionsByType.isEmpty());
    }

    @Test
    public void getLunConnectionsForISCSI() {
        LUNs lun = new LUNs();
        ArrayList<StorageServerConnections> connections = new ArrayList<>();
        connections.add(new StorageServerConnections("Some LUN connection",
                "id",
                "iqn",
                "password",
                StorageType.ISCSI,
                "Username",
                "port",
                "portal"));
        connections.add(new StorageServerConnections("Other LUN connection",
                "id",
                "iqn",
                "password",
                StorageType.ISCSI,
                "Username",
                "port",
                "portal"));

        lun.setLunConnections(connections);
        Map<StorageType, List<StorageServerConnections>> connectionsByType =
                StorageHelperBase.filterConnectionsByStorageType(lun);
        assertTrue("Map of ISCSI storage connections should not be empty.", !connectionsByType.isEmpty());
        assertEquals("Map of ISCSI storage connections should have only one type of connections.",
                1,
                connectionsByType.size());
        assertEquals("Map of ISCSI storage connections should have only 2 ISCSI connections.",
                2,
                connectionsByType.get(StorageType.ISCSI).size());
    }

    @Test
    public void getMixedLunConnections() {
        LUNs lun = new LUNs();
        ArrayList<StorageServerConnections> connections = new ArrayList<>();
        connections.add(new StorageServerConnections("Some LUN connection",
                "id",
                "iqn",
                "password",
                StorageType.ISCSI,
                "Username",
                "port",
                "portal"));
        // Connection for FCP is only for testing, since FCP should not have connections.
        connections.add(new StorageServerConnections("Other LUN connection",
                "id",
                "iqn",
                "password",
                StorageType.FCP,
                "Username",
                "port",
                "portal"));

        lun.setLunConnections(connections);
        Map<StorageType, List<StorageServerConnections>> connectionsByType =
                StorageHelperBase.filterConnectionsByStorageType(lun);
        assertTrue("Map of storage connections should not be empty.", !connectionsByType.isEmpty());
        assertEquals("Map of storage connections should have only two types of connections.",
                2,
                connectionsByType.size());
        assertEquals("Map of ISCSI storage connections should have only 1 ISCSI connections.",
                1,
                connectionsByType.get(StorageType.ISCSI).size());
        assertEquals("Map of FCP storage connections should have only 1 FCP connections.",
                1,
                connectionsByType.get(StorageType.FCP).size());
    }
}
