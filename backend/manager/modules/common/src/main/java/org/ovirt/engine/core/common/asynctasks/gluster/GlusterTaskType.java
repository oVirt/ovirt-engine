package org.ovirt.engine.core.common.asynctasks.gluster;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.job.StepEnum;

public enum GlusterTaskType {
    REBALANCE(StepEnum.REBALANCING_VOLUME),
    REMOVE_BRICK(StepEnum.REMOVING_BRICKS),
    UNKNOWN(StepEnum.UNKNOWN);

    private StepEnum step;
    private static Map<StepEnum, GlusterTaskType> mappings;

    static {
        mappings = new HashMap<>();
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

     public static GlusterTaskType fromValue(String v) {
         try {
             return valueOf(v.toUpperCase());
         } catch (IllegalArgumentException e) {
             return GlusterTaskType.UNKNOWN;
         }
     }
}
