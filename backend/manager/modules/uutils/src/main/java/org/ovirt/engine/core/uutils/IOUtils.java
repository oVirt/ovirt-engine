/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/
package org.ovirt.engine.core.uutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtils {
    private static final Logger log = LoggerFactory.getLogger(IOUtils.class);

    /**
     * Tries to close the resource if it's not {@code null} and ignores any exceptions raised during closing
     *
     * @param resource
     *            a resource to close
     */
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ex) {
                log.debug("Ignored exception", ex);
            }
        }
    }
}
