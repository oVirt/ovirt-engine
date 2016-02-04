/*
Copyright (c) 2016 Red Hat, Inc.

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

package org.ovirt.engine.api.v3;

/**
 * This interface represents an adapter that transforms an object from one source version of the API to another target
 * version.
 *
 * @param <FROM> the type of the source object
 * @param <TO> the type of the target object
 */
public interface V3Adapter<FROM, TO> {
    /**
     * Creates a new instance of the @{code TO} type and populates it copying the contents from the {@code from} object.
     *
     * @param from the object to copy the content from
     * @return a new instance of the {@code TO} type with the contents conpied from the {@code from} object
     */
    TO adapt(FROM from);
}
