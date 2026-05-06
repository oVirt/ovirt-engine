package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.ovirt.engine.core.bll.storage.backup.DeleteAllVmCheckpointsCommand;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DeleteAllVmCheckpointsParameters;
import org.ovirt.engine.core.common.action.VolumeBitmapCommandParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.compat.Guid;

public class DeleteAllVmCheckpointsCommandTest {

    @Test
    public void clearDiskBitmapsCallsInternalActionOnlyForQcow() {
        Guid vmId = Guid.newGuid();
        DeleteAllVmCheckpointsParameters params =
                new DeleteAllVmCheckpointsParameters(vmId, new ArrayList<>());
        DeleteAllVmCheckpointsCommand<DeleteAllVmCheckpointsParameters> cmd =
                spy(new DeleteAllVmCheckpointsCommand<>(params, null));

        doReturn(Guid.newGuid()).when(cmd).getStoragePoolId();

        ActionReturnValue ok = new ActionReturnValue();
        ok.setSucceeded(true);
        doReturn(ok).when(cmd).runInternalActionWithTasksContext(
                eq(ActionType.ClearVolumeBitmaps), any(VolumeBitmapCommandParameters.class));

        DiskImage raw = new DiskImage();
        raw.setVolumeFormat(VolumeFormat.RAW);
        raw.setId(Guid.newGuid());
        raw.setImageId(Guid.newGuid());
        raw.setStorageIds(List.of(Guid.newGuid()));

        DiskImage qcow = new DiskImage();
        qcow.setVolumeFormat(VolumeFormat.COW);
        qcow.setId(Guid.newGuid());
        qcow.setImageId(Guid.newGuid());
        qcow.setStorageIds(List.of(Guid.newGuid()));

        cmd.clearDiskBitmaps(List.of(raw, qcow));

        ArgumentCaptor<VolumeBitmapCommandParameters> captor =
                ArgumentCaptor.forClass(VolumeBitmapCommandParameters.class);

        verify(cmd, times(1)).runInternalActionWithTasksContext(
                eq(ActionType.ClearVolumeBitmaps), captor.capture());

        VolumeBitmapCommandParameters sent = captor.getValue();
        assertEquals(ActionParametersBase.EndProcedure.COMMAND_MANAGED, sent.getEndProcedure());
        assertEquals(params, sent.getParentParameters());
    }
}
