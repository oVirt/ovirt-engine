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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacementRule {
    private Pattern pattern;
    private String replacement;

    public ReplacementRule(String theExpression, String theReplacement) {
        pattern = Pattern.compile(theExpression);
        replacement = theReplacement;
    }

    /**
     * Checks if the given text matches the pattern of this rule. If it does then the replacement is applied and the
     * result returned. If it doesn't match then {@code null} is returned.
     */
    public String process(String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.replaceAll(replacement);
    }
}

