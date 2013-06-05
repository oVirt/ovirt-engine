package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.compat.Guid;

public class ObjectStreamSerializerTest {

    @Test
    public void testSerialize() {
        try {
            ObjectStreamSerializer serializer = new ObjectStreamSerializer();
            StoragePoolIsoMap isoMap = new StoragePoolIsoMap();
            isoMap.setstorage_id(Guid.newGuid());
            isoMap.setstorage_pool_id(Guid.newGuid());
            isoMap.setstatus(StorageDomainStatus.Active);
            byte[] bytes = (byte[]) serializer.serialize(isoMap);
            StoragePoolIsoMap readEntity =
                    (StoragePoolIsoMap) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
            assertEquals(isoMap, readEntity);
        } catch (Exception ex) {
            assertTrue(ex.getMessage(), false);
        }

    }
}
