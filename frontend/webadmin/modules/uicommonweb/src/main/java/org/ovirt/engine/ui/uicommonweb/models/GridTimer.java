package org.ovirt.engine.ui.uicommonweb.models;

import java.util.logging.Logger;

import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.GridTimerStateChangeEvent.GridTimerStateChangeEventHandler;
import org.ovirt.engine.ui.uicommonweb.models.GridTimerStateChangeEvent.HasGridTimerStateChangeEventHandlers;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;

/**
 * The GridTimer holds information about the current refresh rate.
 * The GridTimer can work in one of two modes:
 *  1. Normal mode - in this mode the rate will be determined by selecting one out
 *     of 5 intervals (5,10,20,30,60 sec).
 *  2. Fast-Forward mode - in this mode the timer enters a cycle:
 *              - 3  fast (2 sec) refresh rotations
 *              - 30 medium (4 sec) refresh rotations
 *              - 3  slow (8 sec) refresh rotations
 *     After completing the cycle the GridTimer will return to Normal mode (with the last set
 *     refresh rate). This mode is triggered by the fastForward() method. each call reset the cycle
 *     to the start point.
 */
public abstract class GridTimer extends Timer implements HasGridTimerStateChangeEventHandlers {

    private enum RATE {
        FAST {
            @Override
            int getInterval() {
                return 500;
            }

            @Override
            int getRepetitions() {
                return 4;
            }

            @Override
            public String toString() {
                return "Fast"; //$NON-NLS-1$
            }

        },
        MEDIUM {
            @Override
            int getInterval() {
                return 1000;
            }

            @Override
            int getRepetitions() {
                return 3;
            }

            @Override
            public String toString() {
                return "Medium"; //$NON-NLS-1$
            }
        },
        NORMAL {

            @Override
            int getInterval() {
                return -1;
            }

            @Override
            int getRepetitions() {
                // this interval is set dynamically
                return -1;
            }

            @Override
            public String toString() {
                return "Normal"; //$NON-NLS-1$
            }
        },
        SLOW {
            @Override
            int getInterval() {
                return 2000;
            }

            @Override
            int getRepetitions() {
                return 2;
            }

            @Override
            public String toString() {
                return "Slow"; //$NON-NLS-1$
            }
        };

        abstract int getInterval();

        abstract int getRepetitions();

    }

    public static final int DEFAULT_NORMAL_RATE = ((Configurator) TypeResolver.getInstance()
            .resolve(Configurator.class)).getPollingTimerInterval();

    private static final Logger logger = Logger.getLogger(GridTimer.class.getName());

    private int currentRate = 0;

    private final EventBus eventBus;

    private final String name;

    private boolean active;

    private boolean paused;

    private int normalInterval = DEFAULT_NORMAL_RATE;

    private final RATE[] rateCycle = { RATE.NORMAL, RATE.FAST, RATE.MEDIUM, RATE.SLOW };

    private int repetitions;

    /**
     * Constructor.
     * @param name The name of the timer
     * @param eventBus The GWT event bus.
     */
    public GridTimer(String name, final EventBus eventBus) {
        assert eventBus != null : "EventBus cannot be null"; //$NON-NLS-1$
        this.name = name;
        this.eventBus = eventBus;
    }

    @Override
    public HandlerRegistration addGridTimerStateChangeEventHandler(GridTimerStateChangeEventHandler handler) {
        return eventBus.addHandler(GridTimerStateChangeEvent.getType(), handler);
    }

    /**
     * This method will be called when a timer fires. Override it to implement the timer's logic.
     */
    public abstract void execute();

    /**
     * Speed Up the search interval for a limited number of repetitions.
     */
    public void fastForward() {
        logger.fine("GridTimer[" + name + "].fastForward()"); //$NON-NLS-1$ //$NON-NLS-2$
        if (isFastForwarding()) {
            // there is already a fast forward running - reset to normal and start over
            reset();
        }
        cycleRate();
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    /**
     * Get the refresh rate.
     * @return refresh rate in milliseconds.
     */
    public int getRefreshRate() {
        RATE rate = rateCycle[currentRate];
        return rate == RATE.NORMAL ? normalInterval : rate.getInterval();
    }

    /**
     * Is this GridTimer currently running on Fast-Forward mode.
     * @return - true if running in Fast-Forward mode. false otherwise.
     */
    public boolean isFastForwarding() {
        return rateCycle[currentRate] != RATE.NORMAL;
    }

    @Override
    public final void run() {
        logger.fine("GridTimer[" + name + "].run() called"); //$NON-NLS-1$ //$NON-NLS-2$
        if (repetitions > 0) {
            repetitions--;
        } else if (repetitions == 0) {
            cycleRate();
        }
        logger.fine("GridTimer[" + name + "] Executing! Current Rate: " //$NON-NLS-1$ //$NON-NLS-2$
                + rateCycle[currentRate] + ":" + getRefreshRate() //$NON-NLS-1$
                + " Reps: " //$NON-NLS-1$
                + repetitions);
        execute();
    }

    /**
     * Set the refresh rate and fire an event that the state of the timer has changed.
     *
     * @param interval in seconds
     */
    public void setRefreshRate(int interval) {
        setRefreshRate(interval, true);
    }

    /**
     * Set the refresh rate and fire an event that the state of the timer has changed.
     *
     * @param interval in seconds
     */
    public void setRefreshRate(int interval, boolean fireEvent) {
        if (getRefreshRate() == interval) {
            return;
        }
        logger.fine("GridTimer[" + name + "]: Refresh Rate set to: " + interval); //$NON-NLS-1$ //$NON-NLS-2$
        // set the NORMAL interval
        normalInterval = interval;
        if (fireEvent) {
            GridTimerStateChangeEvent.fire(this, getRefreshRate());
        }
    }

    /**
     * Start an inactive timer.
     */
    public void start() {
        logger.fine("GridTimer[" + name + "].start()"); //$NON-NLS-1$ //$NON-NLS-2$
        active = true;
        scheduleRepeating(getRefreshRate());
    }

    /**
     * Stop an active timer.
     */
    public void stop() {
        logger.fine("GridTimer[" + name + "].stop()"); //$NON-NLS-1$ //$NON-NLS-2$
        active = false;
        doStop();
    }

    /**
     * Pause an active timer.
     */
    public void pause() {
        logger.fine("GridTimer[" + name + "].pause()"); //$NON-NLS-1$ //$NON-NLS-2$
        if (active) {
            paused = true;
            doStop();
        }
    }

    /**
     * Resume a paused timer, will NOT start an inactive timer.
     */
    public void resume() {
        logger.fine("GridTimer[" + name + "].resume()"); //$NON-NLS-1$ //$NON-NLS-2$
        if (active) {
            paused = false;
            start();
        }
    }

    /**
     * Is the timer paused?
     * @return {@code true} if paused, {@code false} otherwise
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Is the timer active?
     * @return {@code true} if active, {@code false} otherwise
     */
    public boolean isActive() {
        return active;
    }

    public String getTimerRefreshStatus() {
        logger.fine((isActive() ? "Refresh Status: Active(" : "Inactive(") + (isPaused() ? "paused)" : "running)") + ":" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                + " Rate: " + rateCycle[currentRate] + "(" + getRefreshRate() / 1000 + " sec)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$); }
        return ConstantsManager.getInstance().getMessages().refreshInterval(getRefreshRate() / 1000);
    }

    /**
     * Stop the timer, fire event with the new refresh rate.
     */
    private void doStop() {
        reset();
        cancel();
    }

    /**
     * Cycle the current refresh rate. The rate goes from FAST, MEDIUM, SLOW, NORMAL. Each rate has a number of
     * repetitions associated with it. Once those repetitions for that rate are exhausted the rate is switched to
     * the next rate until the rate is normal.
     */
    private void cycleRate() {
        currentRate = (currentRate + 1) % rateCycle.length;
        RATE rate = rateCycle[currentRate];
        repetitions = rate.getRepetitions();
        logger.fine("GridTimer[" + name + "] Rate Cycled: Current Rate: " + rate //$NON-NLS-1$ //$NON-NLS-2$
                + " Reps: " + repetitions + " Interval: " //$NON-NLS-1$ //$NON-NLS-2$
                + rate.getInterval());
        cancel();
        start();
        if (rate == RATE.NORMAL) {
            GridTimerStateChangeEvent.fire(this, getRefreshRate());
        }
    }

    /**
     * Reset the GridTimer rate back to normal.
     */
    private void reset() {
        // reset rate to NORMAL
        currentRate = 0;
        repetitions = RATE.NORMAL.getRepetitions();
    }

}
