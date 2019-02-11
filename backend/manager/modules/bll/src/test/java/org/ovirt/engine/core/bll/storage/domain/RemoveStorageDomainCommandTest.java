package org.ovirt.engine.core.bll.storage.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.connection.IStorageHelper;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;

/** A test case for the {@link RemoveStorageDomainCommand} */
@MockitoSettings(strictness = Strictness.LENIENT)
public class RemoveStorageDomainCommandTest extends BaseCommandTest {

    @Spy
    @InjectMocks
    private RemoveStorageDomainCommand<RemoveStorageDomainParameters> command =
            new RemoveStorageDomainCommand<>(new RemoveStorageDomainParameters(Guid.newGuid()), null);

    @Mock
    private StorageDomainDao storageDomainDaoMock;

    @Mock
    private VdsDao vdsDaoMock;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    private StorageDomain storageDomain;

    @BeforeEach
    public void setUp() {
        Guid storageDomainID = command.getStorageDomainId();
        storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainID);
        storageDomain.setStatus(StorageDomainStatus.Maintenance);

        Guid vdsID = Guid.newGuid();
        VDS vds = new VDS();
        vds.setId(vdsID);

        command.setVdsId(vdsID);
        command.getParameters().setVdsId(vdsID);
        command.getParameters().setDoFormat(true);
        command.init();

        doReturn(storageDomain).when(storageDomainDaoMock).get(storageDomainID);
        doReturn(Collections.singletonList(storageDomain)).when(storageDomainDaoMock).getAllForStorageDomain(storageDomainID);

        doReturn(vds).when(vdsDaoMock).get(vdsID);

        StorageDomainToPoolRelationValidator domainToPoolValidator = spy(new StorageDomainToPoolRelationValidator(storageDomain.getStorageStaticData(), null));
        doReturn(ValidationResult.VALID).when(domainToPoolValidator).isStorageDomainNotInAnyPool();
        doReturn(domainToPoolValidator).when(command).createDomainToPoolValidator(storageDomain);
        doReturn(Boolean.FALSE).when(command).isStorageDomainAttached(storageDomain);
        doReturn(Boolean.TRUE).when(command).isSystemSuperUser();
    }

    @Test
    public void testValidateNonExistingStorageDomain() {
        doReturn(Collections.emptyList()).when(storageDomainDaoMock).getAllForStorageDomain(storageDomain.getId());
        ValidateTestUtils.runAndAssertValidateFailure(
                "validate shouldn't be possible for a non-existent storage domain",
                command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testValidateSuccess() {
        storageDomain.setStorageType(StorageType.NFS);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        setVdsStatus(VDSStatus.Up);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidateHostNotUpFailure() {
        storageDomain.setStorageType(StorageType.NFS);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        setVdsStatus(VDSStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.CANNOT_REMOVE_STORAGE_DOMAIN_HOST_NOT_UP);
    }

    @Test
    public void testValidateWithAttachedStorageDomain() {
        storageDomain.setStorageType(StorageType.NFS);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        doReturn(Boolean.TRUE).when(command).isStorageDomainAttached(storageDomain);
        setVdsStatus(VDSStatus.Up);
        ValidateTestUtils.runAndAssertValidateFailure(
                "validate shouldn't be possible for an attached storage domain",
                command, EngineMessage.ACTION_TYPE_FAILED_FORMAT_STORAGE_DOMAIN_WITH_ATTACHED_DATA_DOMAIN);
    }

    @Test
    public void testValidateWithAttachedStorageDomainAndNoFormat() {
        storageDomain.setStorageType(StorageType.NFS);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        doReturn(Boolean.TRUE).when(command).isStorageDomainAttached(storageDomain);
        command.getParameters().setDoFormat(false);
        setVdsStatus(VDSStatus.Up);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testSetActionMessageParameters() {
        ValidateTestUtils.runAndAssertSetActionMessageParameters(command,
                EngineMessage.VAR__TYPE__STORAGE__DOMAIN,
                EngineMessage.VAR__ACTION__REMOVE);
    }

    @Test
    public void testRemove() {
        for (boolean shouldFormat : new boolean[] { true, false }) {
            for (StorageDomainType sdType : new StorageDomainType[] { StorageDomainType.Data, StorageDomainType.ISO,
                    StorageDomainType.ImportExport }) {
                for (StorageType sType : StorageType.values()) {
                    if (sType.isConcreteStorageType()) {
                        doTestRemove(sdType, sType, shouldFormat, false);
                    }
                }

            }
        }
    }

    public void doTestRemove
            (StorageDomainType type, StorageType storageType, boolean shouldFormat, boolean shouldFormatFail) {
        command.getParameters().setDoFormat(shouldFormat);
        storageDomain.setStorageDomainType(type);
        storageDomain.setStorageType(storageType);

        setUpStorageHelper();
        if (shouldFormat || type.isDataDomain()) {
            setUpFormatDomain(shouldFormatFail);
        }

        command.executeCommand();

        CommandAssertUtils.checkSucceeded(command, !shouldFormatFail);
    }

    private void setUpStorageHelper() {
        IStorageHelper helper = mock(IStorageHelper.class);
        when(helper.connectStorageToDomainByVdsIdDetails(storageDomain, command.getParameters().getVdsId())).thenReturn(new Pair<>(true, null));
        when(helper.disconnectStorageFromDomainByVdsId(storageDomain, command.getParameters().getVdsId())).thenReturn(true);
        doReturn(helper).when(command).getStorageHelper(storageDomain);
    }

    protected void setUpFormatDomain(boolean shouldFail) {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(!shouldFail);
        when(vdsBrokerFrontend.runVdsCommand(eq(VDSCommandType.FormatStorageDomain), any())).thenReturn(ret);
    }

    private void setVdsStatus(VDSStatus status) {
        VDS vds = vdsDaoMock.get(command.getVdsId());
        vds.setStatus(status);
        command.setVds(vds);
    }
}
