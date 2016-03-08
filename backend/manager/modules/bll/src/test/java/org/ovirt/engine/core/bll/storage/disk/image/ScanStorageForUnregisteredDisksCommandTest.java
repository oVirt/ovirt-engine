package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
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

    private ScanStorageForUnregisteredDisksCommand<StorageDomainParametersBase> cmd;

    DiskImage diskImageForTest = new DiskImage();
    Guid storageId = Guid.newGuid();

    public void mockCommand() throws IOException, OvfReaderException {
        mockCommandParameters();
        setUpCommandEntities();
    }

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
        diskImage.setSizeInGigabytes(10 * 1024 * 1024 * 1024);
        diskImage.setActualSize(10 * 1024 * 1024 * 1024);
        diskImage.setId(Guid.newGuid());
        diskImage.setImageId(Guid.newGuid());
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(storageId)));
    }

    @Test
    public void testExecuteGetAllEntitiesCommand() throws OvfReaderException, IOException {
        createCommand();
        mockCommand();
        cmd.executeCommand();
        assertEquals("return value should be true", true, cmd.getReturnValue().getSucceeded());
    }

    private void mockCommandParameters() {
        // Mock the command Parameters
        doReturn(unregisteredDisksDaoMock).when(cmd).getUnregisteredDisksDao();
        doReturn(unregisteredOVFDataDaoMock).when(cmd).getUnregisteredOVFDataDao();
        doReturn(diskImageDaoMock).when(cmd).getDiskImageDao();
    }

    private void setUpCommandEntities() throws OvfReaderException {
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

    private void createCommand() {
        StorageDomainParametersBase params = new StorageDomainParametersBase();
        params.setStorageDomainId(Guid.newGuid());
        params.setStoragePoolId(Guid.newGuid());
        cmd = spy(new ScanStorageForUnregisteredDisksCommand<>(params, null));
    }
}
