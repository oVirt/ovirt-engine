/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Status;

public class StatusUtils {

    public static Status create(String statusStr) {
        if (StringUtils.isEmpty(statusStr)) {
            return null;
        } else {
            Status status = new Status();
            status.setState(statusStr.toLowerCase());
            return status;
        }
    }

    public static<E extends Enum<E>> Status create(E statusEnum) {
        if (statusEnum==null) {
            return null;
        } else {
            return create(statusEnum.name());
        }
    }

    public static <E extends Enum<E>> boolean exists(Class<E> enumClass, String enumValue) {
        return getEnumValue(enumClass, enumValue)!=null;
    }

    public static<E extends Enum<E>> E getEnumValue(Class<E> enumClass, String enumValue) {
        try {
            return Enum.<E> valueOf(enumClass, enumValue);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static CreationStatus getRequestStatus(Status status) {
        return status==null ? null : getRequestStatus(status.getState());
    }

    public static CreationStatus getRequestStatus(String status) {
        if ( (status==null) || status.isEmpty()) {
            return null;
        } else {
            return getEnumValue(CreationStatus.class, status);
        }
    }
}
