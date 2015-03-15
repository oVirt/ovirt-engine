package org.ovirt.engine.core.bll.storage;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.CanDoActionTestUtils;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class RemoveStorageServerConnectionCommandTest {
    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule();

    private RemoveStorageServerConnectionCommand command = null;

    private StorageServerConnections NFSConnection = null;
    private StorageServerConnections iSCSIConnection = null;

    @Mock
    private LunDAO lunDAO;

    @Mock
    private StorageServerConnectionDAO storageServerConnectionDAO;

    private StorageServerConnectionParametersBase parameters;

    @Before
    public void prepareParams() {

        NFSConnection =
                createNFSConnection(
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.NFS,
                        NfsVersion.V4,
                        50,
                        0);

        iSCSIConnection =
                createIscsiConnection(
                        "10.11.12.225",
                        StorageType.ISCSI,
                        "iqn.2013-04.myhat.com:abc-target1",
                        "user1",
                        "mypassword",
                        "1");

        prepareCommand();
    }

    private void prepareCommand() {
        parameters = new StorageServerConnectionParametersBase();
        parameters.setVdsId(Guid.newGuid());

        command = spy(new RemoveStorageServerConnectionCommand(parameters));
        doReturn(lunDAO).when(command).getLunDao();
        doReturn(storageServerConnectionDAO).when(command).getStorageServerConnectionDao();
    }

    private StorageServerConnections createNFSConnection(String connection,
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

    private StorageServerConnections createIscsiConnection(String connection,
            StorageType type,
            String iqn,
            String userName,
            String password,
            String portal
            ) {
        Guid id = Guid.newGuid();
        StorageServerConnections connectionDetails = populateBasicConnectionDetails(id, connection, type);
        connectionDetails.setiqn(iqn);
        connectionDetails.setuser_name(userName);
        connectionDetails.setpassword(password);
        connectionDetails.setportal(portal);
        return connectionDetails;
    }

    private StorageServerConnections populateBasicConnectionDetails(Guid id, String connection, StorageType type) {
        StorageServerConnections connectionDetails = new StorageServerConnections();
        connectionDetails.setid(id.toString());
        connectionDetails.setconnection(connection);
        connectionDetails.setstorage_type(type);
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
        newNFSConnection.setid("");
        parameters.setStorageServerConnection(newNFSConnection);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ID_EMPTY);
    }

    @Test
    public void checkRemoveNotExistingConnection() {
        parameters.setStorageServerConnection(NFSConnection);
        when(storageServerConnectionDAO.get(NFSConnection.getid())).thenReturn(null);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void checkRemoveNFSConnectionDomainsExist() {
        parameters.setStorageServerConnection(NFSConnection);
        when(storageServerConnectionDAO.get(NFSConnection.getid())).thenReturn(NFSConnection);
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(NFSConnection.getconnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domain1.setStorageName("domain1");
        StorageDomain domain2 = new StorageDomain();
        domain2.setStorage(NFSConnection.getconnection());
        domain2.setStatus(StorageDomainStatus.Maintenance);
        domain2.setStorageName("domain2");
        domains.add(domain1);
        domains.add(domain2);
        doReturn(domains).when(command).getStorageDomainsByConnId(NFSConnection.getid());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }

    @Test
    public void checkRemoveNFSConnectionNoDomain() {
        parameters.setStorageServerConnection(NFSConnection);
        when(storageServerConnectionDAO.get(NFSConnection.getid())).thenReturn(NFSConnection);
        List<StorageDomain> domains = new ArrayList<StorageDomain>();
        doReturn(domains).when(command).getStorageDomainsByConnId(NFSConnection.getid());
        CanDoActionTestUtils.runAndAssertCanDoActionSuccess(command);
    }

    @Test
    public void checkRemoveIscsiConnectionDomainsExist() {
        parameters.setStorageServerConnection(iSCSIConnection);
        when(storageServerConnectionDAO.get(iSCSIConnection.getid())).thenReturn(iSCSIConnection);
        List<LUNs> luns = new ArrayList<LUNs>();
        LUNs lun1 = new LUNs();
        lun1.setLUN_id("3600144f09dbd05000000517e730b1212");
        lun1.setStorageDomainName("storagedomain1");
        lun1.setvolume_group_id("G95OWd-Wvck-vftu-pMq9-9SAC-NF3E-ulDPsQ");
        luns.add(lun1);
        when(lunDAO.getAllForStorageServerConnection(iSCSIConnection.getid())).thenReturn(luns);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }

    @Test
    public void checkRemoveIscsiConnectionDomainsAndDisksExist() {
        parameters.setStorageServerConnection(iSCSIConnection);
        when(storageServerConnectionDAO.get(iSCSIConnection.getid())).thenReturn(iSCSIConnection);
        List<LUNs> luns = new ArrayList<LUNs>();
        LUNs lun1 = new LUNs();
        lun1.setLUN_id("3600144f09dbd05000000517e730b1212");
        lun1.setStorageDomainName("storagedomain1");
        lun1.setvolume_group_id("G95OWd-Wvck-vftu-pMq9-9SAC-NF3E-ulDPsQ");
        luns.add(lun1);
        LUNs lun2 = new LUNs();
        lun2.setLUN_id("3600144f09dbd05000000517e730b1212");
        lun2.setStorageDomainName("");
        lun2.setvolume_group_id("");
        lun2.setDiskAlias("disk2");
        luns.add(lun2);
        when(lunDAO.getAllForStorageServerConnection(iSCSIConnection.getid())).thenReturn(luns);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS_AND_DISKS);
    }

    @Test
    public void checkRemoveIscsiConnectionDisksExist() {
        parameters.setStorageServerConnection(iSCSIConnection);
        when(storageServerConnectionDAO.get(iSCSIConnection.getid())).thenReturn(iSCSIConnection);
        List<LUNs> luns = new ArrayList<LUNs>();
        LUNs lun1 = new LUNs();
        lun1.setLUN_id("3600144f09dbd05000000517e730b1212");
        lun1.setStorageDomainName("storagedomain1");
        lun1.setvolume_group_id("");
        lun1.setDiskAlias("disk1");
        luns.add(lun1);
        LUNs lun2 = new LUNs();
        lun2.setLUN_id("3600144f09dbd05000000517e730b1212");
        lun2.setStorageDomainName("storagedomain4");
        lun2.setvolume_group_id("");
        lun2.setDiskAlias("disk2");
        luns.add(lun2);
        when(lunDAO.getAllForStorageServerConnection(iSCSIConnection.getid())).thenReturn(luns);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_DISKS);
    }

    @Test
    public void checkExecuteCommandWithVdsId() {
        parameters.setStorageServerConnection(NFSConnection);
        doNothing().when(storageServerConnectionDAO).remove(NFSConnection.getid());
        doReturn(true).when(command).disconnectStorage();
        command.executeCommand();
    }

    @Test
    public void checkExecuteCommandWithEmptyVdsId() {
        parameters.setStorageServerConnection(NFSConnection);
        parameters.setVdsId(Guid.Empty);
        doNothing().when(storageServerConnectionDAO).remove(NFSConnection.getid());
        // Test will fail if we try to disconnect
        command.executeCommand();
        verify(command, never()).disconnectStorage();
    }

    @Test
    public void checkExecuteCommandWithNullVdsId() {
        parameters.setStorageServerConnection(NFSConnection);
        parameters.setVdsId(null);
        doNothing().when(storageServerConnectionDAO).remove(NFSConnection.getid());
        // Test will fail if we try to disconnect
        command.executeCommand();
        verify(command, never()).disconnectStorage();
    }
}
