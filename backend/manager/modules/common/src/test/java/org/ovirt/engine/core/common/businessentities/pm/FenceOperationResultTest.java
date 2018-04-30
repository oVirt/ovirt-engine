package org.ovirt.engine.core.common.businessentities.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.pm.FenceOperationResult.Status;

public class FenceOperationResultTest {
    /**
     * Test result value for successful status action of host powered on
     */
    @Test
    public void successfulStatusOn() {
        FenceOperationResult result = new FenceOperationResult(
                FenceActionType.STATUS,
                0,
                null,
                "on",
                null);

        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(PowerStatus.ON, result.getPowerStatus());
    }

    /**
     * Test result value for successful status action of host powered off
     */
    @Test
    public void successfulStatusOff() {
        FenceOperationResult result = new FenceOperationResult(
                FenceActionType.STATUS,
                0,
                null,
                "off",
                null);

        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(PowerStatus.OFF, result.getPowerStatus());
    }

    /**
     * Test result value for failed status action
     */
    @Test
    public void failedStatus() {
        FenceOperationResult result = new FenceOperationResult(
                FenceActionType.STATUS,
                1,
                null,
                "unknown",
                null);

        assertEquals(Status.ERROR, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }

    /**
     * Test result value for successful stop action
     */
    @Test
    public void successfulStop() {
        FenceOperationResult result = new FenceOperationResult(
                FenceActionType.STOP,
                0,
                null,
                null,
                "initiated");

        assertEquals(Status.SUCCESS, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }

    /**
     * Test result value for failed stop action
     */
    @Test
    public void failedStop() {
        FenceOperationResult result = new FenceOperationResult(
                FenceActionType.STOP,
                1,
                null,
                null,
                "initiated");

        assertEquals(Status.ERROR, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }

    /**
     * Test result value for skipped stop action due to fencing policy
     */
    @Test
    public void skippedDueToPolicyStop() {
        FenceOperationResult result = new FenceOperationResult(
                FenceActionType.STOP,
                0,
                null,
                null,
                "skipped");

        assertEquals(Status.SKIPPED_DUE_TO_POLICY, result.getStatus());
        assertEquals(PowerStatus.UNKNOWN, result.getPowerStatus());
    }
}
