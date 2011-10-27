package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.StorageDomainOwnerType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.compat.Guid;

public class ObjectStreamDeserializerTest {

    @Test
    public void testDeserialize() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            storage_pool_iso_map isoMap = new storage_pool_iso_map();
            isoMap.setstorage_id(Guid.NewGuid());
            isoMap.setstorage_pool_id(Guid.NewGuid());
            isoMap.setstatus(StorageDomainStatus.Active);
            isoMap.setowner(StorageDomainOwnerType.Unknown);
            new ObjectOutputStream(outputStream).writeObject(isoMap);
            byte[] bytes = outputStream.toByteArray();
            ObjectStreamDeserializer deserializer = new ObjectStreamDeserializer();
            storage_pool_iso_map readEntity = deserializer.deserialize(bytes, storage_pool_iso_map.class);
            assertEquals(isoMap, readEntity);
        } catch (Exception ex) {
            assertTrue(ex.getMessage(), false);
        }

    }
}
