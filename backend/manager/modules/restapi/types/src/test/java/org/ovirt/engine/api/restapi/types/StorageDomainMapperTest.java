package org.ovirt.engine.api.restapi.types;

import org.junit.Test;
import org.ovirt.engine.api.model.NfsVersion;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.restapi.model.StorageFormat;
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
        model.setType(MappingTestHelper.shuffle(StorageDomainType.class).value());
        model.getStorage().setType(MappingTestHelper.shuffle(StorageType.class).value());
        model.setStorageFormat(MappingTestHelper.shuffle(StorageFormat.class).value());
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
        assertEquals(model.getComment(), transform.getComment());
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
        StorageDomain model = StorageDomainMapper.map(entity, (StorageDomain) null);
        assertEquals(model.getAvailable(), Long.valueOf(3221225472L));
        assertEquals(model.getUsed(), Long.valueOf(4294967296L));
        assertEquals(model.getCommitted(), Long.valueOf(5368709120L));
    }

    @Test
    public void storageDomainMappings() {
        assertEquals(StorageDomainStatus.ACTIVE, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Active, null));
        assertEquals(StorageDomainStatus.INACTIVE, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Inactive, null));
        assertEquals(StorageDomainStatus.LOCKED, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Locked, null));
        assertEquals(StorageDomainStatus.UNATTACHED, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Unattached, null));
        assertEquals(StorageDomainStatus.UNKNOWN, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Unknown, null));
        assertTrue(StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Uninitialized, null) == null);
        assertEquals(StorageDomainStatus.MAINTENANCE, StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.StorageDomainStatus.Maintenance, null));

        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.V3,
                StorageDomainMapper.map(NfsVersion.V3, null));
        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.V4,
                StorageDomainMapper.map(NfsVersion.V4, null));
        assertEquals(org.ovirt.engine.core.common.businessentities.NfsVersion.AUTO,
                StorageDomainMapper.map(NfsVersion.AUTO, null));
        assertEquals(NfsVersion.V3.value(), StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.V3, null));
        assertEquals(NfsVersion.V4.value(), StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.V4, null));
        assertEquals(NfsVersion.AUTO.value(), StorageDomainMapper.map(org.ovirt.engine.core.common
                .businessentities.NfsVersion.AUTO, null));
    }

    @Test
    public void checkISCSIStorageConnectionsMappings() {
        StorageServerConnections connection = new StorageServerConnections();
        Guid connId = Guid.newGuid();
        connection.setid(connId.toString());
        connection.setiqn("iqn.my.target1");
        connection.setport("3260");
        connection.setstorage_type(org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI);
        connection.setconnection("1.2.135.255");
        connection.setuser_name("myuser1");
        connection.setpassword("123");

        Storage RESTConnection = new Storage();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.ISCSI.toString().toLowerCase());
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
        connection.setid(connId.toString());
        connection.setstorage_type(org.ovirt.engine.core.common.businessentities.storage.StorageType.NFS);
        connection.setconnection("1.2.135.255:/myshare/data");
        connection.setNfsRetrans((short) 200);
        connection.setNfsTimeo((short) 400);
        connection.setNfsVersion(org.ovirt.engine.core.common.businessentities.NfsVersion.V3);
        connection.setMountOptions("tcp");

        Storage RESTConnection = new Storage();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.NFS.toString().toLowerCase());
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setPath("/myshare/data");
        RESTConnection.setNfsRetrans(200);
        RESTConnection.setNfsTimeo(400);
        RESTConnection.setNfsVersion(NfsVersion.V3.toString());
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
        connection.setid(connId.toString());
        connection.setstorage_type(org.ovirt.engine.core.common.businessentities.storage.StorageType.POSIXFS);
        connection.setconnection("1.2.135.255:/myshare/data");
        connection.setVfsType("nfs");
        connection.setMountOptions("timeo=30");

        Storage RESTConnection = new Storage();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.POSIXFS.toString().toLowerCase());
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
        connection.setid(connId.toString());
        connection.setstorage_type(org.ovirt.engine.core.common.businessentities.storage.StorageType.POSIXFS);
        connection.setconnection("1.2.135.255:/myshare/data");
        connection.setVfsType("nfs");
        connection.setMountOptions("timeo=30");

        StorageConnection RESTConnection = new StorageConnection();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.POSIXFS.toString().toLowerCase());
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setPath("/myshare/data");
        RESTConnection.setVfsType("nfs");
        RESTConnection.setMountOptions("timeo=30");

        StorageServerConnections mappedResult = StorageDomainMapper.map(RESTConnection, null);
        assertEquals(connection.getid(), mappedResult.getid());
        assertEquals(connection.getstorage_type(), mappedResult.getstorage_type());
        assertEquals(connection.getconnection(), mappedResult.getconnection());
        assertEquals(connection.getVfsType(), mappedResult.getVfsType());
        assertEquals(connection.getMountOptions(), mappedResult.getMountOptions());
    }

    @Test
    public void checkNFSStorageConnectionsMappingsToBll() {
        StorageServerConnections connection = new StorageServerConnections();
        Guid connId = Guid.newGuid();
        connection.setid(connId.toString());
        connection.setstorage_type(org.ovirt.engine.core.common.businessentities.storage.StorageType.NFS);
        connection.setconnection("1.2.135.255:/myshare/data");
        connection.setNfsRetrans((short) 200);
        connection.setNfsTimeo((short) 400);
        connection.setNfsVersion(org.ovirt.engine.core.common.businessentities.NfsVersion.V3);
        connection.setMountOptions("tcp");

        StorageConnection RESTConnection = new StorageConnection();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.NFS.toString().toLowerCase());
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setPath("/myshare/data");
        RESTConnection.setNfsRetrans(200);
        RESTConnection.setNfsTimeo(400);
        RESTConnection.setNfsVersion(NfsVersion.V3.toString());
        RESTConnection.setMountOptions("tcp");

        StorageServerConnections mappedResult = StorageDomainMapper.map(RESTConnection, null);
        assertEquals(connection.getid(), mappedResult.getid());
        assertEquals(connection.getstorage_type(), mappedResult.getstorage_type());
        assertEquals(connection.getconnection(), mappedResult.getconnection());
        assertEquals(connection.getNfsRetrans(), mappedResult.getNfsRetrans());
        assertEquals(connection.getNfsTimeo(), mappedResult.getNfsTimeo());
        assertEquals(connection.getNfsVersion(), mappedResult.getNfsVersion());
        assertEquals(connection.getMountOptions(), mappedResult.getMountOptions());
    }

    @Test
    public void checkISCSISStorageConnectionsMappingsToBll() {
        StorageServerConnections connection = new StorageServerConnections();
        Guid connId = Guid.newGuid();
        connection.setid(connId.toString());
        connection.setiqn("iqn.my.target1");
        connection.setport("3260");
        connection.setstorage_type(org.ovirt.engine.core.common.businessentities.storage.StorageType.ISCSI);
        connection.setconnection("1.2.135.255");
        connection.setuser_name("myuser1");
        connection.setpassword("123");

        StorageConnection RESTConnection = new StorageConnection();
        RESTConnection.setId(connId.toString());
        RESTConnection.setType(StorageType.ISCSI.toString().toLowerCase());
        RESTConnection.setPort(3260);
        RESTConnection.setTarget("iqn.my.target1");
        RESTConnection.setAddress("1.2.135.255");
        RESTConnection.setUsername("myuser1");
        RESTConnection.setPassword("123");

        StorageServerConnections mappedResult = StorageDomainMapper.map(RESTConnection, null);
        assertEquals(mappedResult.getid(), mappedResult.getid());
        assertEquals(mappedResult.getstorage_type(), mappedResult.getstorage_type());
        assertEquals(mappedResult.getconnection(), mappedResult.getconnection());
        assertEquals(mappedResult.getiqn(), mappedResult.getiqn());
        assertEquals(mappedResult.getuser_name(), mappedResult.getuser_name());
        assertEquals(mappedResult.getpassword(), mappedResult.getpassword());
        assertEquals(mappedResult.getport(), mappedResult.getport());

    }

}
