package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class StorageServerConnectionExtensionValidatorTest {
    @Mock
    @InjectedMock
    public VdsDao vdsDao;

    @Mock
    @InjectedMock
    public StorageServerConnectionExtensionDao storageServerConnectionExtensionDao;

    @Spy
    private StorageServerConnectionExtensionValidator validator;

    private StorageServerConnectionExtension conn;

    @BeforeEach
    public void setup() {
        Guid hostId = Guid.newGuid();
        doReturn(new VDS()).when(vdsDao).get(hostId);

        conn = new StorageServerConnectionExtension();
        conn.setHostId(hostId);
        conn.setIqn("iqn1");
        conn.setUserName("user1");
        conn.setPassword("password1");
    }

    @Test
    public void testIsConnectionDoesNotExistForHostAndTargetSucceeds() {
        assertTrue(validator.isConnectionDoesNotExistForHostAndTarget(conn).isValid());
    }

    @Test
    public void testIsConnectionDoesNotExistForHostAndTargetFails() {
        when(storageServerConnectionExtensionDao.getByHostIdAndTarget(conn.getHostId(), conn.getIqn())).thenReturn(new StorageServerConnectionExtension());
        assertThat(validator.isConnectionDoesNotExistForHostAndTarget(conn), failsWith(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_EXTENSION_ALREADY_EXISTS));
    }
}
