package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageDomainServerConnectionsResourceTest extends AbstractBackendCollectionResourceTest<StorageConnection, StorageServerConnections, BackendStorageDomainServerConnectionsResource> {
    protected static final org.ovirt.engine.core.common.businessentities.StorageType STORAGE_TYPES_MAPPED[] = {
            org.ovirt.engine.core.common.businessentities.StorageType.NFS,
            org.ovirt.engine.core.common.businessentities.StorageType.LOCALFS,
            org.ovirt.engine.core.common.businessentities.StorageType.POSIXFS,
            org.ovirt.engine.core.common.businessentities.StorageType.ISCSI };

    public BackendStorageDomainServerConnectionsResourceTest() {
        super(new BackendStorageDomainServerConnectionsResource(GUIDS[3]), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }


    @Override
    protected List<StorageConnection> getCollection() {
        return collection.list().getStorageConnections();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assert (query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetStorageServerConnectionsForDomain,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpStorageConnections(),
                failure);

        control.replay();
    }

    protected List<StorageServerConnections> setUpStorageConnections() {
        List<StorageServerConnections> storageConnections = new ArrayList<>();
        storageConnections.add(getEntity(3));
        return storageConnections;
    }

    @Override
    protected void verifyCollection(List<StorageConnection> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(1, collection.size());
    }

    @Override
    protected StorageServerConnections getEntity(int index) {
        return setUpEntityExpectations(control.createMock(StorageServerConnections.class), index);
    }

    static StorageServerConnections setUpEntityExpectations(StorageServerConnections entity, int index) {
        expect(entity.getid()).andReturn(GUIDS[index].toString()).anyTimes();
        expect(entity.getstorage_type()).andReturn(STORAGE_TYPES_MAPPED[index]).anyTimes();
        expect(entity.getconnection()).andReturn("1.1.1.255").anyTimes();
        if (STORAGE_TYPES_MAPPED[index].equals(StorageType.ISCSI)) {
            expect(entity.getport()).andReturn("3260").anyTimes();
        }

        return entity;
    }
}
