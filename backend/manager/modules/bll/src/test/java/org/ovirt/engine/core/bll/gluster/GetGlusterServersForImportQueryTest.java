package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.AuthenticationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.gluster.GlusterServersQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetGlusterServersForImportQueryTest extends AbstractQueryTest<GlusterServersQueryParameters, GetGlusterServersForImportQuery<GlusterServersQueryParameters>> {
    private static final String SERVER_NAME1 = "testserver1";
    private static final String SERVER_NAME2 = "testserver2";
    private static final String NEW_SERVER = "testserver3";
    private static final String EXISTING_SERVER = "testserver4";
    private static final String PASSWORD = "password";
    private static final String USER = "root";
    private static final String WRONG_PASSWORD = "wrong_password";
    private static final Map<String, String> EXPECTED_MAP = new HashMap<>();
    private static final String FINGER_PRINT1 = "31:e2:1b:7e:89:86:99:c3:f7:1e:57:35:fe:9b:5c:31";
    private static final String FINGER_PRINT2 = "31:e2:1b:7e:89:86:99:c3:f7:1e:57:35:fe:9b:5c:32";

    @Mock
    private VdsStaticDao vdsStaticDao;

    @Mock
    private GlusterUtil glusterUtil;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupMock();
    }

    private void setupMock() throws AuthenticationException, IOException {
        doReturn(getVdsStatic()).when(vdsStaticDao).getByHostName(EXISTING_SERVER);

        EXPECTED_MAP.put(SERVER_NAME1, FINGER_PRINT1);
        EXPECTED_MAP.put(SERVER_NAME2, FINGER_PRINT2);
        doReturn(EXPECTED_MAP).when(glusterUtil).getPeers(NEW_SERVER, USER, PASSWORD, FINGER_PRINT1);
        doThrow(AuthenticationException.class).when(glusterUtil).getPeers(NEW_SERVER, USER, WRONG_PASSWORD, FINGER_PRINT1);
    }

    private VdsStatic getVdsStatic() {
        VdsStatic vds = new VdsStatic();
        vds.setId(Guid.newGuid());
        vds.setHostName(NEW_SERVER);
        return vds;
    }

    private void mockQueryParameters(String server, String password) {
        doReturn(server).when(getQueryParameters()).getServerName();
        doReturn(password).when(getQueryParameters()).getPassword();
        doReturn(FINGER_PRINT1).when(getQueryParameters()).getFingerprint();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testQuerySuccess() {
        mockQueryParameters(NEW_SERVER, PASSWORD);

        getQuery().executeQueryCommand();

        Map<String, String> serverFingerprintMap = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(serverFingerprintMap);
        assertEquals(EXPECTED_MAP, serverFingerprintMap);
    }

    @Test
    public void testQueryFailsIfServerExists() {
        mockQueryParameters(EXISTING_SERVER, PASSWORD);
        Exception e = assertThrows(Exception.class, () -> getQuery().executeQueryCommand());
        assertEquals(EngineMessage.SERVER_ALREADY_EXISTS_IN_ANOTHER_CLUSTER.toString(), e.getMessage());
    }

    @Test
    public void testQueryFailsIfPeerExists() {
        mockQueryParameters(NEW_SERVER, PASSWORD);
        doReturn(getVdsStatic()).when(vdsStaticDao).getByHostName(SERVER_NAME1);
        Exception e = assertThrows(Exception.class, () -> getQuery().executeQueryCommand());
        assertEquals(EngineMessage.SERVER_ALREADY_EXISTS_IN_ANOTHER_CLUSTER.toString(), e.getMessage());
    }

    @Test
    public void testQueryFailsIfWrongPassword() {
        mockQueryParameters(NEW_SERVER, WRONG_PASSWORD);
        Exception e = assertThrows(Exception.class, () -> getQuery().executeQueryCommand());
        assertEquals(EngineMessage.SSH_AUTHENTICATION_FAILED.toString(), e.getMessage());
    }
}
