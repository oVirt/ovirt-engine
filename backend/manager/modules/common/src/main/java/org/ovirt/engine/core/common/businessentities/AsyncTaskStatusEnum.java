package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AsyncTaskStatusEnum")
public enum AsyncTaskStatusEnum {
    // unknown: task doesn't exist.
    unknown,
    // init: task hasn't started yet.
    init,
    // working: task is running.
    running,
    // finished: task has ended successfully.
    finished,
    // aborting: task has ended with failure.
    aborting,
    // cleaning: clean-up is being done due to 'stopTask' request or failed
    // task.
    cleaning;

    public int getValue() {
        return this.ordinal();
    }

    public static AsyncTaskStatusEnum forValue(int value) {
        return values()[value];
    }
}
