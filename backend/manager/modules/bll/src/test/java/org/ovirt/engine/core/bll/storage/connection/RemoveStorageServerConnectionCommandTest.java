package org.ovirt.engine.core.bll.storage.connection;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public class RemoveStorageServerConnectionCommandTest extends BaseCommandTest {
    private static final StorageServerConnections NFSConnection = createNFSConnection(
            "multipass.my.domain.tlv.company.com:/export/allstorage/data1",
            StorageType.NFS,
            NfsVersion.V4,
            50,
            0);

    private StorageServerConnections iSCSIConnection = createIscsiConnection(
            "10.11.12.225",
            StorageType.ISCSI,
            "iqn.2013-04.myhat.com:abc-target1",
            "user1",
            "mypassword",
            "1");

    @Mock
    private LunDao lunDao;

    @Mock
    private StorageServerConnectionDao storageServerConnectionDao;

    private StorageServerConnectionParametersBase parameters =
            new StorageServerConnectionParametersBase(null, Guid.newGuid(), false);

    @Spy
    @InjectMocks
    private RemoveStorageServerConnectionCommand<StorageServerConnectionParametersBase> command = prepareCommand();

    private RemoveStorageServerConnectionCommand<StorageServerConnectionParametersBase> prepareCommand() {
        return new RemoveStorageServerConnectionCommand<>(parameters, null);
    }

    private static StorageServerConnections createNFSConnection(String connection,
            StorageType type,
            NfsVersion version,
            int timeout,
            int retrans) {
        Guid id = Guid.newGuid();
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setNfsVersion(version);
        connectionDetails.setNfsTimeo((short) timeout);
        connectionDetails.setNfsRetrans((short) retrans);
        return connectionDetails;
    }

    private static StorageServerConnections createIscsiConnection(String connection,
            StorageType type,
            String iqn,
            String userName,
            String password,
            String portal
            ) {
        Guid id = Guid.newGuid();
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setIqn(iqn);
        connectionDetails.setUserName(userName);
        connectionDetails.setPassword(password);
        connectionDetails.setPortal(portal);
        return connectionDetails;
    }

    private static StorageServerConnections populateBasicConnectionDetails(Guid id, String connection, StorageType type) {
        StorageServerConnections connectionDetails = new StorageServerConnections();
        connectionDetails.setId(id.toString());
        connectionDetails.setConnection(connection);
        connectionDetails.setStorageType(type);
        return connectionDetails;
    }

    @Test
    public void checkRemoveConnectionEmptyId() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        newNFSConnection.setId("");
        parameters.setStorageServerConnection(newNFSConnection);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ID_EMPTY);
    }

    @Test
    public void checkRemoveNotExistingConnection() {
        parameters.setStorageServerConnection(NFSConnection);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void checkRemoveNFSConnectionDomainsExist() {
        parameters.setStorageServerConnection(NFSConnection);
        when(storageServerConnectionDao.get(NFSConnection.getId())).thenReturn(NFSConnection);
        List<StorageDomain> domains = new ArrayList<>();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(NFSConnection.getConnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domain1.setStorageName("domain1");
        StorageDomain domain2 = new StorageDomain();
        domain2.setStorage(NFSConnection.getConnection());
        domain2.setStatus(StorageDomainStatus.Maintenance);
        domain2.setStorageName("domain2");
        domains.add(domain1);
        domains.add(domain2);
        doReturn(domains).when(command).getStorageDomainsByConnId(NFSConnection.getId());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }

    @Test
    public void checkRemoveNFSConnectionNoDomain() {
        parameters.setStorageServerConnection(NFSConnection);
        when(storageServerConnectionDao.get(NFSConnection.getId())).thenReturn(NFSConnection);
        List<StorageDomain> domains = new ArrayList<>();
        doReturn(domains).when(command).getStorageDomainsByConnId(NFSConnection.getId());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void checkRemoveIscsiConnectionDomainsExist() {
        parameters.setStorageServerConnection(iSCSIConnection);
        when(storageServerConnectionDao.get(iSCSIConnection.getId())).thenReturn(iSCSIConnection);
        List<LUNs> luns = new ArrayList<>();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("3600144f09dbd05000000517e730b1212");
        lun1.setStorageDomainName("storagedomain1");
        lun1.setVolumeGroupId("G95OWd-Wvck-vftu-pMq9-9SAC-NF3E-ulDPsQ");
        luns.add(lun1);
        when(lunDao.getAllForStorageServerConnection(iSCSIConnection.getId())).thenReturn(luns);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }

    @Test
    public void checkRemoveIscsiConnectionDomainsAndDisksExist() {
        parameters.setStorageServerConnection(iSCSIConnection);
        when(storageServerConnectionDao.get(iSCSIConnection.getId())).thenReturn(iSCSIConnection);
        List<LUNs> luns = new ArrayList<>();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("3600144f09dbd05000000517e730b1212");
        lun1.setStorageDomainName("storagedomain1");
        lun1.setVolumeGroupId("G95OWd-Wvck-vftu-pMq9-9SAC-NF3E-ulDPsQ");
        luns.add(lun1);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("3600144f09dbd05000000517e730b1212");
        lun2.setStorageDomainName("");
        lun2.setVolumeGroupId("");
        lun2.setDiskAlias("disk2");
        luns.add(lun2);
        when(lunDao.getAllForStorageServerConnection(iSCSIConnection.getId())).thenReturn(luns);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS_AND_DISKS);
    }

    @Test
    public void checkRemoveIscsiConnectionDisksExist() {
        parameters.setStorageServerConnection(iSCSIConnection);
        when(storageServerConnectionDao.get(iSCSIConnection.getId())).thenReturn(iSCSIConnection);
        List<LUNs> luns = new ArrayList<>();
        LUNs lun1 = new LUNs();
        lun1.setLUNId("3600144f09dbd05000000517e730b1212");
        lun1.setStorageDomainName("storagedomain1");
        lun1.setVolumeGroupId("");
        lun1.setDiskAlias("disk1");
        luns.add(lun1);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("3600144f09dbd05000000517e730b1212");
        lun2.setStorageDomainName("storagedomain4");
        lun2.setVolumeGroupId("");
        lun2.setDiskAlias("disk2");
        luns.add(lun2);
        when(lunDao.getAllForStorageServerConnection(iSCSIConnection.getId())).thenReturn(luns);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_DISKS);
    }

    @Test
    public void checkExecuteCommandWithVdsId() {
        parameters.setStorageServerConnection(NFSConnection);
        doReturn(true).when(command).disconnectStorage();
        command.executeCommand();
    }

    @Test
    public void checkExecuteCommandWithEmptyVdsId() {
        parameters.setStorageServerConnection(NFSConnection);
        parameters.setVdsId(Guid.Empty);
        // Test will fail if we try to disconnect
        command.executeCommand();
        verify(command, never()).disconnectStorage();
    }

    @Test
    public void checkExecuteCommandWithNullVdsId() {
        parameters.setStorageServerConnection(NFSConnection);
        parameters.setVdsId(null);
        // Test will fail if we try to disconnect
        command.executeCommand();
        verify(command, never()).disconnectStorage();
    }
}
