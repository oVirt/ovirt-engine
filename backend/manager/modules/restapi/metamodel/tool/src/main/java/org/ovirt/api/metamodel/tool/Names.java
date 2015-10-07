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
 * This class contains methods useful to do computations with names.
 */
@ApplicationScoped
public class Names {
    @Inject Words words;

    public Name getPlural(Name singular) {
        List<String> all = singular.getWords();
        String last = all.get(all.size() - 1);
        last = words.getPlural(last);
        all.set(all.size() - 1, last);
        return new Name(all);
    }

    public Name getSingular(Name plural) {
        List<String> all = plural.getWords();
        String last = all.get(all.size() - 1);
        last = words.getSingular(last);
        all.set(all.size() - 1, last);
        return new Name(all);
    }
}

