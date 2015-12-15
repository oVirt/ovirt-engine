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

import org.ovirt.api.metamodel.concepts.Name;

/**
 * This interface specifies the rules used to calculate the names of generated Java concepts. These rules may have
 * different implementations, like rules to generate plain traditional Java names and rules to generate versioned
 * Java names.
 */
public interface JavaNames {
    /**
     * Returns a representation of the given name using the capitalization style typically used for Java classes: the
     * first letter of each word in upper case and the rest of the letters in lower case.
     */
    String getJavaClassStyleName(Name name);

    /**
     * Returns a representation of the given name using the capitalization style typically used for Java members: the
     * the first word in lower case and the rest of the words with the first letter in upper case and the rest of the
     * letters in lower case.
     */
    String getJavaMemberStyleName(Name name);

    /**
     * Returns a representation of the given name using the capitalization style typically used for Java constants: all
     * the words in uppercase and separated by underscores.
     */
    String getJavaConstantStyleName(Name name);
}

