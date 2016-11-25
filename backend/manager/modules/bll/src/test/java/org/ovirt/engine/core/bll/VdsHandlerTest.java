package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link VdsHandler} class. */
public class VdsHandlerTest {

    private VdsHandler vdsHandler = new VdsHandler();

    @Before
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
        assertTrue("Update should be valid for different names", updateIsValid);
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
        assertFalse("Update should not be valid for different server SSL enabled states",
                updateIsValid);
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
        assertFalse("Update should not be valid for different cluster IDs on a running host",
                updateIsValid);
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
        assertTrue("Update should be valid for different SSH ports in Down status",
                updateIsValid);
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
        assertTrue("Update should be valid for different names in down status",
                updateIsValid);
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
        assertFalse("Update should not be valid for different server SSL enabled states",
                updateIsValid);
    }
}
