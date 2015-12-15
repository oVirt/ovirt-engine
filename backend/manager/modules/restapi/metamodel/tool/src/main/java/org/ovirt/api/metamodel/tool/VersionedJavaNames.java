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

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.api.metamodel.concepts.Name;

/**
 * This class contains the rules used to calculate the names of generated Java concepts taking into account the
 * version prefix.
 */
@ApplicationScoped
@Style("versioned")
public class VersionedJavaNames implements JavaNames {
    /**
     * Reference to the object used to calculate plain Java names.
     */
    @Inject
    @Style("plain")
    private JavaNames javaNames;

    /**
     * Version prefix to add to all the generate class names.
     */
    private String versionPrefix;

    /**
     * Get the version prefix.
     */
    public String getVersionPrefix() {
        return versionPrefix;
    }

    /**
     * Set the version prefix.
     */
    public void setVersionPrefix(String newVersionPrefix) {
        versionPrefix = newVersionPrefix;
    }

    /**
     */
    public String getJavaClassStyleName(Name name) {
        // Classes need to have the version prefix.
        return javaNames.getJavaClassStyleName(addPrefix(name));
    }

    /**
     * Members don't need the version prefix.
     */
    public String getJavaMemberStyleName(Name name) {
        return javaNames.getJavaMemberStyleName(addPrefix(name));
    }

    /**
     * Returns a representation of the given name using the capitalization style typically used for Java constants: all
     * the words in uppercase and separated by underscores.
     */
    public String getJavaConstantStyleName(Name name) {
        return javaNames.getJavaConstantStyleName(addPrefix(name));
    }

    /**
     * Adds the version prefix to the given name.
     */
    private Name addPrefix(Name name) {
        if (versionPrefix != null && !versionPrefix.isEmpty()) {
            List<String> words = name.getWords();
            words.add(0, versionPrefix);
            name = new Name(words);
        }
        return name;
    }
}

