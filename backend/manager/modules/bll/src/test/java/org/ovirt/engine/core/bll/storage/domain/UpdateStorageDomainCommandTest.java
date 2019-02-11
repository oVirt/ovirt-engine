package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

/**
 * A test case for the {@link UpdateStorageDomainCommand} class.
 */
@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateStorageDomainCommandTest extends BaseCommandTest {

    private Guid sdId = Guid.newGuid();
    private StorageDomain sd;
    private StoragePool sp;

    @Spy
    @InjectMocks
    private UpdateStorageDomainCommand<StorageDomainManagementParameter> cmd =
            new UpdateStorageDomainCommand<>(new StorageDomainManagementParameter(createStorageDomain()), null);

    private static final int STORAGE_DOMAIN_NAME_LENGTH_LIMIT = 100;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.StorageDomainNameSizeLimit,
                STORAGE_DOMAIN_NAME_LENGTH_LIMIT));
    }

    @Mock
    private StorageDomainStaticDao sdsDao;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @BeforeEach
    public void setUp() {
        StorageDomainStatic oldSdStatic = createStorageDomain();
        Guid spId = Guid.newGuid();

        sd = new StorageDomain();
        sd.setId(sdId);
        sd.setStorageStaticData(cmd.getParameters().getStorageDomain());
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStoragePoolId(spId);

        sp = new StoragePool();
        sp.setId(spId);
        sp.setStatus(StoragePoolStatus.Up);
        sp.setIsLocal(false);

        doReturn(sd).when(cmd).getStorageDomain();
        doReturn(sp).when(cmd).getStoragePool();

        when(sdsDao.get(sdId)).thenReturn(oldSdStatic);
        mockStorageDomainValidator();
        doReturn(Boolean.TRUE).when(cmd).isSystemSuperUser();
    }

    private StorageDomainStatic createStorageDomain() {
        StorageDomainStatic sd = new StorageDomainStatic();
        sd.setId(sdId);
        sd.setStorageName("newStorageDomain");
        sd.setComment("a storage domain for testing");
        sd.setDescription("a storage domain for testing");
        sd.setStorageDomainType(StorageDomainType.Data);
        sd.setStorageType(StorageType.NFS);
        sd.setStorageFormat(StorageFormatType.V3);
        return sd;
    }

    @Test
    public void validateSame() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void setActionMessageParameters() {
        cmd.setActionMessageParameters();
        List<String> messages = cmd.getReturnValue().getValidationMessages();
        assertTrue(messages.remove(EngineMessage.VAR__ACTION__UPDATE.name()), "action name not in messages");
        assertTrue(messages.remove(EngineMessage.VAR__TYPE__STORAGE__DOMAIN.name()), "type not in messages");
        assertTrue(messages.isEmpty(), "redundant messages " + messages);
    }

    @Test
    public void validateNoDomain() {
        doReturn(null).when(cmd).getStorageDomain();
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void validateWrongStatus() {
        sd.setStatus(StorageDomainStatus.Locked);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void validateChangeDescription() {
        sd.setDescription(StringUtils.reverse(sd.getDescription()));
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void validateChangeComment() {
        sd.setComment(StringUtils.reverse(sd.getComment()));
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void validateChangeForbiddenField() {
        sd.setStorageType(StorageType.UNKNOWN);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ERROR_CANNOT_CHANGE_STORAGE_DOMAIN_FIELDS);
    }

    @Test
    public void validateName() {
        sd.setStorageName(StringUtils.reverse(sd.getStorageName()));
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    @Test
    public void validateLongName() {
        // Generate a really long name
        String longName = StringUtils.leftPad("name", STORAGE_DOMAIN_NAME_LENGTH_LIMIT * 2, 'X');
        sd.setStorageName(longName);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
    }

    @Test
    public void validateChangeNamePoolNotUp() {
        sd.setStorageName(StringUtils.reverse(sd.getStorageName()));
        sp.setStatus(StoragePoolStatus.Maintenance);

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
    }

    @Test
    public void validateChangeNameExists() {
        String newName = StringUtils.reverse(sd.getStorageName());
        sd.setStorageName(newName);

        doReturn(new StorageDomainStatic()).when(sdsDao).getByName(newName);
        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
    }

    @Test
    public void validateFailsIllegalDiscardAfterDelete() {
        EngineMessage message =
                EngineMessage.ACTION_TYPE_FAILED_DISCARD_AFTER_DELETE_NOT_SUPPORTED_BY_UNDERLYING_STORAGE;
        when(storageDomainValidator.isDiscardAfterDeleteLegalForExistingStorageDomain())
                .thenReturn(new ValidationResult(message));
        ValidateTestUtils.runAndAssertValidateFailure(cmd, message);
    }

    @Test
    public void testUpdateFailOnHostedEngineSdWithNonSuperUser() {
        doReturn(Boolean.FALSE).when(cmd).isSystemSuperUser();
        sd.setHostedEngineStorage(true);
        ValidateTestUtils.runAndAssertValidateFailure(
                cmd, EngineMessage.NON_ADMIN_USER_NOT_AUTHORIZED_TO_PERFORM_ACTION_ON_HE);
    }

    private void mockStorageDomainValidator() {
        doReturn(storageDomainValidator).when(cmd).getStorageDomainValidator();
    }
}
