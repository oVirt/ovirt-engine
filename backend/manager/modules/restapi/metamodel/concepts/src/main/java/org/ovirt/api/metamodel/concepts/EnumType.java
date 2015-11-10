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

package org.ovirt.api.metamodel.concepts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class represents an enumerated type.
 */
public class EnumType extends Type {
    // The valid values of the enumerated tyupe:
    private List<EnumValue> values = new ArrayList<>();

    /**
     * Returns the list of values of this enumerated type. The returned list is a copy of the one used internally, so it
     * is safe to modify it in any way. If you aren't going to modify the list consider using the {@link #values()}
     * method * instead.
     */
    public List<EnumValue> getValues() {
        return Collections.unmodifiableList(values);
    }

    /**
     * Returns a stream that delivers the values of this enumerated type.
     */
    public Stream<EnumValue> values() {
        return values.stream();
    }

    /**
     * Adds a new value to this enumerated type.
     */
    public void addValue(EnumValue value) {
        values.add(value);
    }
}

