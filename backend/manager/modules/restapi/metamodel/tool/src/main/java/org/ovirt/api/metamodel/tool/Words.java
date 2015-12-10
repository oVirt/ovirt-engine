/*
Copyright (c) 2015 Red Hat, Inc.

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

package org.ovirt.api.metamodel.tool;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * This class contains methods useful to do computations with words.
 */
@ApplicationScoped
public class Words {
    // Exceptions to the rules to calculate plurals and singulars:
    private Map<String, String> plurals = new HashMap<>();
    private Map<String, String> singulars = new HashMap<>();

    @PostConstruct
    private void init() {
        // Populate the plurals exceptions:
        plurals.put("display", "displays");
        plurals.put("erratum", "errata");
        plurals.put("key", "keys");

        // Populate the singulars exceptions:
        singulars.put("errata", "erratum");
    }

    public String getSingular(String plural) {
        String singular = singulars.get(plural);
        if (singular == null) {
            if (plural.endsWith("ies")) {
                singular = plural.substring(0, plural.length() - 3) + "y";
            }
            else if (plural.endsWith("s")) {
                singular = plural.substring(0, plural.length() - 1);
            }
            else {
                singular = plural;
            }
        }
        return singular;
    }

    public String getPlural(String singular) {
        String plural = plurals.get(singular);
        if (plural == null) {
            if (singular.endsWith("y")) {
                plural = singular.substring(0, singular.length() - 1) + "ies";
            }
            else {
                plural = singular + "s";
            }
        }
        return plural;
    }

    public String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}

