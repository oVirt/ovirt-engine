package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.StorageFormat;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;

public class StorageFormatMapperTest {

    @Test
    public void testMapping() {
        StorageFormat storageFormat = StorageFormatMapper.map(StorageFormatType.V1, null);
        assertEquals (StorageFormat.V1, storageFormat);
        StorageFormatType storageFormatType = StorageFormatMapper.map(storageFormat, null);
        assertEquals (StorageFormatType.V1, storageFormatType);

        storageFormat = StorageFormatMapper.map(StorageFormatType.V2, null);
        assertEquals (StorageFormat.V2, storageFormat);
        storageFormatType = StorageFormatMapper.map(storageFormat, null);
        assertEquals (StorageFormatType.V2, storageFormatType);
    }
}
