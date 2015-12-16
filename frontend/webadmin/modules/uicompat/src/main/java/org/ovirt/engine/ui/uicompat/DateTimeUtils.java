package org.ovirt.engine.ui.uicompat;

import java.util.Date;

import org.ovirt.engine.core.compat.TimeSpan;

public class DateTimeUtils {

    /**
     * @return Object of type TimeSpan containing the time part of date.
     */
    public static TimeSpan getTimeOfDay(Date source) {
        return new TimeSpan(source.getHours(), source.getMinutes(), source.getSeconds());
    }
}
