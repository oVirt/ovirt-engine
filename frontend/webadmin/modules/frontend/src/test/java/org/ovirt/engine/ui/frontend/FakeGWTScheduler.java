package org.ovirt.engine.ui.frontend;

import com.google.gwt.core.client.Scheduler;

/**
 * This is a fake GWT Scheduler class to simulate the deferred scheduling of commands during GWT runtime. It allows
 * one to set how many times the schedule has to be called before actually executing the command.
 */
public class FakeGWTScheduler extends Scheduler {

    /**
     * Count the number of times scheduleDeferred is called.
     */
    private int callCount = 0;

    /**
     * The threshold for executing a deferred command. If the call count is greater or equals to the threshold. The
     * {@code ScheduledCommand} is executed immediately. This allows us to simulate several calls the scheduler
     * without actually implementing the scheduler.
     */
    private int threshold = 0;

    /**
     * Set the threshold that needs to be matched before executing the command.
     * @param thresholdCount The threshold.
     */
    public void setThreshold(final int thresholdCount) {
        threshold = thresholdCount;
    }

    @Override
    public void scheduleDeferred(ScheduledCommand cmd) {
        callCount++;
        if (callCount >= threshold) {
            // threshold matched, execute the command.
            cmd.execute();
        }
    }

    @Override
    public void scheduleEntry(RepeatingCommand cmd) {
        throw new RuntimeException("Not implemented"); //$NON-NLS-1$
    }

    @Override
    public void scheduleEntry(ScheduledCommand cmd) {
        throw new RuntimeException("Not implemented"); //$NON-NLS-1$
    }

    @Override
    public void scheduleFinally(RepeatingCommand cmd) {
        throw new RuntimeException("Not implemented"); //$NON-NLS-1$
    }

    @Override
    public void scheduleFinally(ScheduledCommand cmd) {
        throw new RuntimeException("Not implemented"); //$NON-NLS-1$
    }

    @Override
    public void scheduleFixedDelay(RepeatingCommand cmd, int delayMs) {
        throw new RuntimeException("Not implemented"); //$NON-NLS-1$
    }

    @Override
    public void scheduleFixedPeriod(RepeatingCommand cmd, int delayMs) {
        throw new RuntimeException("Not implemented"); //$NON-NLS-1$
    }

    @Override
    public void scheduleIncremental(RepeatingCommand cmd) {
        throw new RuntimeException("Not implemented"); //$NON-NLS-1$
    }

    /**
     * Reset the call count.
     */
    public void resetCount() {
        this.callCount = 0;
    }

}
