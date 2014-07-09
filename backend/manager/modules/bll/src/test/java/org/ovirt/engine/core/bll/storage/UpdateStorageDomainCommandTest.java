package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.List;
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
    private StorageDomainStatic oldSdStatic;
    private StorageDomainStatic newSdStatic;
    private UpdateStorageDomainCommand<StorageDomainManagementParameter> cmd;

    @ClassRule
    public static MockConfigRule mcr =
            new MockConfigRule(mockConfig(ConfigValues.StorageDomainNameSizeLimit, 100));

    @Mock
    private StorageDomainStaticDAO sdsDao;

    @Before
    public void setUp() {
        sdId = Guid.newGuid();
        oldSdStatic = createStorageDomain();
        newSdStatic = createStorageDomain();

        sd = new StorageDomain();
        sd.setStorageStaticData(newSdStatic);
        sd.setStatus(StorageDomainStatus.Active);

        cmd = spy(new UpdateStorageDomainCommand<>(new StorageDomainManagementParameter(newSdStatic)));
        doReturn(sd).when(cmd).getStorageDomain();
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

}
