package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AsyncTaskStatusEnum")
public enum AsyncTaskStatusEnum {
    // unknown: task doesn't exist.
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    unknown(0),
    // init: task hasn't started yet.
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    init(1),
    // working: task is running.
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    running(2),
    // finished: task has ended successfully.
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    finished(3),
    // aborting: task has ended with failure.
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    aborting(4),
    // cleaning: clean-up is being done due to 'stopTask' request or failed
    // task.
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    cleaning(5);

    private int intValue;
    private static java.util.HashMap<Integer, AsyncTaskStatusEnum> mappings;

    private synchronized static java.util.HashMap<Integer, AsyncTaskStatusEnum> getMappings() {
        if (mappings == null) {
            mappings = new java.util.HashMap<Integer, AsyncTaskStatusEnum>();
        }
        return mappings;
    }

    private AsyncTaskStatusEnum(int value) {
        intValue = value;
        AsyncTaskStatusEnum.getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static AsyncTaskStatusEnum forValue(int value) {
        return getMappings().get(value);
    }
}
