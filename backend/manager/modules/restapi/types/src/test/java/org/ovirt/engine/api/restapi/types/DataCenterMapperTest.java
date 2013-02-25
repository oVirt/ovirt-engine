package org.ovirt.engine.api.restapi.types;

import org.junit.Test;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DataCenterStatus;
import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;

public class DataCenterMapperTest extends
        AbstractInvertibleMappingTest<DataCenter, storage_pool, storage_pool> {

    public DataCenterMapperTest() {
        super(DataCenter.class, storage_pool.class, storage_pool.class);
    }

    @Override
    protected DataCenter postPopulate(DataCenter model) {
        model.setStorageType(MappingTestHelper.shuffle(StorageType.class).value());
        model.setStorageFormat(MappingTestHelper.shuffle(StorageFormat.class).value());
        return model;
    }

    @Override
    protected void verify(DataCenter model, DataCenter transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getStorageType(), transform.getStorageType());
        assertEquals(model.getStorageFormat(), transform.getStorageFormat());
    }

    @Test
    //this test was added to support 'status' field, which has only a one-way mapping (from Backend entity to REST entity).
    //The generic test does a round-trip, which would fail when there's only one-way mapping.
    public void testFromBackendToRest() {
        testStatusMapping(StoragePoolStatus.Contend, DataCenterStatus.CONTEND);
        testStatusMapping(StoragePoolStatus.Maintenance, DataCenterStatus.MAINTENANCE);
        testStatusMapping(StoragePoolStatus.NotOperational, DataCenterStatus.NOT_OPERATIONAL);
        testStatusMapping(StoragePoolStatus.Problematic, DataCenterStatus.PROBLEMATIC);
        testStatusMapping(StoragePoolStatus.Uninitialized, DataCenterStatus.UNINITIALIZED);
        testStatusMapping(StoragePoolStatus.Up, DataCenterStatus.UP);
    }

    private void testStatusMapping(StoragePoolStatus storagePoolStatus, DataCenterStatus dataCenterStatus) {
        storage_pool storagePool = new storage_pool();
        storagePool.setstatus(storagePoolStatus);
        DataCenter dataCenter = DataCenterMapper.map(storagePool, null);
        assertEquals(dataCenter.getStatus().getState(), dataCenterStatus.value());
    }
}
