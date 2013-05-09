package org.ovirt.engine.api.restapi.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;

public class StorageFormatMapperTest {

    @Test
    public void testMapping() {
        StorageFormat storageFormat = StorageFormatMapper.map(StorageFormatType.V1, (StorageFormat)null);
        assertEquals (storageFormat, StorageFormat.V1);
        StorageFormatType storageFormatType = StorageFormatMapper.map(storageFormat, (StorageFormatType)null);
        assertEquals (storageFormatType, StorageFormatType.V1);

        storageFormat = StorageFormatMapper.map(StorageFormatType.V2, (StorageFormat)null);
        assertEquals (storageFormat, StorageFormat.V2);
        storageFormatType = StorageFormatMapper.map(storageFormat, (StorageFormatType)null);
        assertEquals (storageFormatType, StorageFormatType.V2);
    }
}
