package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.TransferImageStatusParameters;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.lock.LockManager;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TransferImageStatusCommandTest extends BaseCommandTest {

    @Mock
    private ImageTransferDao imageTransferDao;

    @Mock
    private LockManager lockManager;

    @Spy
    @InjectMocks
    private ImageTransferUpdater imageTransferUpdater;

    @Spy
    @InjectMocks
    private TransferImageStatusCommand<TransferImageStatusParameters> command =
            new TransferImageStatusCommand<>(new TransferImageStatusParameters(), null);

    private static Guid diskId = Guid.newGuid();
    private static Guid storageDomainId = Guid.newGuid();

    @BeforeEach
    public void setUp() {
        ImageTransfer entity = new ImageTransfer(Guid.newGuid());
        imageTransferUpdater = Mockito.spy(new ImageTransferUpdater(imageTransferDao, lockManager));
        doReturn(entity).when(imageTransferUpdater).updateEntity(any(), eq(entity.getId()), eq(false));
        when(imageTransferDao.getByDiskId(diskId)).thenReturn(entity);
    }

    @Test
    void testBaseExecution() {
        command.getParameters().setStorageDomainId(storageDomainId);
        command.getParameters().setDiskId(diskId);
        List<PermissionSubject> subjects = command.getPermissionCheckSubjects();
        PermissionSubject subject = subjects.get(0);
        assertEquals(diskId, subject.getObjectId());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
        command.executeCommand();
    }

    @ParameterizedTest
    @MethodSource("commandCheckPermissionTestParameters")
    public void testCorrectPermissionCheckSubjects(Guid inputDiskId, Guid inputStorageDomainId, Guid expectedPermissionSubjectId) {
        command.getParameters().setStorageDomainId(inputStorageDomainId);
        command.getParameters().setDiskId(inputDiskId);

        List<PermissionSubject> subjects = command.getPermissionCheckSubjects();
        PermissionSubject subject = subjects.get(0);

        assertEquals(expectedPermissionSubjectId, subject.getObjectId());
    }

    private static Stream<Arguments> commandCheckPermissionTestParameters() {
        return Stream.of(
                Arguments.of(diskId, storageDomainId, diskId),
                Arguments.of(null, storageDomainId, storageDomainId),
                Arguments.of(null, null, MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID)
        );
    }
}
