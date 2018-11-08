package org.ovirt.engine.core.bll.storage.connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.CommandAssertUtils;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VmDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateStorageServerConnectionCommandTest extends
        StorageServerConnectionTestCommon<UpdateStorageServerConnectionCommand<StorageServerConnectionParametersBase>> {

    @Mock
    protected ISCSIStorageHelper iscsiStorageHelper;

    @Override
    protected UpdateStorageServerConnectionCommand<StorageServerConnectionParametersBase> createCommand() {
        parameters = new StorageServerConnectionParametersBase();
        parameters.setVdsId(Guid.newGuid());

        return new UpdateStorageServerConnectionCommand<>(parameters, null);
    }

    private StorageServerConnections oldNFSConnection = null;
    private StorageServerConnections oldPosixConnection = null;

    @Mock
    private StorageDomainDynamicDao storageDomainDynamicDao;

    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Mock
    private LunDao lunDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private StorageDomainDao storageDomainDao;


    @BeforeEach
    public void prepareMembers() {

        oldNFSConnection =
                createNFSConnection(
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.NFS,
                        NfsVersion.V4,
                        50,
                        0);

        oldPosixConnection =
                createPosixConnection(
                        "multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");
    }

    protected StorageDomain createDomain() {
        StorageDomain domain = new StorageDomain();
        domain.setStorageName("mydomain");
        return domain;
    }

    @Test
    public void checkNoHost() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);
        parameters.setVdsId(null);
        parameters.setStorageServerConnection(newNFSConnection);
        when(storageConnDao.get(newNFSConnection.getId())).thenReturn(oldNFSConnection);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void checkEmptyIdHost() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);
        parameters.setVdsId(Guid.Empty);
        when(storageConnDao.get(newNFSConnection.getId())).thenReturn(oldNFSConnection);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void updateFCPUnsupportedConnectionType() {
        StorageServerConnections dummyFCPConn =
                createISCSIConnection("10.35.16.25", StorageType.FCP, "", "3260", "user1", "mypassword123");
        parameters.setStorageServerConnection(dummyFCPConn);

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE_TYPE);
    }

    @Test
    public void updateChangeConnectionType() {
        StorageServerConnections iscsiConnection =
                createISCSIConnection("10.35.16.25", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3260", "user1", "mypassword123");
        parameters.setStorageServerConnection(iscsiConnection);
        when(storageConnDao.get(iscsiConnection.getId())).thenReturn(oldNFSConnection);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_CHANGE_STORAGE_TYPE);
    }

    @Test
    public void updateNonExistingConnection() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
    }

    @Test
    public void updateBadFormatPath() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "host/mydir",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.VALIDATION_STORAGE_CONNECTION_INVALID);
    }

    @Test
    public void updateSeveralConnectionsWithSamePath() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);
        StorageServerConnections conn1 = new StorageServerConnections();
        conn1.setConnection(newNFSConnection.getConnection());
        conn1.setId(newNFSConnection.getId());
        StorageServerConnections conn2 = new StorageServerConnections();
        conn2.setConnection(newNFSConnection.getConnection());
        conn2.setId(Guid.newGuid().toString());
        when(storageConnDao.get(newNFSConnection.getId())).thenReturn(oldNFSConnection);
        String guid = Guid.newGuid().toString();
        doReturn(guid).when(command).isConnWithSameDetailsExists(newNFSConnection, null);
        doReturn("storage_domain_01").when(command).getStorageNameByConnectionId(guid);
        List<String> messages = ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
        assertTrue(messages.contains("$connectionId " + guid) && messages.contains("$storageDomainName storage_domain_01"));
    }

    @Test
    public void updateConnectionOfSeveralDomains() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);

        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newNFSConnection.getConnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domain1.setStorageName("domain1");

        StorageDomain domain2 = new StorageDomain();
        domain2.setStorage(newNFSConnection.getConnection());
        domain2.setStatus(StorageDomainStatus.Maintenance);
        domain2.setStorageName("domain2");
        when(storageConnDao.get(newNFSConnection.getId())).thenReturn(oldNFSConnection);

        initDomainListForConnection(newNFSConnection.getId(), domain1, domain2);

        doReturn("").when(command).isConnWithSameDetailsExists(newNFSConnection, null);
        List<String> messages =
                ValidateTestUtils.runAndAssertValidateFailure(command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
        assertTrue(messages.contains("$domainNames domain1,domain2"));
    }

    @Test
    public void updateConnectionOfActiveDomain() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);

        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newNFSConnection.getConnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domain1.setStorageDomainSharedStatus(StorageDomainSharedStatus.Active);
        initDomainListForConnection(newNFSConnection.getId(), domain1);

        parameters.setStorageServerConnection(newNFSConnection);
        when(storageConnDao.get(newNFSConnection.getId())).thenReturn(oldNFSConnection);

        doReturn("").when(command).isConnWithSameDetailsExists(newNFSConnection, null);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_DOMAIN_MUST_BE_IN_MAINTENANCE_OR_UNATTACHED);
    }

    @Test
    public void updateConnectionOfDomainsAndLunDisks() {
        StorageServerConnections iscsiConnection = createISCSIConnection("10.35.16.25", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3260", "user1", "mypassword123");
        LUNs lun1 = new LUNs();
        lun1.setLUNId("3600144f09dbd05000000517e730b1212");
        lun1.setVolumeGroupId("");
        lun1.setDiskAlias("disk1");
        Guid diskId1 = Guid.newGuid();
        lun1.setDiskId(diskId1);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("3600144f09dbd05000000517e730b1212");
        lun2.setVolumeGroupId("");
        lun2.setDiskAlias("disk2");
        Guid diskId2 = Guid.newGuid();
        lun2.setDiskId(diskId2);
        LUNs lun3 = new LUNs();
        lun3.setLUNId("3600144f09dbd05000000517e730b1212");
        lun3.setStorageDomainName("storagedomain4");
        Guid storageDomainId = Guid.newGuid();
        lun3.setStorageDomainId(storageDomainId);
        lun3.setVolumeGroupId(Guid.newGuid().toString());
        List<LUNs> luns = Arrays.asList(lun1, lun2, lun3);

        Map<Boolean, List<VM>> vmsMap = new HashMap<>();
        VM vm1 = new VM();
        vm1.setName("vm1");
        vm1.setStatus(VMStatus.Up);
        VM vm2 = new VM();
        vm2.setName("vm2");
        vm2.setStatus(VMStatus.Down);
        VM vm3 = new VM();
        vm3.setName("vm3");
        vm3.setStatus(VMStatus.Up);
        List<VM> pluggedVms = Arrays.asList(vm1, vm2);
        List<VM> unPluggedVms = Collections.singletonList(vm3);
        vmsMap.put(Boolean.FALSE, unPluggedVms);
        vmsMap.put(Boolean.TRUE, pluggedVms);
        when(vmDao.getForDisk(diskId1, true)).thenReturn(vmsMap);
        parameters.setStorageServerConnection(iscsiConnection);
        when(storageConnDao.get(iscsiConnection.getId())).thenReturn(iscsiConnection);
        doReturn(luns).when(command).getLuns();

        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(iscsiConnection.getConnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domain1.setStorageDomainSharedStatus(StorageDomainSharedStatus.Active);
        domain1.setId(storageDomainId);
        domain1.setStorageName("storagedomain4");

        when(storageDomainDao.get(storageDomainId)).thenReturn(domain1);
        when(storagePoolIsoMapDao.getAllForStorage(storageDomainId)).
                thenReturn(Collections.singletonList
                        (new StoragePoolIsoMap(storageDomainId, Guid.newGuid(), StorageDomainStatus.Active)));
        List<String> messages = ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_RUNNING_VMS_AND_DOMAINS_STATUS);
        assertTrue(messages.contains("$vmNames vm1"));
        assertTrue(messages.contains("$domainNames storagedomain4"));
    }

    @Test
    public void updateConnectionOfLunDisks() {
        StorageServerConnections iscsiConnection = createISCSIConnection("10.35.16.25", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3260", "user1", "mypassword123");
        LUNs lun1 = new LUNs();
        lun1.setLUNId("3600144f09dbd05000000517e730b1212");
        lun1.setVolumeGroupId("");
        lun1.setDiskAlias("disk1");
        Guid diskId1 = Guid.newGuid();
        lun1.setDiskId(diskId1);
        LUNs lun2 = new LUNs();
        lun2.setLUNId("3600144f09dbd05000000517e730b1212");
        lun2.setVolumeGroupId("");
        lun2.setDiskAlias("disk2");
        Guid diskId2 = Guid.newGuid();
        lun2.setDiskId(diskId2);
        List<LUNs> luns = Arrays.asList(lun1, lun2);
        Map<Boolean, List<VM>> vmsMap = new HashMap<>();
        VM vm1 = new VM();
        vm1.setName("vm1");
        vm1.setStatus(VMStatus.Up);
        VM vm2 = new VM();
        vm2.setName("vm2");
        vm2.setStatus(VMStatus.Paused);
        VM vm3 = new VM();
        vm3.setName("vm3");
        vm3.setStatus(VMStatus.Up);
        List<VM> pluggedVms = Arrays.asList(vm1, vm2);
        List<VM> unPluggedVms = Collections.singletonList(vm3);
        vmsMap.put(Boolean.FALSE, unPluggedVms);
        vmsMap.put(Boolean.TRUE, pluggedVms);
        when(vmDao.getForDisk(diskId1, true)).thenReturn(vmsMap);
        parameters.setStorageServerConnection(iscsiConnection);
        when(storageConnDao.get(iscsiConnection.getId())).thenReturn(iscsiConnection);
        doReturn(luns).when(command).getLuns();
        List<String> messages = ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_RUNNING_VMS);
        assertTrue(messages.contains("$vmNames vm1,vm2"));
    }

    @Test
    public void updateConnectionOfDomains() {
        StorageServerConnections iscsiConnection = createISCSIConnection("10.35.16.25", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3260", "user1", "mypassword123");
        LUNs lun1 = new LUNs();
        lun1.setLUNId("3600144f09dbd05000000517e730b1212");
        lun1.setStorageDomainName("storagedomain4");
        Guid storageDomainId = Guid.newGuid();
        lun1.setStorageDomainId(storageDomainId);
        lun1.setVolumeGroupId(Guid.newGuid().toString());
        List<LUNs> luns = Collections.singletonList(lun1);
        parameters.setStorageServerConnection(iscsiConnection);
        when(storageConnDao.get(iscsiConnection.getId())).thenReturn(iscsiConnection);
        doReturn(luns).when(command).getLuns();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(iscsiConnection.getConnection());
        domain1.setStatus(StorageDomainStatus.Active);
        domain1.setStorageDomainSharedStatus(StorageDomainSharedStatus.Active);
        domain1.setId(storageDomainId);
        domain1.setStorageName("storagedomain4");
        when(storageDomainDao.get(storageDomainId)).thenReturn(domain1);
        when(storagePoolIsoMapDao.getAllForStorage(storageDomainId)).
                thenReturn(Collections.singletonList
                        (new StoragePoolIsoMap(storageDomainId, Guid.newGuid(), StorageDomainStatus.Active)));
        List<String> messages =
                ValidateTestUtils.runAndAssertValidateFailure(command,
                        EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_DOMAIN_MUST_BE_IN_MAINTENANCE_OR_UNATTACHED);
        assertTrue(messages.contains("$domainNames storagedomain4"));
    }

    @Test
    public void updateConnectionOfUnattachedBlockDomain() {
        StorageServerConnections iscsiConnection = createISCSIConnection("10.35.16.25", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3260", "user1", "mypassword123");
        LUNs lun1 = new LUNs();
        lun1.setLUNId("3600144f09dbd05000000517e730b1212");
        lun1.setStorageDomainName("storagedomain4");
        Guid storageDomainId = Guid.newGuid();
        lun1.setStorageDomainId(storageDomainId);
        lun1.setVolumeGroupId(Guid.newGuid().toString());
        List<LUNs> luns = Collections.singletonList(lun1);
        parameters.setStorageServerConnection(iscsiConnection);
        when(storageConnDao.get(iscsiConnection.getId())).thenReturn(iscsiConnection);
        doReturn(luns).when(command).getLuns();
        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(iscsiConnection.getConnection());
        domain1.setStatus(StorageDomainStatus.Unknown);
        domain1.setStorageDomainSharedStatus(StorageDomainSharedStatus.Unattached);
        domain1.setId(storageDomainId);
        domain1.setStorageName("storagedomain4");
        when(storageDomainDao.get(storageDomainId)).thenReturn(domain1);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void updateConnectionOfUnattachedFileDomain() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);

        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newNFSConnection.getConnection());
        domain1.setStatus(StorageDomainStatus.Unknown);
        domain1.setStorageDomainSharedStatus(StorageDomainSharedStatus.Unattached);
        initDomainListForConnection(newNFSConnection.getId(), domain1);

        parameters.setStorageServerConnection(newNFSConnection);
        when(storageConnDao.get(newNFSConnection.getId())).thenReturn(oldNFSConnection);

        doReturn("").when(command).isConnWithSameDetailsExists(newNFSConnection, null);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }


    @Test
    public void updateConnectionNoDomain() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);
        when(storageConnDao.get(newNFSConnection.getId())).thenReturn(oldNFSConnection);
        List<StorageDomain> domains = Collections.emptyList();
        doReturn(domains).when(command).getStorageDomainsByConnId(newNFSConnection.getId());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void succeedValidateNFS() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);

        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newNFSConnection.getConnection());
        domain1.setStatus(StorageDomainStatus.Maintenance);
        initDomainListForConnection(newNFSConnection.getId(), domain1);

        when(storageConnDao.get(newNFSConnection.getId())).thenReturn(oldNFSConnection);
        doReturn("").when(command).isConnWithSameDetailsExists(newNFSConnection, null);

        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void succeedValidatePosix() {
        StorageServerConnections newPosixConnection =
                createPosixConnection("multipass.my.domain.tlv.company.com:/export/allstorage/data1",
                        StorageType.POSIXFS,
                        "nfs",
                        "timeo=30");
        parameters.setStorageServerConnection(newPosixConnection);

        StorageDomain domain1 = new StorageDomain();
        domain1.setStorage(newPosixConnection.getConnection());
        domain1.setStatus(StorageDomainStatus.Maintenance);
        initDomainListForConnection(newPosixConnection.getId(), domain1);

        parameters.setStorageServerConnection(newPosixConnection);
        when(storageConnDao.get(newPosixConnection.getId())).thenReturn(oldPosixConnection);

        doReturn("").when(command).isConnWithSameDetailsExists(newPosixConnection, null);
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void succeedUpdateNFSCommandWithDomain() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        parameters.setStorageServerConnection(newNFSConnection);
        VDSReturnValue returnValueConnectSuccess = new VDSReturnValue();
        StoragePoolIsoMap map = new StoragePoolIsoMap();
        returnValueConnectSuccess.setSucceeded(true);
        StorageDomain domain = createDomain();
        doReturn(Collections.singletonList(map)).when(command).getStoragePoolIsoMap(domain);
        returnValueConnectSuccess.setReturnValue(domain);
        doReturn(returnValueConnectSuccess).when(command).getStatsForDomain(domain);
        doReturn(true).when(command).connectToStorage();
        List<StorageDomain> domains = Collections.singletonList(domain);
        doReturn(domains).when(command).getStorageDomainsByConnId(newNFSConnection.getId());
        doNothing().when(command).changeStorageDomainStatusInTransaction(StorageDomainStatus.Locked);
        doNothing().when(command).disconnectFromStorage();
        doNothing().when(command).updateStorageDomain(domains);
        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
    }

    @Test
    public void succeedUpdateNFSCommandNoDomain() {
        StorageServerConnections newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);
        VDSReturnValue returnValueConnectSuccess = new VDSReturnValue();
        doReturn(false).when(command).doDomainsUseConnection(newNFSConnection);
        returnValueConnectSuccess.setSucceeded(true);
        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
        verify(command, never()).connectToStorage();
        verify(command, never()).disconnectFromStorage();
        verify(command, never()).changeStorageDomainStatusInTransaction(StorageDomainStatus.Locked);

    }

    @Test
    public void failUpdateStats() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
        parameters.setStorageServerConnection(newNFSConnection);
        VDSReturnValue returnValueUpdate = new VDSReturnValue();
        returnValueUpdate.setSucceeded(false);

        StorageDomain domain = createDomain();
        initDomainListForConnection(newNFSConnection.getId(), domain);

        StorageDomainDynamic domainDynamic = new StorageDomainDynamic();
        StoragePoolIsoMap map = new StoragePoolIsoMap();
        doReturn(Collections.singletonList(map)).when(command).getStoragePoolIsoMap(domain);
        doReturn(returnValueUpdate).when(command).getStatsForDomain(domain);
        doReturn(true).when(command).connectToStorage();
        doNothing().when(command).changeStorageDomainStatusInTransaction(StorageDomainStatus.Locked);
        doNothing().when(command).disconnectFromStorage();
        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
        verify(storageDomainDynamicDao, never()).update(domainDynamic);
    }

    @Test
    public void failUpdateConnectToStorage() {
        StorageServerConnections  newNFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        parameters.setStorageServerConnection(newNFSConnection);
        doReturn(false).when(command).connectToStorage();
        VDSReturnValue returnValueUpdate = new VDSReturnValue();
        returnValueUpdate.setSucceeded(true);

        StorageDomainDynamic domainDynamic = new StorageDomainDynamic();
        StorageDomain domain = createDomain();
        initDomainListForConnection(newNFSConnection.getId(), domain);

        StoragePoolIsoMap map = new StoragePoolIsoMap();
        doReturn(Collections.singletonList(map)).when(command).getStoragePoolIsoMap(domain);
        command.executeCommand();
        CommandAssertUtils.checkSucceeded(command, true);
        verify(storageDomainDynamicDao, never()).update(domainDynamic);
        verify(command, never()).disconnectFromStorage();
    }

    @Test
    public void isConnWithSameDetailsExistFileDomains() {
       StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);

       StorageServerConnections connection1 = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);
       StorageServerConnections connection2 = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        600,
                        0);
        List<StorageServerConnections> connections = Arrays.asList(connection1, connection2);

       when(storageConnDao.getAllForStorage(newNFSConnection.getConnection())).thenReturn(connections);
       boolean isExists = !command.isConnWithSameDetailsExists(newNFSConnection, null).isEmpty();
       assertTrue(isExists);
    }

    @Test
    public void isConnWithSameDetailsExistSameConnection() {
       StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);

       List<StorageServerConnections> connections = Collections.singletonList(newNFSConnection);

       boolean isExists = !command.isConnWithSameDetailsExists(newNFSConnection, null).isEmpty();
        assertFalse(isExists);
    }

    @Test
    public void isConnWithSameDetailsExistNoConnections() {
       StorageServerConnections  newNFSConnection = createNFSConnection(
                       "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                        StorageType.NFS,
                        NfsVersion.V4,
                        300,
                        0);

       List<StorageServerConnections> connections = Collections.emptyList();

       boolean isExists = !command.isConnWithSameDetailsExists(newNFSConnection, null).isEmpty();
       assertFalse(isExists);
    }

    @Test
    public void isConnWithSameDetailsExistBlockDomains() {
       StorageServerConnections  newISCSIConnection = createISCSIConnection("1.2.3.4", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3260", "user1", "mypassword123");

       StorageServerConnections connection1 = createISCSIConnection("1.2.3.4", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3260", "user1", "mypassword123");
       when(iscsiStorageHelper.findConnectionWithSameDetails(newISCSIConnection)).thenReturn(connection1);
       boolean isExists = !command.isConnWithSameDetailsExists(newISCSIConnection, null).isEmpty();
       assertTrue(isExists);
    }

    @Test
    public void isConnWithSameDetailsExistCheckSameConn() {
       StorageServerConnections  newISCSIConnection = createISCSIConnection("1.2.3.4", StorageType.ISCSI, "iqn.2013-04.myhat.com:aaa-target1", "3260", "user1", "mypassword123");
       when(iscsiStorageHelper.findConnectionWithSameDetails(newISCSIConnection)).thenReturn(newISCSIConnection);
       boolean isExists = !command.isConnWithSameDetailsExists(newISCSIConnection, null).isEmpty();
        assertTrue(isExists);
    }

    @Test
    public void failDomainIsActive() {
        StorageServerConnections NFSConnection = createNFSConnection(
                "multipass.my.domain.tlv.company.com:/export/allstorage/data2",
                StorageType.NFS,
                NfsVersion.V4,
                300,
                0);
        when(command.getConnection()).thenReturn(NFSConnection);
        doReturn(oldNFSConnection).when(storageConnDao).get(any());

        // Create an active domain.
        StorageDomain domain = new StorageDomain();
        domain.setStatus(StorageDomainStatus.Active);
        domain.setStorageDomainSharedStatus(StorageDomainSharedStatus.Active);

        initDomainListForConnection(NFSConnection.getId(), domain);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_DOMAIN_MUST_BE_IN_MAINTENANCE_OR_UNATTACHED);
    }

    private void initDomainListForConnection(String connId, StorageDomain... domains) {
        doReturn(Arrays.asList(domains)).when(command).getStorageDomainsByConnId(connId);
    }

    @Override
    protected boolean createConnectionWithId() {
        return true;
    }
}
