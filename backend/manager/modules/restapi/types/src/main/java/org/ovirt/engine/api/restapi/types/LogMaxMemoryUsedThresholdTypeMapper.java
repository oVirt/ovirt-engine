/*
Copyright (c) 2018 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
