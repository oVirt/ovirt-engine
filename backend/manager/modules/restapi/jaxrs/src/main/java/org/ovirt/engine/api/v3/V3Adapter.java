/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
