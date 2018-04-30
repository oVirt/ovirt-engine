package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link VdsHandler} class. */
public class VdsHandlerTest {

    private VdsHandler vdsHandler = new VdsHandler();

    @BeforeEach
    public void setUp() {
        vdsHandler.init();
    }

    @Test
    public void testValidUpdateOfEditableFieldOnRunningHost() {
        // Given
        VdsStatic src = new VdsStatic();
        src.setName(RandomUtils.instance().nextString(10));
        VdsStatic dest = new VdsStatic();
        dest.setName(RandomUtils.instance().nextString(10));

        // When
        boolean updateIsValid = vdsHandler.isUpdateValid(src, dest, VDSStatus.Up);

        // Then
        assertTrue(updateIsValid, "Update should be valid for different names");
    }

    @Test
    public void testInvalidUpdateOfNonEditableFieldOnRunningHost() {
        // Given
        VdsStatic src = new VdsStatic();
        src.setServerSslEnabled(true);
        VdsStatic dest = new VdsStatic();
        dest.setServerSslEnabled(false);

        // When
        boolean updateIsValid = vdsHandler.isUpdateValid(src, dest, VDSStatus.Up);

        // Then
        assertFalse(
                updateIsValid, "Update should not be valid for different server SSL enabled states");
    }

    @Test
    public void testInvalidUpdateOfStatusRestrictedEditableFieldOnRunningHost() {
        // Given
        VdsStatic src = new VdsStatic();
        src.setClusterId(Guid.newGuid());
        VdsStatic dest = new VdsStatic();
        dest.setClusterId(Guid.newGuid());

        // When
        boolean updateIsValid = vdsHandler.isUpdateValid(src, dest, VDSStatus.Up);

        // Then
        assertFalse(
                updateIsValid, "Update should not be valid for different cluster IDs on a running host");
    }

    @Test
    public void testValidUpdateOfStatusRestrictedEditableFieldOnDownHost() {
        // Given
        int srcSshPort = 22;
        int destSshPort = 23;
        VdsStatic src = new VdsStatic();
        src.setSshPort(srcSshPort);
        VdsStatic dest = new VdsStatic();
        dest.setSshPort(destSshPort);

        // When
        boolean updateIsValid = vdsHandler.isUpdateValid(src, dest, VDSStatus.Down);

        // Then
        assertTrue(
                updateIsValid, "Update should be valid for different SSH ports in Down status");
    }

    @Test
    public void testValidUpdateOfEditableFieldOnDownHost() {
        // Given
        VdsStatic src = new VdsStatic();
        src.setName(RandomUtils.instance().nextString(10));
        VdsStatic dest = new VdsStatic();
        dest.setName(RandomUtils.instance().nextString(10));

        // When
        boolean updateIsValid = vdsHandler.isUpdateValid(src, dest, VDSStatus.Down);

        // Then
        assertTrue(
                updateIsValid, "Update should be valid for different names in down status");
    }

    @Test
    public void testUpdateNonEditableFieldOnDownHost() {
        // Given
        VdsStatic src = new VdsStatic();
        src.setServerSslEnabled(true);
        VdsStatic dest = new VdsStatic();
        dest.setServerSslEnabled(false);

        // When
        boolean updateIsValid = vdsHandler.isUpdateValid(src, dest, VDSStatus.Down);

        // Then
        assertFalse(
                updateIsValid, "Update should not be valid for different server SSL enabled states");
    }
}
