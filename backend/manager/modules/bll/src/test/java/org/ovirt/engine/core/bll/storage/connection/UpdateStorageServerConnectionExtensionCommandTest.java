package org.ovirt.engine.core.bll.storage.connection;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageServerConnectionExtensionValidator;
import org.ovirt.engine.core.common.action.StorageServerConnectionExtensionParameters;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class UpdateStorageServerConnectionExtensionCommandTest {

    @Mock
    private StorageServerConnectionExtensionValidator storageServerConnectionExtensionValidator;

    @Test
    public void testUpdateFailsOnExistingHostAndTargetCombination() {
        Guid hostId = Guid.newGuid();
        StorageServerConnectionExtension conn = createConnection(hostId, "iqn1", "user", "pass");

        StorageServerConnectionExtensionParameters params = new StorageServerConnectionExtensionParameters(conn);
        UpdateStorageServerConnectionExtensionCommand realCmd =
                new UpdateStorageServerConnectionExtensionCommand(params, null);
        UpdateStorageServerConnectionExtensionCommand cmd = spy(realCmd);
        when(cmd.getConnectionExtensionValidator()).thenReturn(storageServerConnectionExtensionValidator);

        when(storageServerConnectionExtensionValidator.isConnectionDoesNotExistForHostAndTarget(conn)).thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_EXTENSION_ALREADY_EXISTS));

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_EXTENSION_ALREADY_EXISTS);

    }

    private StorageServerConnectionExtension createConnection(Guid hostId, String iqn, String userName, String password) {
        StorageServerConnectionExtension conn = new StorageServerConnectionExtension();
        conn.setHostId(hostId);
        conn.setIqn(iqn);
        conn.setUserName(userName);
        conn.setPassword(password);
        return conn;
    }
}
