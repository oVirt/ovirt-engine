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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;

public class DetailHelper {

    private static final String ACCEPT = "Accept";
    private static final String DETAIL = "detail";
    private static final String PARAM_SEPARATOR = ";";
    private static final String VALUE_SEPARATOR = "=";
    private static final String DETAIL_SEPARATOR = "\\+";

    public static boolean include(HttpHeaders httpheaders, String relation) {
        List<String> accepts = httpheaders.getRequestHeader(ACCEPT);
        if (!(accepts == null || accepts.isEmpty())) {
            String[] parameters = accepts.get(0).split(PARAM_SEPARATOR);
            for (String parameter : parameters) {
                String[] includes = parameter.split(VALUE_SEPARATOR);
                if (includes.length > 1 && DETAIL.equalsIgnoreCase(includes[0].trim())) {
                    for (String rel : includes[1].trim().split(DETAIL_SEPARATOR)) {
                        if (relation.equalsIgnoreCase(rel.trim())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public enum Detail {
        DISKS,
        NICS,
        STATISTICS,
        TAGS
    }

    public static Set<Detail> getDetails(HttpHeaders httpHeaders) {
        Set<Detail> details = EnumSet.noneOf(Detail.class);
        for (Detail detail : Detail.class.getEnumConstants()) {
            if (include(httpHeaders, detail.name().toLowerCase())) {
                details.add(detail);
            }
        }
        return details;
    }
}
