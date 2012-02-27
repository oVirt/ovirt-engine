package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;

/**
 * A class that provides for static mocking of ImportExportCommon through the use of Powermock. mockStatic can only be
 * called once per class and cannot be called from a static scope. Because of this, the mocking methods cannot be
 * declared as static and an instance of this class needs to be created. Consumers of this class need to use the
 * Powermock PrepareForTest annotation to prepare ImportExportCommon for static mocking.
 */
public class ImportExportCommonMocker {

    public ImportExportCommonMocker() {
        mockStatic(ImportExportCommon.class);
    }

    public void mockCheckStorageDomain(final boolean toReturn) {
        when(ImportExportCommon.CheckStorageDomain(any(Guid.class), any(ArrayList.class))).thenReturn(toReturn);
    }

    public void mockCheckStoragePool(final boolean toReturn) {
        when(ImportExportCommon.checkStoragePool(any(storage_pool.class), any(ArrayList.class))).thenReturn(toReturn);
    }

}
