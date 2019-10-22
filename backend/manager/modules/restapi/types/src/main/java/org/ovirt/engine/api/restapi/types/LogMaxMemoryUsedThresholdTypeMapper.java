/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.LogMaxMemoryUsedThresholdType;

public class LogMaxMemoryUsedThresholdTypeMapper {
    public static org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType mapFromModel(
            LogMaxMemoryUsedThresholdType logMaxMemoryUsedThresholdType) {
        switch (logMaxMemoryUsedThresholdType) {
            case PERCENTAGE:
                return org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType.PERCENTAGE;
            case ABSOLUTE_VALUE_IN_MB:
                return org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType.ABSOLUTE_VALUE;
            default:
                throw new IllegalArgumentException("Unknown log max memory used threshold type value: " + logMaxMemoryUsedThresholdType);
        }
    }

    public static LogMaxMemoryUsedThresholdType mapToModel(
            org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType logMaxMemoryUsedThresholdType) {
        if (logMaxMemoryUsedThresholdType == null) {
            return null;
        }

        switch (logMaxMemoryUsedThresholdType) {
            case PERCENTAGE:
                return LogMaxMemoryUsedThresholdType.PERCENTAGE;
            case ABSOLUTE_VALUE:
                return LogMaxMemoryUsedThresholdType.ABSOLUTE_VALUE_IN_MB;
            default:
                throw new IllegalArgumentException("Unknown log max memory used threshold type value: " + logMaxMemoryUsedThresholdType);
        }
    }
}
