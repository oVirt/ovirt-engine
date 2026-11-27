package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.compat.Guid;

class FenceVolumeJobCommandTest extends BaseCommandTest {

    @Mock
    private VdsCommandsHelper vdsCommandsHelper;

    @Mock
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Spy
    @InjectMocks
    private FenceVolumeJobCommand<FenceVolumeJobCommandParameters> command =
            new FenceVolumeJobCommand<>(new FenceVolumeJobCommandParameters(
                    new VdsmImageLocationInfo(Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), null)), null);

    @Test
    public void executeFailsWhenGenerationIsUnknown() {
        command.executeCommand();

        assertFalse(command.getReturnValue().getSucceeded());
        verify(vdsCommandsHelper, never()).runVdsCommandWithoutFailover(any(), any(), any(), any());
    }
}
