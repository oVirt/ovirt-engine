package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.NfsVersion;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageFormat;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;


public class StorageDomainMapperTest extends
        AbstractInvertibleMappingTest<StorageDomain, StorageDomainStatic, org.ovirt.engine.core.common.businessentities.StorageDomain> {

    public StorageDomainMapperTest() {
        super(StorageDomain.class, StorageDomainStatic.class, org.ovirt.engine.core.common.businessentities.StorageDomain.class);
    }

    @Override
    protected StorageDomain postPopulate(StorageDomain model) {
        model.setType(StorageDomainType.DATA);
        model.getStorage().setType(StorageType.CINDER);
        model.setStorageFormat(StorageFormat.V1);
        return model;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.StorageDomain getInverse(StorageDomainStatic to) {
        org.ovirt.engine.core.common.businessentities.StorageDomain inverse = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        inverse.setId(to.getId());
        inverse.setStorageName(to.getStorageName());
        inverse.setStorageDomainType(to.getStorageDomainType());
        inverse.setStorageType(to.getStorageType());
        inverse.setStorageFormat(to.getStorageFormat());
        inverse.setComment(to.getComment());
        return inverse;
    }

    @Override
    protected void verify(StorageDomain model, StorageDomain transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        // REVIST No descriptions for storage domains
        // assertEquals(model.getDescription(), transform.getDescription());
        // REVIST No comment for storage domains
        //assertEquals(model.getComment(), transform.getComment());
        assertEquals(model.getType(), transform.getType());
        assertNotNull(transform.getStorage());
        assertEquals(model.getStorage().getType(), transform.getStorage().getType());
        assertEquals(model.getStorageFormat(), transform.getStorageFormat());
    }

    @Test
    public void testMemory() {
        org.ovirt.engine.core.common.businessentities.StorageDomain entity = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        entity.setAvailableDiskSize(3);
        entity.setUsedDiskSize(4);
        entity.setCommittedDiskSize(5);
        StorageDomain model = StorageDomainMapper.map(entity, null);
        assertEquals(Long.valueOf(3221225472L), model.getAvailable());
        assertEquals(Long.valueOf(4294967296L), model.getUsed());
        assertEquals(Long.valueOf(5368709120L), model.getCommitted());
    }

    @Test
    public void storageDomainMappings() {
        assertEquals(
            StorageDomainStatus.ACTIVE,
            StorageDomainMapper.mapStorageDomainStatus(org.ovirt.engine.core.common.businessentities.StorageDomainStatus.Active)
        );
        assertEquals(
            StorageDomainStatus.INACTIVE,
            StorageDomainMapper.mapStorageDomainStatus(org.ovirt.engine.core.common.businessentities.StorageDomainStatus.Inactive)
        );
        assertEquals(
            StorageDomainStatus.LOCKED,
            StorageDomainMapper.mapStorageDomainStatus(org.ovirt.engine.core.common.businessentities.StorageDomainStatus.Locked)
        );
        assertEquals(
            StorageDomainStatus.UNATTACHED,
            StorageDomainMapper.mapStorageDomainStatus(org.ovirt.engine.core.common.businessentities.StorageDomainStatus.Unattached)
        );
        assertEquals(
            StorageDomainStatus.UNKNOWN,
            StorageDomainMapper.mapStorageDomainStatus(org.ovirt.engine.core.common.businessentities.StorageDomainStatus.Unknown)
        );
        assertNull(
            StorageDomainMapper.mapStorageDomainStatus(org.ovirt.engine.core.common.businessentities.StorageDomainStatus.Uninitialized)
        );
        assertEquals(
            StorageDomainStatus.MAINTENANCE,
            StorageDomainMapper.mapStorageDomainStatus(org.ovirt.engine.core.common.businessentities.StorageDomainStatus.Maintenance)
        );

        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.V3,
                StorageDomainMapper.map(NfsVersion.V3, null));
        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.V4,
                StorageDomainMapper.map(NfsVersion.V4, null));
        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.V4_0,
                StorageDomainMapper.map(NfsVersion.V4_0, null));
        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.V4_1,
                StorageDomainMapper.map(NfsVersion.V4_1, null));
        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.V4_2,
                StorageDomainMapper.map(NfsVersion.V4_2, null));
        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.AUTO,
                StorageDomainMapper.map(NfsVersion.AUTO, null));
        assertEquals(NfsVersion.V3, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.V3, null));
        assertEquals(NfsVersion.V4, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.V4, null));
        assertEquals(NfsVersion.V4_0, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.V4_0, null));
        assertEquals(NfsVersion.V4_1, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.V4_1, null));
        assertEquals(NfsVersion.V4_2, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.V4_2, null));
        assertEquals(NfsVersion.AUTO, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.AUTO, null));
    }

    @Test
    public void checkISCSIStorageConnectionsMappings() {
        StorageServerConnections connection = new StorageServerConnections();
        Guid connId = Guid.newGuid();
        connection.setId(connId.toString());
        connection.setIqn("iqn.my.target1");
        connection.setPort("3260");
        connection.setStorageType(org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI);
        connection.setConnection("1.2.135.255");
        connection.setUserName("myuser1");
        connection.setPassword("123");

        HostStorage RESTConnection = new HostStorage();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.ISCSI);
        RESTConnection.setPort(3260);
        RESTConnection.setTarget("iqn.my.target1");
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setUsername("myuser1");

        StorageConnection mappedResult = StorageDomainMapper.map(connection, null);
        assertEquals(RESTConnection.getId(), mappedResult.getId());
        // Although password was set on StorageServerConnections object, it should not be returned via REST
        // thus testing here that it remains empty.
        assertEquals(RESTConnection.getPassword(), mappedResult.getPassword());
        assertEquals(RESTConnection.getType(), mappedResult.getType());
        assertEquals(RESTConnection.getAddress(), mappedResult.getAddress());
        assertEquals(RESTConnection.getUsername(), mappedResult.getUsername());
        assertEquals(RESTConnection.getTarget(), mappedResult.getTarget());
    }

    @Test
    public void checkNFSStorageConnectionsMappings() {
        StorageServerConnections connection = new StorageServerConnections();
        Guid connId = Guid.newGuid();
        connection.setId(connId.toString());
        connection.setStorageType(org.ovirt.engine.core.common.businessentities.storage.StorageType.NFS);
        connection.setConnection("1.2.135.255:/myshare/data");
        connection.setNfsRetrans((short) 200);
        connection.setNfsTimeo((short) 400);
        connection.setNfsVersion(org.ovirt.engine.core.common.businessentities.NfsVersion.V3);
        connection.setMountOptions("tcp");

        HostStorage RESTConnection = new HostStorage();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.NFS);
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setPath("/myshare/data");
        RESTConnection.setNfsRetrans(200);
        RESTConnection.setNfsTimeo(400);
        RESTConnection.setNfsVersion(NfsVersion.V3);
        RESTConnection.setMountOptions("tcp");

        StorageConnection mappedResult = StorageDomainMapper.map(connection, null);
        assertEquals(RESTConnection.getId(), mappedResult.getId());
        assertEquals(RESTConnection.getType(), mappedResult.getType());
        assertEquals(RESTConnection.getAddress(), mappedResult.getAddress());
        assertEquals(RESTConnection.getPath(), mappedResult.getPath());
        assertEquals(RESTConnection.getNfsRetrans(), mappedResult.getNfsRetrans());
        assertEquals(RESTConnection.getNfsTimeo(), mappedResult.getNfsTimeo());
        assertEquals(RESTConnection.getNfsVersion(), mappedResult.getNfsVersion());
        assertEquals(RESTConnection.getMountOptions(), mappedResult.getMountOptions());
    }

    @Test
    public void checkPosixStorageConnectionsMappings() {
        StorageServerConnections connection = new StorageServerConnections();
        Guid connId = Guid.newGuid();
        connection.setId(connId.toString());
        connection.setStorageType(org.ovirt.engine.core.common.businessentities.storage.StorageType.POSIXFS);
        connection.setConnection("1.2.135.255:/myshare/data");
        connection.setVfsType("nfs");
        connection.setMountOptions("timeo=30");

        HostStorage RESTConnection = new HostStorage();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.POSIXFS);
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setPath("/myshare/data");
        RESTConnection.setVfsType("nfs");
        RESTConnection.setMountOptions("timeo=30");

        StorageConnection mappedResult = StorageDomainMapper.map(connection, null);
        assertEquals(RESTConnection.getId(), mappedResult.getId());
        assertEquals(RESTConnection.getType(), mappedResult.getType());
        assertEquals(RESTConnection.getAddress(), mappedResult.getAddress());
        assertEquals(RESTConnection.getVfsType(), mappedResult.getVfsType());
        assertEquals(RESTConnection.getPath(), mappedResult.getPath());
        assertEquals(RESTConnection.getMountOptions(), mappedResult.getMountOptions());
    }

    @Test
    public void checkPosixStorageConnectionsMappingsToBll() {
        StorageServerConnections connection = new StorageServerConnections();
        Guid connId = Guid.newGuid();
        connection.setId(connId.toString());
        connection.setStorageType(org.ovirt.engine.core.common.businessentities.storage.StorageType.POSIXFS);
        connection.setConnection("1.2.135.255:/myshare/data");
        connection.setVfsType("nfs");
        connection.setMountOptions("timeo=30");

        StorageConnection RESTConnection = new StorageConnection();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.POSIXFS);
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setPath("/myshare/data");
        RESTConnection.setVfsType("nfs");
        RESTConnection.setMountOptions("timeo=30");

        StorageServerConnections mappedResult = StorageDomainMapper.map(RESTConnection, null);
        assertEquals(connection.getId(), mappedResult.getId());
        assertEquals(connection.getStorageType(), mappedResult.getStorageType());
        assertEquals(connection.getConnection(), mappedResult.getConnection());
        assertEquals(connection.getVfsType(), mappedResult.getVfsType());
        assertEquals(connection.getMountOptions(), mappedResult.getMountOptions());
    }

    @Test
    public void checkNFSStorageConnectionsMappingsToBll() {
        StorageServerConnections connection = new StorageServerConnections();
        Guid connId = Guid.newGuid();
        connection.setId(connId.toString());
        connection.setStorageType(org.ovirt.engine.core.common.businessentities.storage.StorageType.NFS);
        connection.setConnection("1.2.135.255:/myshare/data");
        connection.setNfsRetrans((short) 200);
        connection.setNfsTimeo((short) 400);
        connection.setNfsVersion(org.ovirt.engine.core.common.businessentities.NfsVersion.V3);
        connection.setMountOptions("tcp");

        StorageConnection RESTConnection = new StorageConnection();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.NFS);
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setPath("/myshare/data");
        RESTConnection.setNfsRetrans(200);
        RESTConnection.setNfsTimeo(400);
        RESTConnection.setNfsVersion(NfsVersion.V3);
        RESTConnection.setMountOptions("tcp");

        StorageServerConnections mappedResult = StorageDomainMapper.map(RESTConnection, null);
        assertEquals(connection.getId(), mappedResult.getId());
        assertEquals(connection.getStorageType(), mappedResult.getStorageType());
        assertEquals(connection.getConnection(), mappedResult.getConnection());
        assertEquals(connection.getNfsRetrans(), mappedResult.getNfsRetrans());
        assertEquals(connection.getNfsTimeo(), mappedResult.getNfsTimeo());
        assertEquals(connection.getNfsVersion(), mappedResult.getNfsVersion());
        assertEquals(connection.getMountOptions(), mappedResult.getMountOptions());
    }

    @Test
    public void checkISCSISStorageConnectionsMappingsToBll() {
        StorageServerConnections connection = new StorageServerConnections();
        Guid connId = Guid.newGuid();
        connection.setId(connId.toString());
        connection.setIqn("iqn.my.target1");
        connection.setPort("3260");
        connection.setStorageType(org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI);
        connection.setConnection("1.2.135.255");
        connection.setUserName("myuser1");
        connection.setPassword("123");

        StorageConnection RESTConnection = new StorageConnection();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.ISCSI);
        RESTConnection.setPort(3260);
        RESTConnection.setTarget("iqn.my.target1");
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setUsername("myuser1");
        RESTConnection.setPassword("123");

        StorageServerConnections mappedResult = StorageDomainMapper.map(RESTConnection, null);
        assertEquals(mappedResult.getId(), mappedResult.getId());
        assertEquals(mappedResult.getStorageType(), mappedResult.getStorageType());
        assertEquals(mappedResult.getConnection(), mappedResult.getConnection());
        assertEquals(mappedResult.getIqn(), mappedResult.getIqn());
        assertEquals(mappedResult.getUserName(), mappedResult.getUserName());
        assertEquals(mappedResult.getPassword(), mappedResult.getPassword());
        assertEquals(mappedResult.getPort(), mappedResult.getPort());

    }

}
