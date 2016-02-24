package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.SPMGetVolumeInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

@RunWith(Theories.class)
public class DestroyImageCheckCommandTest {

    public static final List<Guid> ALL_VOLUMES = Arrays.asList(Guid.newGuid(), Guid.newGuid(), Guid.newGuid());

    @DataPoints
    public static List[] getFailedVolumes() {
        List<List<Guid>> result = new LinkedList<>();

        // All the single guids
        for (Guid guid : ALL_VOLUMES) {
            result.add(Collections.singletonList(guid));
        }

        // All pairs
        for (int i = 0; i < ALL_VOLUMES.size(); ++i) {
            List<Guid> pair = new LinkedList<>(ALL_VOLUMES);
            pair.remove(i);
            result.add(pair);
        }

        // All the volumes
        result.add(ALL_VOLUMES);

        return result.toArray(new List[result.size()]);
    }

    @Theory
    public void testGetFailedVolumeIds(List<Guid> failedVolumes) {
        DestroyImageParameters params =
                new DestroyImageParameters(Guid.newGuid(),
                        Guid.newGuid(),
                        Guid.newGuid(),
                        Guid.newGuid(),
                        Guid.newGuid(),
                        ALL_VOLUMES,
                        false,
                        false);

        DestroyImageCheckCommand<DestroyImageParameters> cmd = spy(new DestroyImageCheckCommand<>(params, null));

        for (Guid volumeId : ALL_VOLUMES) {
            SPMGetVolumeInfoVDSCommandParameters vdsParams =
                    new SPMGetVolumeInfoVDSCommandParameters(
                            params.getStoragePoolId(),
                            params.getStorageDomainId(),
                            params.getImageGroupId(),
                            volumeId);
            vdsParams.setExpectedEngineErrors(Collections.singleton(EngineError.VolumeDoesNotExist));

            if (failedVolumes.contains(volumeId)) {
                doReturn(null).when(cmd).runVdsCommand(eq(VDSCommandType.SPMGetVolumeInfo), refEq(vdsParams));
            } else {
                doThrow(new EngineException(EngineError.VolumeDoesNotExist)).when(cmd)
                        .runVdsCommand(eq(VDSCommandType.SPMGetVolumeInfo), refEq(vdsParams));
            }
        }

        assertEquals(cmd.getFailedVolumeIds(), failedVolumes);
    }
}
