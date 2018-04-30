package org.ovirt.engine.core.vdsbroker.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;

@ExtendWith(MockitoExtension.class)
public class StorageConnectionHelperTest {

    @Mock
    private StorageServerConnectionExtensionDao connExtDaoMock;

    @InjectMocks
    private StorageConnectionHelper helper;

    @Test
    public void testCredentialsWithNoConnectionExtension() {
        StorageServerConnections conn = createConnectionWithCredentials("target1");

        Pair<String, String> credentials = helper.getStorageConnectionCredentialsForhost(Guid.newGuid(), conn);
        assertCredentials(credentials, conn.getUserName(), conn.getPassword());
    }

    @Test
    public void testCredentialsWithConnectionExtension() {
        StorageServerConnections conn = createConnectionWithCredentials("target1");
        StorageServerConnectionExtension connExt = createConnectionExtension(Guid.newGuid());
        when(connExtDaoMock.getByHostIdAndTarget(connExt.getHostId(), connExt.getIqn())).thenReturn(connExt);

        Pair<String, String> credentials = helper.getStorageConnectionCredentialsForhost(connExt.getHostId(), conn);
        assertCredentials(credentials, connExt.getUserName(), connExt.getPassword());
    }

    @Test
    public void testCredentialsWithConnectionExtensionSameHostDifferentTarget() {
        StorageServerConnections conn = createConnectionWithCredentials("target2");
        StorageServerConnectionExtension connExt = createConnectionExtension(Guid.newGuid());

        Pair<String, String> credentials = helper.getStorageConnectionCredentialsForhost(connExt.getHostId(), conn);
        assertCredentials(credentials, conn.getUserName(), conn.getPassword());
    }

    @Test
    public void testCredentialsWithConnectionExtensionDifferentHostSameTarget() {
        StorageServerConnections conn = createConnectionWithCredentials("target1");
        StorageServerConnectionExtension connExt = createConnectionExtension(Guid.newGuid());

        Pair<String, String> credentials = helper.getStorageConnectionCredentialsForhost(connExt.getHostId(), conn);
        assertCredentials(credentials, conn.getUserName(), conn.getPassword());
    }

    private static StorageServerConnections createConnectionWithCredentials(String target) {
        StorageServerConnections conn = new StorageServerConnections();
        conn.setIqn(target);
        conn.setUserName("userConn");
        conn.setPassword("pwdConn");
        return conn;
    }

    private static StorageServerConnectionExtension createConnectionExtension(Guid hostId) {
        StorageServerConnectionExtension connExt = new StorageServerConnectionExtension();
        connExt.setHostId(hostId);
        connExt.setIqn("target1");
        connExt.setUserName("userConnExt");
        connExt.setPassword("pwdConnExt");
        return connExt;
    }

    private static void assertCredentials(Pair<String, String> credentials, String userName, String password) {
        assertEquals(userName, credentials.getFirst());
        assertEquals(password, credentials.getSecond());
    }
}
