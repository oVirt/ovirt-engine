package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.compat.Guid;

public class ObjectStreamDeserializerTest {

    @Test
    public void testDeserialize() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StoragePoolIsoMap isoMap = new StoragePoolIsoMap();
            isoMap.setstorage_id(Guid.NewGuid());
            isoMap.setstorage_pool_id(Guid.NewGuid());
            isoMap.setstatus(StorageDomainStatus.Active);
            new ObjectOutputStream(outputStream).writeObject(isoMap);
            byte[] bytes = outputStream.toByteArray();
            ObjectStreamDeserializer deserializer = new ObjectStreamDeserializer();
            StoragePoolIsoMap readEntity = deserializer.deserialize(bytes, StoragePoolIsoMap.class);
            assertEquals(isoMap, readEntity);
        } catch (Exception ex) {
            assertTrue(ex.getMessage(), false);
        }

    }
}
