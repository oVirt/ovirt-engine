package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;

public abstract class GridTimer extends Timer implements HasValueChangeHandlers<String> {

    private enum RATE {
        FAST {
            @Override
            int getInterval() {
                return 2000;
            }

            @Override
            int getRepetitions() {
                return 3;
            }

            @Override
            public String toString() {
                return "Fast";
            }

        },
        MEDIUM {
            @Override
            int getInterval() {
                return 4000;
            }

            @Override
            int getRepetitions() {
                return 30;
            }

            @Override
            public String toString() {
                return "Medium";
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
                return "Normal";
            }
        },
        SLOW {
            @Override
            int getInterval() {
                return 8000;
            }

            @Override
            int getRepetitions() {
                return 3;
            }

            @Override
            public String toString() {
                return "Slow";
            }
        };

        abstract int getInterval();

        abstract int getRepetitions();

    }

    public static final int DEFAULT_NORMAL_RATE = ((Configurator) TypeResolver.getInstance()
            .Resolve(Configurator.class)).getPollingTimerInterval();

    private int currentRate = 0;

    private final SimpleEventBus eventBus;

    private final String name;

    private boolean active;

    private boolean paused;

    private int normalInterval = DEFAULT_NORMAL_RATE;

    private final RATE[] rateCycle = { RATE.NORMAL, RATE.FAST, RATE.MEDIUM, RATE.SLOW };

    private int repetitions;

    public GridTimer(String name) {
        this.name = name;
        eventBus = new SimpleEventBus();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return eventBus.addHandler(ValueChangeEvent.getType(), handler);
    }

    /**
     * This method will be called when a timer fires. Override it to implement the timer's logic.
     */
    public abstract void execute();

    /**
     * Speed Up the search interval for a limited number of repetitions.
     */
    public void fastForward() {
        GWT.log("GridTimer[" + name + "].fastForward()");
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
     * get the refresh rate in seconds
     */
    public int getRefreshRate() {
        RATE rate = rateCycle[currentRate];
        return rate == RATE.NORMAL ? normalInterval : rate.getInterval();
    }

    public boolean isFastForwarding() {
        return rateCycle[currentRate] != RATE.NORMAL;
    }

    @Override
    public final void run() {
        GWT.log("GridTimer[" + name + "].run() called");
        if (repetitions > 0) {
            repetitions--;
        } else if (repetitions == 0) {
            cycleRate();
        }
        GWT.log("GridTimer[" + name + "] Executing! Current Rate: " + rateCycle[currentRate] + ":" + getRefreshRate()
                + " Reps: "
                + repetitions);
        execute();
    }

    /**
     * Set the refresh rate. Stops a fast-forward
     *
     * @param interval
     *            in seconds
     */
    public void setRefreshRate(int interval) {
        if (getRefreshRate() == interval) {
            return;
        }
        reset();
        GWT.log("GridTimer[" + name + "]: Refresh Rate set to: " + interval);
        // set the NORMAL interval
        normalInterval = interval;
        start();
    }

    public void start() {
        GWT.log("GridTimer[" + name + "].start()");
        active = true;
        scheduleRepeating(getRefreshRate());
        ValueChangeEvent.fire(this, getValue());
    }

    public void stop() {
        GWT.log("GridTimer[" + name + "].stop()");
        active = false;
        doStop();
    }

    public void pause() {
        GWT.log("GridTimer[" + name + "].pause()");
        if (active) {
            paused = true;
            doStop();
        }
    }

    public void resume() {
        GWT.log("GridTimer[" + name + "].resume()");
        if (active) {
            paused = false;
            start();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isActive() {
        return active;
    }

    private String getValue() {
        return (isActive() ? "Refresh Status: Active(" : "Inactive(") + (isPaused() ? "paused)" : "running)") + ":"
                + " Rate: " + rateCycle[currentRate] + "(" + getRefreshRate() / 1000 + " sec)";
    }

    private void doStop() {
        reset();
        cancel();
        ValueChangeEvent.fire(this, getValue());
    }

    private void cycleRate() {
        currentRate = (currentRate + 1) % rateCycle.length;
        RATE rate = rateCycle[currentRate];
        repetitions = rate.getRepetitions();
        GWT.log("GridTimer[" + name + "] Rate Cycled: Current Rate: " + rate + " Reps: " + repetitions + " Interval: "
                + rate.getInterval());
        start();
    }

    private void reset() {
        // reset rate to NORMAL
        currentRate = 0;
        repetitions = RATE.NORMAL.getRepetitions();
    }

}
