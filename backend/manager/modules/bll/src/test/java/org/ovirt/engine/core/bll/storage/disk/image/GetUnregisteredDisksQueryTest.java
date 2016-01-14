package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;

@RunWith(MockitoJUnitRunner.class)
public class GetUnregisteredDisksQueryTest
        extends
        AbstractQueryTest<GetUnregisteredDisksQueryParameters, GetUnregisteredDisksQuery<GetUnregisteredDisksQueryParameters>> {

    private Guid importDiskId;
    private Guid existingDiskId;
    private Guid storageDomainId;
    private Guid storagePoolId;

    private List<Guid> importDiskIds;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        importDiskId = Guid.newGuid();
        existingDiskId = Guid.newGuid();
        storageDomainId = Guid.newGuid();
        storagePoolId = Guid.newGuid();
        // Wrapping the list in a new ArrayList as this will eventually be modified by the GetUnregisteredDisksQuery command and
        // Arrays returned by Arrays.asList are immutable. The wrapping allows for mutability.
        importDiskIds = new ArrayList<>(Arrays.asList(importDiskId, existingDiskId));
        prepareMocks();
    }

    @Test
    public void testGetUnregisteredDisks() {
        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        StorageDomainDao storageDomainDaoMock = mock(StorageDomainDao.class);
        when(dbFacadeMock.getStorageDomainDao()).thenReturn(storageDomainDaoMock);
        StorageDomain storageDomain = new StorageDomain();
        when(storageDomainDaoMock.get(storageDomainId)).thenReturn(storageDomain);

        // Execute query
        getQuery().executeQueryCommand();

        // Assert the query's results
        @SuppressWarnings("unchecked")
        List<Disk> newDisks = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, newDisks.size());
        assertEquals(importDiskId, newDisks.get(0).getId());
    }

    /**
     * Mock the DbFacade/VDSBroker and the Daos
     */
    private void prepareMocks() {
        BackendInternal backendMock = mock(BackendInternal.class);
        VDSBrokerFrontend vdsBroker = mock(VDSBrokerFrontend.class);

        DiskImage existingDiskImage = mock(DiskImage.class);
        when(existingDiskImage.getId()).thenReturn(existingDiskId);
        List<DiskImage> existingDiskImages = Collections.singletonList(existingDiskImage);

        // Mock the get images List VDS command
        VDSReturnValue volListReturnValue = new VDSReturnValue();
        volListReturnValue.setSucceeded(true);
        volListReturnValue.setReturnValue(importDiskIds);
        doReturn(volListReturnValue).when(vdsBroker).runVdsCommand(eq(VDSCommandType.GetImagesList),
                any(GetImagesListVDSCommandParameters.class));

        // Mock the get unregistered disk query
        when(backendMock.runInternalQuery(eq(VdcQueryType.GetUnregisteredDisk), any(GetUnregisteredDiskQueryParameters.class), any(EngineContext.class)))
                .thenAnswer(invocation -> {
                    GetUnregisteredDiskQueryParameters p = (GetUnregisteredDiskQueryParameters) invocation
                            .getArguments()[1];
                    VdcQueryReturnValue unregDiskReturnValue = new VdcQueryReturnValue();
                    unregDiskReturnValue.setSucceeded(true);
                    DiskImage newDiskImage = mock(DiskImage.class);
                    when(newDiskImage.getId()).thenReturn(p.getDiskId());
                    unregDiskReturnValue.setReturnValue(newDiskImage);
                    return unregDiskReturnValue;
                });

        doReturn(storagePoolId).when(getQuery()).getStoragePoolId();
        doReturn(storageDomainId).when(getQuery()).getStorageDomainId();

        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        DiskImageDao diskImageDaoMock = mock(DiskImageDao.class);
        StorageDomainDao storageDomainDaoMock = mock(StorageDomainDao.class);
        when(diskImageDaoMock.getAllSnapshotsForStorageDomain(eq(storageDomainId))).thenReturn(existingDiskImages);
        when(dbFacadeMock.getDiskImageDao()).thenReturn(diskImageDaoMock);
        StorageDomain storageDomain = new StorageDomain();
        when(storageDomainDaoMock.getForStoragePool(storageDomainId, storagePoolId)).thenReturn(storageDomain);

        // Return the mocked backend when getBackend() is called on the query
        doReturn(backendMock).when(getQuery()).getBackend();

        // Return the mocked vdsBroker when getVDSBroker() is called on the query
        doReturn(vdsBroker).when(getQuery()).getVdsBroker();
    }
}
