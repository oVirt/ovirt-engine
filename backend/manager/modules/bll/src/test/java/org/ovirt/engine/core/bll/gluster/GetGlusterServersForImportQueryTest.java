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
    private static final String PK1 = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDCyjmpkot5oNiMOElWAXUTwiItYDegil5efQHp4fTPuGsm3BBJbfVXMyCVXR8aVV+/B2keDvCUaClXq18cYzMFLbMschSBqmQfnveDdFg59hGxIOV4VzAAK7p2az/jnPWKqtNgZvxTe7PNsJ/2bCAIvlpCH5/GlXiuDjWJNBrOaO9RyeHz79KYEggq2LdDmMepioCdzo3xObVXO5DLRYFz2J7zRyqJbshLvtsq/fmdBSmQEjUqu5gEmoqyajgBpxpkCdLza/uP1bmVwmCmYGH14xybfY8ocmODx52LUY2BYjFNGTQJyU+QmpDB3PlU8HJJs/n6VlpL7agpCEqVEX+XXc3i1qp5Wte2EGP4/U3r73onkl2UkxW0oMm/Fgi9G7dhJTfDVTbsm6caTUpx+l2+nkrIY/DS4g/srFcCF2UEv7xgTw5BWgR2KASIE9yYcgM1Q1AMB9u5MAcB28T+dCr3zPF903y9CeNsAbm9edG/+gFIx/0A15EvX4ld4rS1qnE=";
    private static final String PK2 = "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBMGxcFCjUP16rF7Ovnxx0uBvO0jo0MHzaw3worb9pd1uIW6ZFhadQ/SKrzowTwIuWcmWH4uE0DTBh1//9GPUeHo=";

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

        EXPECTED_MAP.put(SERVER_NAME1, PK1);
        EXPECTED_MAP.put(SERVER_NAME2, PK2);
        doReturn(EXPECTED_MAP).when(glusterUtil).getPeersWithSshPublicKeys(NEW_SERVER, USER, PASSWORD);
        doThrow(AuthenticationException.class).when(glusterUtil).getPeersWithSshPublicKeys(NEW_SERVER, USER, WRONG_PASSWORD);
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
        doReturn(PK1).when(getQueryParameters()).getSshPublicKey();
    }

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
