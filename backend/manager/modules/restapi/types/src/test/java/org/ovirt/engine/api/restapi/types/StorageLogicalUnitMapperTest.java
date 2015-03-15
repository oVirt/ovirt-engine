package org.ovirt.engine.api.restapi.types;

import org.junit.Test;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.LunStatus;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;

public class StorageLogicalUnitMapperTest extends AbstractInvertibleMappingTest<Storage, LUNs, LUNs> {

    public StorageLogicalUnitMapperTest() {
        super(Storage.class, LUNs.class, LUNs.class);
    }

    @Override
    protected Storage postPopulate(Storage from) {
        from.setType(MappingTestHelper.shuffle(StorageType.class).value());
        LogicalUnit unit = new LogicalUnit();
        unit.setId(from.getId());
        from.unsetLogicalUnits();
        from.getLogicalUnits().add(unit);
        return from;
    }

    @Override
    protected void verify(Storage model, Storage transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getType(), transform.getType());
        assertEquals(model.getLogicalUnits().size(), model.getLogicalUnits().size());
        for (int i = 0; i < model.getLogicalUnits().size(); i++) {
            assertEquals(model.getLogicalUnits().get(i).getId(), transform.getLogicalUnits().get(i).getId());
        }
    }

    @Test
    public void testOneWayMapping() {
        LUNs model = new LUNs();
        model.setVendorId("vendor_id_1");
        model.setProductId("product_id_1");
        model.setLunMapping(5);
        model.setSerial("some_serial");
        model.setvolume_group_id("volume_group_id_1");
        model.setStorageDomainId(Guid.Empty);
        model.setDiskId(Guid.Empty);
        model.setStatus(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Free);
        LogicalUnit entity = StorageLogicalUnitMapper.map(model, (LogicalUnit) null);
        assertEquals(entity.getVendorId(), "vendor_id_1");
        assertEquals(entity.getProductId(), "product_id_1");
        assertEquals(entity.getSerial(), "some_serial");
        assertEquals(entity.getLunMapping(), Integer.valueOf(5));
        assertEquals(entity.getVolumeGroupId(), "volume_group_id_1");
        assertEquals(entity.getStorageDomainId(), Guid.Empty.toString());
        assertEquals(entity.getDiskId(), Guid.Empty.toString());
        assertEquals(entity.getStatus(), LunStatus.Free.value());
    }

    @Test
    public void testStorageDomainMappings() {
        assertEquals(LunStatus.Free, StorageLogicalUnitMapper.map(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Free, null));
        assertEquals(LunStatus.Used, StorageLogicalUnitMapper.map(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Used, null));
        assertEquals(LunStatus.Unusable, StorageLogicalUnitMapper.map(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Unusable, null));
    }
}
