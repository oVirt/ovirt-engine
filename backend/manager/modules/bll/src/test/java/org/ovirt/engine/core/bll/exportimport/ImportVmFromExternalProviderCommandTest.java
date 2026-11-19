package org.ovirt.engine.core.bll.exportimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ImportVmFromExternalProviderParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmFromExternalProviderCommandTest extends BaseCommandTest {

    @Mock
    private BackendInternal backendInternal;

    private final ImportVmFromExternalProviderParameters parameters =
            new ImportVmFromExternalProviderParameters(new VM(), Guid.newGuid(), Guid.newGuid(), Guid.newGuid());

    @Spy
    @InjectMocks
    private ImportVmFromExternalProviderCommand<ImportVmFromExternalProviderParameters> cmd =
            new ImportVmFromExternalProviderCommand<>(parameters, CommandContext.createContext(""));

    @Test
    public void renameVmdkImage() {
        String alias = ImportVmFromExternalProviderCommand.renameDiskAlias(OriginType.VMWARE, "[datastore] Fedora21/Fedora21.vmdk");
        assertEquals("Fedora21", alias);
    }

    @Test
    public void renameXenImage() {
        String alias = ImportVmFromExternalProviderCommand.renameDiskAlias(OriginType.XEN, "/home/vdsm/Fedora22.img");
        assertEquals("Fedora22", alias);
    }

    @Test
    public void renameImageWithSpecialChars() {
        String alias = ImportVmFromExternalProviderCommand.renameDiskAlias(OriginType.VMWARE, "[datastore] Fedora21/Fedora 21-yy%2fmm.vmdk");
        assertEquals("Fedora_21-yy_2fmm", alias);
    }

    private void mockDiskQuery(Guid diskId, DiskImage disk) {
        QueryReturnValue returnValue = new QueryReturnValue();
        returnValue.setSucceeded(true);
        returnValue.setReturnValue(disk);
        when(backendInternal.runInternalQuery(eq(QueryType.GetDiskByDiskId),
                argThat(params -> params instanceof IdQueryParameters
                        && diskId.equals(((IdQueryParameters) params).getId())),
                any()))
                .thenReturn(returnValue);
    }

    private static DiskImage createDisk() {
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        return disk;
    }

    @Test
    public void getDisksReturnsImportedDisksInOrder() {
        DiskImage disk1 = createDisk();
        DiskImage disk2 = createDisk();
        mockDiskQuery(disk1.getId(), disk1);
        mockDiskQuery(disk2.getId(), disk2);
        parameters.setDisks(new ArrayList<>(Arrays.asList(disk1.getId(), disk2.getId())));

        assertEquals(Arrays.asList(disk1, disk2), cmd.getDisks());
    }

    @Test
    public void getDisksFailsWhenImportedDiskIsMissing() {
        DiskImage disk = createDisk();
        Guid missingDiskId = Guid.newGuid();
        mockDiskQuery(disk.getId(), disk);
        mockDiskQuery(missingDiskId, null);
        parameters.setDisks(new ArrayList<>(Arrays.asList(disk.getId(), missingDiskId)));

        EngineException e = assertThrows(EngineException.class, () -> cmd.getDisks());
        assertTrue(e.getMessage().contains(missingDiskId.toString()), e.getMessage());
    }
}
