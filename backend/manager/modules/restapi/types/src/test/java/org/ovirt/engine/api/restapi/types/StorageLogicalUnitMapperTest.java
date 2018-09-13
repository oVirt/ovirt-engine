package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
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
        from.setType(StorageType.CINDER);
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
        assertEquals("vendor_id_1", entity.getVendorId());
        assertEquals("product_id_1", entity.getProductId());
        assertEquals("some_serial", entity.getSerial());
        assertEquals(Integer.valueOf(5), entity.getLunMapping());
        assertEquals("volume_group_id_1", entity.getVolumeGroupId());
        assertEquals(entity.getStorageDomainId(), Guid.Empty.toString());
        assertEquals(entity.getDiskId(), Guid.Empty.toString());
        assertEquals(LunStatus.FREE, entity.getStatus());
    }

    @Test
    public void testStorageDomainMappings() {
        assertEquals(LunStatus.FREE, StorageLogicalUnitMapper.map(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Free, null));
        assertEquals(LunStatus.USED, StorageLogicalUnitMapper.map(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Used, null));
        assertEquals(LunStatus.UNUSABLE, StorageLogicalUnitMapper.map(org.ovirt.engine.core.common.businessentities.storage.LunStatus.Unusable, null));
    }
}
