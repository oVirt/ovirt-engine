package org.ovirt.engine.ui.common.uicommon;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;

/**
 * Not really a factory since it doesn't actually generate a timer but rather registers it in a list, this is done out
 * of convenience instead of having to instantiate a timer and then invoke addTimerToList on a static class.
 */
public abstract class TimerFactory {

    private static Map<String, Timer> timerList = new HashMap<>();
    private static final Logger logger = Logger.getLogger(TimerFactory.class.getName());

    public static Timer factoryTimer(String timerName, Timer timer) {
        logger.fine("Adding timer '" + timerName + "' to the timers list"); //$NON-NLS-1$ //$NON-NLS-2$
        timerList.put(timerName, timer);
        return timer;
    }

    public static void cancelAllTimers() {
        for (Map.Entry<String, Timer> stringTimerEntry : timerList.entrySet()) {
            logger.fine("Cancelling the timer '" + stringTimerEntry.getKey() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            stringTimerEntry.getValue().cancel();
        }
    }

    public static void cancelTimer(String timerName) {
        for (Map.Entry<String, Timer> timerEntry : timerList.entrySet()) {
            if (timerEntry.getKey().equals(timerName)) {
                logger.fine("Cancelling the timer '" + timerEntry.getKey() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                timerEntry.getValue().cancel();
            }
        }
    }

}
