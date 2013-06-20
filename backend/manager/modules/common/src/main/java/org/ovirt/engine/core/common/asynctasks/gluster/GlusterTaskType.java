package org.ovirt.engine.core.common.asynctasks.gluster;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.job.StepEnum;

public enum GlusterTaskType {
    REBALANCE_VOLUME(StepEnum.REBALANCING_VOLUME),
    ;

    private StepEnum step;
    private static Map<StepEnum, GlusterTaskType> mappings;

    static {
        mappings = new HashMap<StepEnum, GlusterTaskType>();
        for (GlusterTaskType value : values()) {
            mappings.put(value.getStep(), value);
        }
    }

    private GlusterTaskType(StepEnum stepType) {
        this.step = stepType;
    }

    public StepEnum getStep() {
        return step;
    }

     public static GlusterTaskType forValue(StepEnum step) {
        return mappings.get(step);
    }
}
