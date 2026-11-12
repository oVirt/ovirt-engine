package org.ovirt.engine.core.bll.exportimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;

public class OvaImportUtilsTest {

    @Test
    public void mapTarNamesByDiskIdPairsByPosition() {
        Guid disk1 = Guid.newGuid();
        Guid disk2 = Guid.newGuid();
        Guid tar1 = Guid.newGuid();
        Guid tar2 = Guid.newGuid();

        Map<Guid, Guid> result = OvaImportUtils.mapTarNamesByDiskId(
                Arrays.asList(disk1, disk2), Arrays.asList(tar1, tar2));

        assertEquals(Arrays.asList(disk1, disk2), new ArrayList<>(result.keySet()));
        assertEquals(tar1, result.get(disk1));
        assertEquals(tar2, result.get(disk2));
    }

    @Test
    public void mapTarNamesByDiskIdFailsOnSizeMismatch() {
        EngineException e = assertThrows(EngineException.class,
                () -> OvaImportUtils.mapTarNamesByDiskId(
                        Collections.singletonList(Guid.newGuid()),
                        Arrays.asList(Guid.newGuid(), Guid.newGuid())));

        assertTrue(e.getMessage().contains("(1)"), e.getMessage());
        assertTrue(e.getMessage().contains("(2)"), e.getMessage());
    }

    @Test
    public void mapTarNamesByDiskIdFailsOnNullDiskId() {
        List<Guid> diskIds = Arrays.asList(Guid.newGuid(), null);

        EngineException e = assertThrows(EngineException.class,
                () -> OvaImportUtils.mapTarNamesByDiskId(diskIds, Arrays.asList(Guid.newGuid(), Guid.newGuid())));

        assertTrue(e.getMessage().contains("disk #1"), e.getMessage());
    }
}
