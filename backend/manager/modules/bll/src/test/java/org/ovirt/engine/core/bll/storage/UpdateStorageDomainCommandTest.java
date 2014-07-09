package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

/**
 * A test case for the {@link UpdateStorageDomainCommand} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateStorageDomainCommandTest {
    private Guid sdId;
    private StorageDomain sd;
    private StoragePool sp;
    private StorageDomainStatic oldSdStatic;
    private StorageDomainStatic newSdStatic;
    private UpdateStorageDomainCommand<StorageDomainManagementParameter> cmd;

    private static final int STORAGE_DOMAIN_NAME_LENGTH_LIMIT = 100;

    @ClassRule
    public static MockConfigRule mcr =
            new MockConfigRule(mockConfig(ConfigValues.StorageDomainNameSizeLimit, STORAGE_DOMAIN_NAME_LENGTH_LIMIT));

    @Mock
    private StorageDomainStaticDAO sdsDao;

    @Before
    public void setUp() {
        sdId = Guid.newGuid();
        oldSdStatic = createStorageDomain();
        newSdStatic = createStorageDomain();
        Guid spId = Guid.newGuid();

        sd = new StorageDomain();
        sd.setStorageStaticData(newSdStatic);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setStoragePoolId(spId);

        sp = new StoragePool();
        sp.setId(spId);
        sp.setStatus(StoragePoolStatus.Up);
        sp.setIsLocal(false);

        cmd = spy(new UpdateStorageDomainCommand<>(new StorageDomainManagementParameter(newSdStatic)));
        doReturn(sd).when(cmd).getStorageDomain();
        doReturn(sp).when(cmd).getStoragePool();
        doReturn(sdsDao).when(cmd).getStorageDomainStaticDAO();

        when(sdsDao.get(sdId)).thenReturn(oldSdStatic);
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
    public void canDoActionSame() {
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void setActionMessageParameters() {
        cmd.setActionMessageParameters();
        List<String> messages = cmd.getReturnValue().getCanDoActionMessages();
        assertTrue("action name not in messages", messages.remove(VdcBllMessages.VAR__ACTION__UPDATE.name()));
        assertTrue("type not in messages", messages.remove(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN.name()));
        assertTrue("redundant messages " + messages, messages.isEmpty());
    }

    @Test
    public void canDoActionNoDomain() {
        doReturn(null).when(cmd).getStorageDomain();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void canDoActionWrongStatus() {
        sd.setStatus(StorageDomainStatus.Maintenance);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
    }

    @Test
    public void canDoChangeDescription() {
        sd.setDescription(StringUtils.reverse(sd.getDescription()));
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void canDoChangeComment() {
        sd.setComment(StringUtils.reverse(sd.getComment()));
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void canDoChangeForbiddenField() {
        sd.setStorageType(StorageType.UNKNOWN);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ERROR_CANNOT_CHANGE_STORAGE_DOMAIN_FIELDS);
    }

    @Test
    public void canDoActionName() {
        sd.setStorageName(StringUtils.reverse(sd.getStorageName()));
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(cmd);
    }

    @Test
    public void canDoActionLongName() {
        // Generate a really long name
        String longName = StringUtils.leftPad("name", STORAGE_DOMAIN_NAME_LENGTH_LIMIT * 2, 'X');
        sd.setStorageName(longName);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
    }

    @Test
    public void canDoChangeNamePoolNotUp() {
        sd.setStorageName(StringUtils.reverse(sd.getStorageName()));
        sp.setStatus(StoragePoolStatus.Maintenance);

        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
    }

    @Test
    public void canDoChangeNameExists() {
        String newName = StringUtils.reverse(sd.getStorageName());
        sd.setStorageName(newName);

        doReturn(new StorageDomainStatic()).when(sdsDao).getByName(newName);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
    }
}
