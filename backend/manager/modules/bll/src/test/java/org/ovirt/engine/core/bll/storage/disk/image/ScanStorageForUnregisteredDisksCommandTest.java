package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class ScanStorageForUnregisteredDisksCommandTest extends BaseCommandTest {

    @Mock
    private UnregisteredDisksDao unregisteredDisksDaoMock;

    @Mock
    private UnregisteredOVFDataDao unregisteredOVFDataDaoMock;

    @Mock
    private DiskImageDao diskImageDaoMock;

    @Spy
    @InjectMocks
    private ScanStorageForUnregisteredDisksCommand<StorageDomainParametersBase> cmd =
            new ScanStorageForUnregisteredDisksCommand<>
                    (new StorageDomainParametersBase(Guid.newGuid(), Guid.newGuid()), null);

    private DiskImage diskImageForTest = new DiskImage();
    private Guid storageId = Guid.newGuid();

    private VdcQueryReturnValue generateQueryReturnValueForGetDiskImages(boolean succeeded,
            List<DiskImage> disksFromStorage,
            String exceptionString) {
        VdcQueryReturnValue vdcQueryReturnValue = new VdcQueryReturnValue();
        vdcQueryReturnValue.setSucceeded(succeeded);
        if (!succeeded) {
            vdcQueryReturnValue.setReturnValue(disksFromStorage);
        } else {
            vdcQueryReturnValue.setExceptionString(exceptionString);
        }
        return vdcQueryReturnValue;
    }

    private void createDiskForTest(DiskImage diskImage) {
        diskImage.setDiskAlias("Disk Alias");
        diskImage.setDiskDescription("Disk Description");
        diskImage.setSizeInGigabytes(10L * 1024 * 1024 * 1024);
        diskImage.setActualSize(10L * 1024 * 1024 * 1024);
        diskImage.setId(Guid.newGuid());
        diskImage.setImageId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(storageId)));
    }

    @Test
    public void testExecuteGetAllEntitiesCommand() throws OvfReaderException, IOException {
        cmd.executeCommand();
        assertTrue("return value should be true", cmd.getReturnValue().getSucceeded());
    }

    @Before
    public void setUpCommandEntities() throws OvfReaderException {
        List<DiskImage> disksFromStorage = new ArrayList<>();
        createDiskForTest(diskImageForTest);
        disksFromStorage.add(diskImageForTest);

        VdcQueryReturnValue vdcRetVal = generateQueryReturnValueForGetDiskImages(true, disksFromStorage, "Exception");
        doReturn(vdcRetVal).when(cmd).getUnregisteredDisksFromHost();

        List<OvfEntityData> allEntities = new ArrayList<>();
        OvfEntityData ovf = new OvfEntityData();
        ovf.setEntityId(Guid.newGuid());
        ovf.setEntityName("Any Name");
        allEntities.add(ovf);
        when(unregisteredOVFDataDaoMock.getAllForStorageDomainByEntityType(storageId, null)).thenReturn(allEntities);
        doNothing().when(cmd).setVmsForUnregisteredDisks(allEntities);
        when(unregisteredOVFDataDaoMock.getAllForStorageDomainByEntityType(storageId, null)).thenReturn(allEntities);
        doNothing().when(cmd).removeUnregisteredDisks();
        doNothing().when(cmd).saveUnregisterDisk(anyObject());
    }
}
