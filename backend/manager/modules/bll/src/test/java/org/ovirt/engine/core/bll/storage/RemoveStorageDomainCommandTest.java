package org.ovirt.engine.core.bll.storage;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

/** A test case for the {@link RemoveStorageDomainCommand} */
@RunWith(MockitoJUnitRunner.class)
public class RemoveStorageDomainCommandTest {
    @ClassRule
    public static MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule();

    private RemoveStorageDomainCommand<RemoveStorageDomainParameters> command;

    @Mock
    private StorageDomainDAO storageDomainDAOMock;

    @Mock
    private StoragePoolDAO storagePoolDAOMock;

    @Mock
    private StoragePoolIsoMapDAO storagePoolIsoMapDAOMock;

    @Mock
    private VdsDAO vdsDAOMock;

    private StorageDomain storageDomain;

    @Before
    public void setUp() {
        Guid storageDomainID = Guid.newGuid();
        storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainID);
        storageDomain.setStatus(StorageDomainStatus.Maintenance);

        Guid vdsID = Guid.newGuid();
        VDS vds = new VDS();
        vds.setId(vdsID);

        RemoveStorageDomainParameters params = new RemoveStorageDomainParameters();
        params.setVdsId(vdsID);
        params.setStorageDomainId(storageDomainID);
        params.setDoFormat(true);

        command = spy(new RemoveStorageDomainCommand<>(params));

        doReturn(storageDomainDAOMock).when(command).getStorageDomainDAO();
        doReturn(storageDomain).when(storageDomainDAOMock).get(storageDomainID);
        doReturn(Collections.singletonList(storageDomain)).when(storageDomainDAOMock).getAllForStorageDomain(storageDomainID);

        doReturn(storagePoolIsoMapDAOMock).when(command).getStoragePoolIsoMapDAO();
        doReturn(Collections.emptyList()).when(storagePoolIsoMapDAOMock).getAllForStorage(storageDomainID);

        doReturn(vdsDAOMock).when(command).getVdsDAO();
        doReturn(vds).when(vdsDAOMock).get(vdsID);

        StorageDomainToPoolRelationValidatorTesting domainToPoolValidator = spy(new StorageDomainToPoolRelationValidatorTesting(storageDomain, null));
        doReturn(storagePoolIsoMapDAOMock).when(domainToPoolValidator).getStoragePoolIsoMapDao();
        doReturn(domainToPoolValidator).when(command).createDomainToPoolValidator(storageDomain);
        doReturn(Boolean.FALSE).when(command).isStorageDomainAttached(storageDomain);
    }

    @Test
    public void testCanDoActionNonExistingStorageDomain() {
        doReturn(null).when(storageDomainDAOMock).get(storageDomain.getId());
        doReturn(Collections.emptyList()).when(storageDomainDAOMock).getAllForStorageDomain(storageDomain.getId());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(
                "canDoAction shouldn't be possible for a non-existent storage domain",
                command, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testCanDoActionSuccess() {
        storageDomain.setStorageType(StorageType.NFS);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void testCanDoActionWithAttachedStorageDomain() {
        storageDomain.setStorageType(StorageType.NFS);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        doReturn(Boolean.TRUE).when(command).isStorageDomainAttached(storageDomain);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(
                "canDoAction shouldn't be possible for an attached storage domain",
                command, VdcBllMessages.ACTION_TYPE_FAILED_FORMAT_STORAGE_DOMAIN_WITH_ATTACHED_DATA_DOMAIN);
    }

    @Test
    public void testCanDoActionWithAttachedStorageDomainAndNoFormat() {
        storageDomain.setStorageType(StorageType.NFS);
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        doReturn(Boolean.TRUE).when(command).isStorageDomainAttached(storageDomain);
        command.getParameters().setDoFormat(false);
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void testSetActionMessageParameters() {
        CanDoActionTestUtils.runAndAssertSetActionMessageParameters(command,
                VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN,
                VdcBllMessages.VAR__ACTION__REMOVE);
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
        when(helper.connectStorageToDomainByVdsIdDetails(storageDomain, command.getParameters().getVdsId())).thenReturn(new Pair<Boolean, VdcFault>(true, null));
        when(helper.disconnectStorageFromDomainByVdsId(storageDomain, command.getParameters().getVdsId())).thenReturn(true);
        doReturn(helper).when(command).getStorageHelper(storageDomain);
    }

    protected void setUpFormatDomain(boolean shouldFail) {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(!shouldFail);
        doReturn(ret).when(command).runVdsCommand
                (eq(VDSCommandType.FormatStorageDomain), any(FormatStorageDomainVDSCommandParameters.class));
    }

    protected class StorageDomainToPoolRelationValidatorTesting extends StorageDomainToPoolRelationValidator {
        public StorageDomainToPoolRelationValidatorTesting(StorageDomain domain,
                StoragePool pool) {
            super(domain.getStorageStaticData(), pool);
        }

        public StoragePoolIsoMapDAO getStoragePoolIsoMapDao() {
            return DbFacade.getInstance().getStoragePoolIsoMapDao();
        }
    }
}
