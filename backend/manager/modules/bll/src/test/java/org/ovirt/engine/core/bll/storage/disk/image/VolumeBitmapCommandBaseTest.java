package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.VolumeBitmapCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

class VolumeBitmapCommandBaseTest extends BaseCommandTest {

    private final Guid storagePoolId = Guid.newGuid();
    private final VdsmImageLocationInfo volInfo =
            new VdsmImageLocationInfo(Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), null);

    @Mock
    private ImagesHandler imagesHandler;

    @Mock
    private VdsCommandsHelper vdsCommandsHelper;

    @Mock
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Spy
    @InjectMocks
    private RemoveVolumeBitmapCommand<VolumeBitmapCommandParameters> command =
            new RemoveVolumeBitmapCommand<>(
                    new VolumeBitmapCommandParameters(storagePoolId, volInfo, "bitmap"), null);

    @Test
    public void executeFailsWhenVolumeInfoCannotBeRetrieved() {
        when(imagesHandler.getVolumeInfoFromVdsm(any(), any(), any(), any()))
                .thenThrow(new EngineException(EngineError.VolumeDoesNotExist));

        command.executeCommand();

        assertFalse(command.getReturnValue().getSucceeded());
        assertEquals(CommandStatus.FAILED, command.getCommandStatus());
        assertNull(command.getParameters().getVolInfo().getGeneration());
        verify(vdsCommandsHelper, never()).runVdsCommandWithFailover(any(), any(), any(), any());
    }

    @Test
    public void pollFailsWhenGenerationIsUnknown() {
        // The generation stays unknown when the command failed before submitting the job, so the entity
        // can't be polled; the job is reported as failed instead of undetermined, so it won't be fenced.
        // VdsmImagePoller is final and can't be mocked - it's left null, so any polling attempt would fail.
        assertEquals(HostJobStatus.failed, command.poll());
    }
}
