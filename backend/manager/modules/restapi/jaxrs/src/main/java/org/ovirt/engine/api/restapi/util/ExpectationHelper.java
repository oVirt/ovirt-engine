/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;

/**
 * This class contains helper methods to work with the {@code Expect} header.
 */
public class ExpectationHelper {
    /**
     * The names of the headers that can contain expectations. By default the HTTP {@code Expect} should be used, but
     * this is rejected by the Apache web server if the value is anything other than {@code 100-continue}, so we also
     * accept {@code X-Ovirt-Expect}.
     */
    private static final String[] HEADERS = {
        "Expect",
        "X-Ovirt-Expect"
    };

    /**
     * Return the values contained in the {@code Expect} and {@code X-Ovirt-Expect} headers.
     */
    public static Set<String> getExpectations(HttpHeaders headers) {
        Set<String> expectations = new HashSet<>();
        if (headers == null) {
            return expectations;
        }

        for (String header : HEADERS) {
            List<String> values = headers.getRequestHeader(header);
            if (values != null) {
                expectations.addAll(values);
            }
        }
        return expectations;
    }
}
