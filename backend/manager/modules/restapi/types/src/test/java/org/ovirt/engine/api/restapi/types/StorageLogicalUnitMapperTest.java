package org.ovirt.engine.api.restapi.types;

import org.junit.Test;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.LunStatus;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;

public class StorageLogicalUnitMapperTest extends AbstractInvertibleMappingTest<HostStorage, LUNs, LUNs> {

    public StorageLogicalUnitMapperTest() {
        super(HostStorage.class, LUNs.class, LUNs.class);
    }

    @Override
    protected HostStorage postPopulate(HostStorage from) {
        from.setType(MappingTestHelper.shuffle(StorageType.class));
        LogicalUnit unit = new LogicalUnit();
        unit.setId(from.getId());
        from.getLogicalUnits().unsetLogicalUnits();
        from.getLogicalUnits().getLogicalUnits().add(unit);
        return from;
    }

    @Override
    protected void verify(HostStorage model, HostStorage transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getType(), transform.getType());
        assertEquals(model.getLogicalUnits().getLogicalUnits().size(), model.getLogicalUnits().getLogicalUnits().size());
        for (int i = 0; i < model.getLogicalUnits().getLogicalUnits().size(); i++) {
            assertEquals(model.getLogicalUnits().getLogicalUnits().get(i).getId(),
                    transform.getLogicalUnits().getLogicalUnits().get(i).getId());
        }
    }

    @Test
    public void testOneWayMapping() {
        LUNs model = new LUNs();
        model.setVendorId("vendor_id_1");
        model.setProductId("product_id_1");
        model.setLunMapping(5);
        model.setSerial("some_serial");
        model.setVolumeGroupId("volume_group_id_1");
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
        assertEquals(entity.getStatus(), LunStatus.FREE);
    }

    @Test
    public void testStorageDomainMappings() {
        assertEquals(LunStatus.FREE, StorageLogicalUnitMapper.map(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Free, null));
        assertEquals(LunStatus.USED, StorageLogicalUnitMapper.map(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Used, null));
        assertEquals(LunStatus.UNUSABLE, StorageLogicalUnitMapper.map(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Unusable, null));
    }
}
