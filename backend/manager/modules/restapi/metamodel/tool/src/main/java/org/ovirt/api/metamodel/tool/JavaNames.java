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

import static java.util.stream.Collectors.joining;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.api.metamodel.concepts.Name;

/**
 * This class contains the rules used to calculate the names of generated Java concepts.
 */
@ApplicationScoped
public class JavaNames {
    /**
     * Reference to the object used to do computations with words.
     */
    @Inject private Words words;

    /**
     * Returns a representation of the given name using the capitalization style typically used for Java classes: the
     * first letter of each word in upper case and the rest of the letters in lower case.
     */
    public String getJavaClassStyleName(Name name) {
        return name.words().map(words::capitalize).collect(joining());
    }

    /**
     * Returns a representation of the given name using the capitalization style typically used for Java members: the
     * the first word in lower case and the rest of the words with the first letter in upper case and the rest of the
     * letters in lower case.
     */
    public String getJavaMemberStyleName(Name name) {
        StringBuilder buffer = new StringBuilder();
        name.words().findFirst().map(String::toLowerCase).ifPresent(buffer::append);
        name.words().skip(1).map(words::capitalize).forEach(buffer::append);
        return buffer.toString();
    }

    /**
     * Returns a representation of the given name using the capitalization style typically used for Java constants: all
     * the words in uppercase and separated by underscores.
     */
    public String getJavaConstantStyleName(Name name) {
        return name.words().map(String::toUpperCase).collect(joining("_"));
    }
}

