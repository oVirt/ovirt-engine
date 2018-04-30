package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ScanStorageForUnregisteredDisksCommandTest extends BaseCommandTest {
    @Mock
    private UnregisteredOVFDataDao unregisteredOVFDataDaoMock;

    @Mock
    private DiskImageDao diskImageDaoMock;

    @Spy
    @InjectMocks
    private ScanStorageForUnregisteredDisksCommand<StorageDomainParametersBase> cmd =
            new ScanStorageForUnregisteredDisksCommand<>
                    (new StorageDomainParametersBase(Guid.newGuid(), Guid.newGuid()), null);

    private Guid storageId = Guid.newGuid();

    private QueryReturnValue generateQueryReturnValueForGetDiskImages() {
        QueryReturnValue queryReturnValue = new QueryReturnValue();
        queryReturnValue.setSucceeded(true);
        return queryReturnValue;
    }

    @Test
    public void testExecuteGetAllEntitiesCommand() {
        cmd.executeCommand();
        assertTrue(cmd.getReturnValue().getSucceeded(), "return value should be true");
    }

    @BeforeEach
    public void setUpCommandEntities() {
        QueryReturnValue vdcRetVal = generateQueryReturnValueForGetDiskImages();
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
        doNothing().when(cmd).saveUnregisterDisk(any());
    }
}
