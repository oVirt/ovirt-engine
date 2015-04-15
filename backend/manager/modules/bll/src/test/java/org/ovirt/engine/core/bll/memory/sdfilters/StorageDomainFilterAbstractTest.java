package org.ovirt.engine.core.bll.memory.sdfilters;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainFilterAbstractTest {

    protected StorageDomain storageDomain;
    protected List<DiskImage> disksList;

    @Before
    public void setUp() {
        storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
        disksList = new LinkedList<>();
    }
}
